// =================================================================================================
// ADOBE SYSTEMS INCORPORATED
// Copyright 2006 Adobe Systems Incorporated
// All Rights Reserved
//
// NOTICE:  Adobe permits you to use, modify, and distribute this file in accordance with the terms
// of the Adobe license agreement accompanying it.
// =================================================================================================

package com.github.gasrios.xmp.impl;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.github.gasrios.xmp.XMPConst;
import com.github.gasrios.xmp.XMPDateTime;
import com.github.gasrios.xmp.XMPError;
import com.github.gasrios.xmp.XMPException;
import com.github.gasrios.xmp.XMPMeta;
import com.github.gasrios.xmp.XMPMetaFactory;
import com.github.gasrios.xmp.XMPUtils;
import com.github.gasrios.xmp.impl.xpath.XMPPathParser;
import com.github.gasrios.xmp.options.ParseOptions;
import com.github.gasrios.xmp.options.PropertyOptions;
import com.github.gasrios.xmp.properties.XMPAliasInfo;

public class XMPNormalizer {

	private static Map<String, PropertyOptions> dcArrayForms;

	static {
		initDCArrays();
	}

	private XMPNormalizer() {}

	static XMPMeta process(XMPMetaImpl xmp, ParseOptions options) throws XMPException {
		XMPNode tree = xmp.getRoot();
		touchUpDataModel(xmp);
		moveExplicitAliases(tree, options);
		tweakOldXMP(tree);
		deleteEmptySchemas(tree);
		return xmp;
	}

	private static void tweakOldXMP(XMPNode tree) throws XMPException {
		if (tree.getName() != null && tree.getName().length() >= Utils.UUID_LENGTH) {
			String nameStr = tree.getName().toLowerCase();
			if (nameStr.startsWith("uuid:")) nameStr = nameStr.substring(5);
			if (Utils.checkUUIDFormat(nameStr)) {
				XMPNode idNode = XMPNodeUtils.findNode(tree, XMPPathParser.expandXPath(XMPConst.NS_XMP_MM, "InstanceID"), true, null);
				if (idNode != null) {
					idNode.setOptions(null);
					idNode.setValue("uuid:" + nameStr);
					idNode.removeChildren();
					idNode.removeQualifiers();
					tree.setName(null);
				} else throw new XMPException("Failure creating xmpMM:InstanceID", XMPError.INTERNALFAILURE);
			}
		}
	}

	private static void touchUpDataModel(XMPMetaImpl xmp) throws XMPException {
		XMPNodeUtils.findSchemaNode(xmp.getRoot(), XMPConst.NS_DC, true);
		for (Iterator<XMPNode> it = xmp.getRoot().iterateChildren(); it.hasNext();) {
			XMPNode currSchema = it.next();
			if (XMPConst.NS_DC.equals(currSchema.getName())) normalizeDCArrays(currSchema);
			else if (XMPConst.NS_EXIF.equals(currSchema.getName())) {
				fixGPSTimeStamp(currSchema);
				XMPNode arrayNode = XMPNodeUtils.findChildNode(currSchema, "exif:UserComment", false);
				if (arrayNode != null) repairAltText(arrayNode);
			} else if (XMPConst.NS_DM.equals(currSchema.getName())) {
				XMPNode dmCopyright = XMPNodeUtils.findChildNode(currSchema, "xmpDM:copyright", false);
				if (dmCopyright != null) migrateAudioCopyright(xmp, dmCopyright);
			} else if (XMPConst.NS_XMP_RIGHTS.equals(currSchema.getName())) {
				XMPNode arrayNode = XMPNodeUtils.findChildNode(currSchema, "xmpRights:UsageTerms", false);
				if (arrayNode != null) repairAltText(arrayNode);
			}
		}
	}

	private static void normalizeDCArrays(XMPNode dcSchema) throws XMPException {
		for (int i = 1; i <= dcSchema.getChildrenLength(); i++) {
			XMPNode currProp = dcSchema.getChild(i);
			PropertyOptions arrayForm = (PropertyOptions) dcArrayForms.get(currProp.getName());
			if (arrayForm == null) continue;
			else if (currProp.getOptions().isSimple()) {
				XMPNode newArray = new XMPNode(currProp.getName(), arrayForm);
				currProp.setName(XMPConst.ARRAY_ITEM_NAME);
				newArray.addChild(currProp);
				dcSchema.replaceChild(i, newArray);
				if (arrayForm.isArrayAltText() && !currProp.getOptions().getHasLanguage())
					currProp.addQualifier(new XMPNode(XMPConst.XML_LANG, XMPConst.X_DEFAULT, null));
			} else {
				currProp.getOptions().setOption(
					PropertyOptions.ARRAY |
					PropertyOptions.ARRAY_ORDERED |
					PropertyOptions.ARRAY_ALTERNATE |
					PropertyOptions.ARRAY_ALT_TEXT,
					false
				);
				currProp.getOptions().mergeWith(arrayForm);
				if (arrayForm.isArrayAltText()) repairAltText(currProp);
			}
		}
	}

	private static void repairAltText(XMPNode arrayNode) throws XMPException {
		if (arrayNode == null || !arrayNode.getOptions().isArray()) return;
		arrayNode.getOptions().setArrayOrdered(true).setArrayAlternate(true).setArrayAltText(true);
		for (Iterator<XMPNode> it = arrayNode.iterateChildren(); it.hasNext();) {
			XMPNode currChild = it.next();
			if (currChild.getOptions().isCompositeProperty()) it.remove();
			else if (!currChild.getOptions().getHasLanguage()) {
				String childValue = currChild.getValue();
				if (childValue == null || childValue.length() == 0) it.remove();
				else currChild.addQualifier(new XMPNode(XMPConst.XML_LANG, "x-repair", null));
			}
		}
	}

	private static void moveExplicitAliases(XMPNode tree, ParseOptions options) throws XMPException {
		if (!tree.getHasAliases()) return;
		tree.setHasAliases(false);
		boolean strictAliasing = options.getStrictAliasing();
		for (Iterator<XMPNode> schemaIt = tree.getUnmodifiableChildren().iterator(); schemaIt.hasNext();) {
			XMPNode currSchema = schemaIt.next();
			if (!currSchema.getHasAliases()) continue;
			for (Iterator<XMPNode> propertyIt = currSchema.iterateChildren(); propertyIt.hasNext();) {
				XMPNode currProp = propertyIt.next();
				if (!currProp.isAlias()) continue;
				currProp.setAlias(false);
				XMPAliasInfo info = XMPMetaFactory.getSchemaRegistry().findAlias(currProp.getName());
				if (info != null) {
					XMPNode baseSchema = XMPNodeUtils.findSchemaNode(tree, info.getNamespace(), null, true);
					baseSchema.setImplicit(false);
					XMPNode baseNode = XMPNodeUtils.findChildNode(baseSchema, info.getPrefix() + info.getPropName(), false);
					if (baseNode == null) {
						if (info.getAliasForm().isSimple()) {
							String qname = info.getPrefix() + info.getPropName();
							currProp.setName(qname);
							baseSchema.addChild(currProp);
							propertyIt.remove();
						} else {
							baseNode = new XMPNode(info.getPrefix() + info.getPropName(), info.getAliasForm().toPropertyOptions());
							baseSchema.addChild(baseNode);
							transplantArrayItemAlias(propertyIt, currProp, baseNode);
						}
					} else if (info.getAliasForm().isSimple()) {
						if (strictAliasing) compareAliasedSubtrees(currProp, baseNode, true);
						propertyIt.remove();
					} else {
						XMPNode itemNode = null;
						if (info.getAliasForm().isArrayAltText()) {
							int xdIndex = XMPNodeUtils.lookupLanguageItem(baseNode, XMPConst.X_DEFAULT);
							if (xdIndex != -1) itemNode = baseNode.getChild(xdIndex);
						} else if (baseNode.hasChildren()) itemNode = baseNode.getChild(1);
						if (itemNode == null) transplantArrayItemAlias(propertyIt, currProp, baseNode);
						else {
							if (strictAliasing) compareAliasedSubtrees(currProp, itemNode, true);
							propertyIt.remove();
						}
					}
				}
			}
			currSchema.setHasAliases(false);
		}
	}

	private static void transplantArrayItemAlias(Iterator<XMPNode> propertyIt, XMPNode childNode, XMPNode baseArray) throws XMPException {
		if (baseArray.getOptions().isArrayAltText()) {
			if (childNode.getOptions().getHasLanguage()) throw new XMPException("Alias to x-default already has a language qualifier", XMPError.BADXMP);
			childNode.addQualifier(new XMPNode(XMPConst.XML_LANG, XMPConst.X_DEFAULT, null));
		}
		propertyIt.remove();
		childNode.setName(XMPConst.ARRAY_ITEM_NAME);
		baseArray.addChild(childNode);
	}

	private static void fixGPSTimeStamp(XMPNode exifSchema) throws XMPException {
		XMPNode gpsDateTime = XMPNodeUtils.findChildNode(exifSchema, "exif:GPSTimeStamp", false);
		if (gpsDateTime == null) return;
		try {
			XMPDateTime binGPSStamp;
			XMPDateTime binOtherDate;
			binGPSStamp = XMPUtils.convertToDate(gpsDateTime.getValue());
			if (binGPSStamp.getYear() != 0 || binGPSStamp.getMonth() != 0 || binGPSStamp.getDay() != 0) return;
			XMPNode otherDate = XMPNodeUtils.findChildNode(exifSchema, "exif:DateTimeOriginal", false);
			if (otherDate == null) otherDate = XMPNodeUtils.findChildNode(exifSchema, "exif:DateTimeDigitized", false);
			binOtherDate = XMPUtils.convertToDate(otherDate.getValue());
			Calendar cal = binGPSStamp.getCalendar();
			cal.set(Calendar.YEAR, binOtherDate.getYear());
			cal.set(Calendar.MONTH, binOtherDate.getMonth());
			cal.set(Calendar.DAY_OF_MONTH, binOtherDate.getDay());
			binGPSStamp = new XMPDateTimeImpl(cal);
			gpsDateTime.setValue(XMPUtils.convertFromDate(binGPSStamp));
		} catch (XMPException e) {
			return;
		}
	}

	private static void deleteEmptySchemas(XMPNode tree) {
		for (Iterator<XMPNode> it = tree.iterateChildren(); it.hasNext();) if (!it.next().hasChildren()) it.remove();
	}

	private static void compareAliasedSubtrees(XMPNode aliasNode, XMPNode baseNode, boolean outerCall) throws XMPException {
		if (
			!aliasNode.getValue().equals(baseNode.getValue()) ||
			aliasNode.getChildrenLength() != baseNode.getChildrenLength()
		) throw new XMPException("Mismatch between alias and base nodes", XMPError.BADXMP);
		if (
			!outerCall && (
				!aliasNode.getName().equals(baseNode.getName()) ||
				!aliasNode.getOptions().equals(baseNode.getOptions()) ||
				aliasNode.getQualifierLength() != baseNode.getQualifierLength()
			)
		) throw new XMPException("Mismatch between alias and base nodes", XMPError.BADXMP);
		for (Iterator<XMPNode> an = aliasNode.iterateChildren(), bn = baseNode.iterateChildren(); an.hasNext() && bn.hasNext();)
			compareAliasedSubtrees(an.next(), bn.next(), false);
		for (Iterator<XMPNode> an = aliasNode.iterateQualifier(), bn = baseNode.iterateQualifier(); an.hasNext() && bn.hasNext();)
			compareAliasedSubtrees(an.next(), bn.next(), false);
	}

	private static void migrateAudioCopyright(XMPMeta xmp, XMPNode dmCopyright) {
		try {
			String dmValue = dmCopyright.getValue();
			XMPNode dcRightsArray =
					XMPNodeUtils.findChildNode(XMPNodeUtils.findSchemaNode(((XMPMetaImpl) xmp).getRoot(), XMPConst.NS_DC, true), "dc:rights", false);
			if (dcRightsArray == null || !dcRightsArray.hasChildren()) {
				dmValue = "\n\n" + dmValue;
				xmp.setLocalizedText(XMPConst.NS_DC, "rights", "", XMPConst.X_DEFAULT, dmValue, null);
			} else {
				int xdIndex = XMPNodeUtils.lookupLanguageItem(dcRightsArray, XMPConst.X_DEFAULT);
				if (xdIndex < 0) {
					String firstValue = dcRightsArray.getChild(1).getValue();
					xmp.setLocalizedText(XMPConst.NS_DC, "rights", "", XMPConst.X_DEFAULT, firstValue, null);
					xdIndex = XMPNodeUtils.lookupLanguageItem(dcRightsArray, XMPConst.X_DEFAULT);
				}
				XMPNode defaultNode = dcRightsArray.getChild(xdIndex);
				String defaultValue = defaultNode.getValue();
				int lfPos = defaultValue.indexOf("\n\n");
				if (lfPos < 0) {
					if (!dmValue.equals(defaultValue)) defaultNode.setValue(defaultValue + "\n\n" + dmValue);
				} else if (!defaultValue.substring(lfPos + 2).equals(dmValue))
					defaultNode.setValue(defaultValue.substring(0, lfPos + 2) + dmValue);
			}
			dmCopyright.getParent().removeChild(dmCopyright);
		} catch (XMPException e) {}
	}

	private static void initDCArrays() {
		dcArrayForms = new HashMap<String, PropertyOptions>();
		PropertyOptions bagForm = new PropertyOptions();
		bagForm.setArray(true);
		dcArrayForms.put("dc:contributor", bagForm);
		dcArrayForms.put("dc:language", bagForm);
		dcArrayForms.put("dc:publisher", bagForm);
		dcArrayForms.put("dc:relation", bagForm);
		dcArrayForms.put("dc:subject", bagForm);
		dcArrayForms.put("dc:type", bagForm);
		PropertyOptions seqForm = new PropertyOptions();
		seqForm.setArray(true);
		seqForm.setArrayOrdered(true);
		dcArrayForms.put("dc:creator", seqForm);
		dcArrayForms.put("dc:date", seqForm);
		PropertyOptions altTextForm = new PropertyOptions();
		altTextForm.setArray(true);
		altTextForm.setArrayOrdered(true);
		altTextForm.setArrayAlternate(true);
		altTextForm.setArrayAltText(true);
		dcArrayForms.put("dc:description", altTextForm);
		dcArrayForms.put("dc:rights", altTextForm);
		dcArrayForms.put("dc:title", altTextForm);
	}

}