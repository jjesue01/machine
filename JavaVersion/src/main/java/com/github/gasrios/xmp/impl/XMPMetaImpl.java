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
import java.util.Iterator;

import com.github.gasrios.xmp.XMPConst;
import com.github.gasrios.xmp.XMPDateTime;
import com.github.gasrios.xmp.XMPError;
import com.github.gasrios.xmp.XMPException;
import com.github.gasrios.xmp.XMPIterator;
import com.github.gasrios.xmp.XMPMeta;
import com.github.gasrios.xmp.XMPPathFactory;
import com.github.gasrios.xmp.XMPUtils;
import com.github.gasrios.xmp.impl.xpath.XMPPath;
import com.github.gasrios.xmp.impl.xpath.XMPPathParser;
import com.github.gasrios.xmp.options.IteratorOptions;
import com.github.gasrios.xmp.options.ParseOptions;
import com.github.gasrios.xmp.options.PropertyOptions;
import com.github.gasrios.xmp.properties.XMPProperty;

public class XMPMetaImpl implements XMPMeta, XMPConst {

	private static final int VALUE_STRING = 0;

	private static final int VALUE_BOOLEAN = 1;

	private static final int VALUE_INTEGER = 2;

	private static final int VALUE_LONG = 3;

	private static final int VALUE_DOUBLE = 4;

	private static final int VALUE_DATE = 5;

	private static final int VALUE_CALENDAR = 6;

	private static final int VALUE_BASE64 = 7;

	private XMPNode tree;

	private String packetHeader = null;

	public XMPMetaImpl() {
		tree = new XMPNode(null, null, null);
	}

	public XMPMetaImpl(XMPNode tree) {
		this.tree = tree;
	}

	public void appendArrayItem(String schemaNS, String arrayName, PropertyOptions arrayOptions, String itemValue, PropertyOptions itemOptions)
	throws XMPException {
		ParameterAsserts.assertSchemaNS(schemaNS);
		ParameterAsserts.assertArrayName(arrayName);
		if (arrayOptions == null) arrayOptions = new PropertyOptions();
		if (!arrayOptions.isOnlyArrayOptions()) throw new XMPException("Only array form flags allowed for arrayOptions", XMPError.BADOPTIONS);
		arrayOptions = XMPNodeUtils.verifySetOptions(arrayOptions, null);
		XMPPath arrayPath = XMPPathParser.expandXPath(schemaNS, arrayName);
		XMPNode arrayNode = XMPNodeUtils.findNode(tree, arrayPath, false, null);
		if (arrayNode != null) {
			if (!arrayNode.getOptions().isArray()) throw new XMPException("The named property is not an array", XMPError.BADXPATH);
		} else {
			if (arrayOptions.isArray()) {
				arrayNode = XMPNodeUtils.findNode(tree, arrayPath, true, arrayOptions);
				if (arrayNode == null) throw new XMPException("Failure creating array node", XMPError.BADXPATH);
			} else throw new XMPException("Explicit arrayOptions required to create new array", XMPError.BADOPTIONS);
		}
		doSetArrayItem(arrayNode, ARRAY_LAST_ITEM, itemValue, itemOptions, true);
	}

	public void appendArrayItem(String schemaNS, String arrayName, String itemValue) throws XMPException {
		appendArrayItem(schemaNS, arrayName, null, itemValue, null);
	}

	public int countArrayItems(String schemaNS, String arrayName) throws XMPException {
		ParameterAsserts.assertSchemaNS(schemaNS);
		ParameterAsserts.assertArrayName(arrayName);
		XMPNode arrayNode = XMPNodeUtils.findNode(tree, XMPPathParser.expandXPath(schemaNS, arrayName), false, null);
		if (arrayNode == null) return 0;
		if (arrayNode.getOptions().isArray()) return arrayNode.getChildrenLength();
		else throw new XMPException("The named property is not an array", XMPError.BADXPATH);
	}

	public void deleteArrayItem(String schemaNS, String arrayName, int itemIndex) {
		try {
			ParameterAsserts.assertSchemaNS(schemaNS);
			ParameterAsserts.assertArrayName(arrayName);
			deleteProperty(schemaNS, XMPPathFactory.composeArrayItemPath(arrayName, itemIndex));
		} catch (XMPException e) {}
	}

	public void deleteProperty(String schemaNS, String propName) {
		try {
			ParameterAsserts.assertSchemaNS(schemaNS);
			ParameterAsserts.assertPropName(propName);
			XMPNode propNode = XMPNodeUtils.findNode(tree, XMPPathParser.expandXPath(schemaNS, propName), false, null);
			if (propNode != null) XMPNodeUtils.deleteNode(propNode);
		} catch (XMPException e) {}
	}

	public void deleteQualifier(String schemaNS, String propName, String qualNS, String qualName) {
		try {
			ParameterAsserts.assertSchemaNS(schemaNS);
			ParameterAsserts.assertPropName(propName);
			deleteProperty(schemaNS, propName + XMPPathFactory.composeQualifierPath(qualNS, qualName));
		} catch (XMPException e) {}
	}

	public void deleteStructField(String schemaNS, String structName, String fieldNS, String fieldName) {
		try {
			ParameterAsserts.assertSchemaNS(schemaNS);
			ParameterAsserts.assertStructName(structName);
			deleteProperty(schemaNS, structName + XMPPathFactory.composeStructFieldPath(fieldNS, fieldName));
		} catch (XMPException e) {}
	}

	public boolean doesPropertyExist(String schemaNS, String propName) {
		try {
			ParameterAsserts.assertSchemaNS(schemaNS);
			ParameterAsserts.assertPropName(propName);
			return XMPNodeUtils.findNode(tree, XMPPathParser.expandXPath(schemaNS, propName), false, null) != null;
		} catch (XMPException e) {
			return false;
		}
	}

	public boolean doesArrayItemExist(String schemaNS, String arrayName, int itemIndex) {
		try {
			ParameterAsserts.assertSchemaNS(schemaNS);
			ParameterAsserts.assertArrayName(arrayName);
			return doesPropertyExist(schemaNS, XMPPathFactory.composeArrayItemPath(arrayName, itemIndex));
		} catch (XMPException e) {
			return false;
		}
	}

	public boolean doesStructFieldExist(String schemaNS, String structName, String fieldNS, String fieldName) {
		try {
			ParameterAsserts.assertSchemaNS(schemaNS);
			ParameterAsserts.assertStructName(structName);
			return doesPropertyExist(schemaNS, structName + XMPPathFactory.composeStructFieldPath(fieldNS, fieldName));
		} catch (XMPException e) {
			return false;
		}
	}

	public boolean doesQualifierExist(String schemaNS, String propName, String qualNS, String qualName) {
		try {
			ParameterAsserts.assertSchemaNS(schemaNS);
			ParameterAsserts.assertPropName(propName);
			return doesPropertyExist(schemaNS, propName + XMPPathFactory.composeQualifierPath(qualNS, qualName));
		} catch (XMPException e) {
			return false;
		}
	}

	public XMPProperty getArrayItem(String schemaNS, String arrayName, int itemIndex) throws XMPException {
		ParameterAsserts.assertSchemaNS(schemaNS);
		ParameterAsserts.assertArrayName(arrayName);
		return getProperty(schemaNS, XMPPathFactory.composeArrayItemPath(arrayName, itemIndex));
	}

	public XMPProperty getLocalizedText(String schemaNS, String altTextName, String genericLang, String specificLang) throws XMPException {
		ParameterAsserts.assertSchemaNS(schemaNS);
		ParameterAsserts.assertArrayName(altTextName);
		ParameterAsserts.assertSpecificLang(specificLang);
		genericLang = genericLang != null ? Utils.normalizeLangValue(genericLang) : null;
		specificLang = Utils.normalizeLangValue(specificLang);
		XMPNode arrayNode = XMPNodeUtils.findNode(tree, XMPPathParser.expandXPath(schemaNS, altTextName), false, null);
		if (arrayNode == null) return null;
		Object[] result = XMPNodeUtils.chooseLocalizedText(arrayNode, genericLang, specificLang);
		int match = ((Integer) result[0]).intValue();
		final XMPNode itemNode = (XMPNode) result[1];
		if (match != XMPNodeUtils.CLT_NO_VALUES) {
			return new XMPProperty() {
				public String getValue() {
					return itemNode.getValue();
				}
				public PropertyOptions getOptions() {
					return itemNode.getOptions();
				}
				public String getLanguage() {
					return itemNode.getQualifier(1).getValue();
				}
				public String toString() {
					return itemNode.getValue().toString();
				}
			};
		} else return null;
	}

	public void setLocalizedText(String schemaNS, String altTextName, String genericLang, String specificLang, String itemValue, PropertyOptions options)
	throws XMPException {
		ParameterAsserts.assertSchemaNS(schemaNS);
		ParameterAsserts.assertArrayName(altTextName);
		ParameterAsserts.assertSpecificLang(specificLang);
		genericLang = genericLang != null ? Utils.normalizeLangValue(genericLang) : null;
		specificLang = Utils.normalizeLangValue(specificLang);
		XMPNode arrayNode =
			XMPNodeUtils.findNode(
				tree,
				XMPPathParser.expandXPath(schemaNS, altTextName),
				true,
				new PropertyOptions(
					PropertyOptions.ARRAY |
					PropertyOptions.ARRAY_ORDERED |
					PropertyOptions.ARRAY_ALTERNATE |
					PropertyOptions.ARRAY_ALT_TEXT)
			);
		if (arrayNode == null) throw new XMPException("Failed to find or create array node", XMPError.BADXPATH);
		else if (!arrayNode.getOptions().isArrayAltText())
			if (!arrayNode.hasChildren() && arrayNode.getOptions().isArrayAlternate()) arrayNode.getOptions().setArrayAltText(true);
			else throw new XMPException("Specified property is no alt-text array", XMPError.BADXPATH);
		boolean haveXDefault = false;
		XMPNode xdItem = null;
		for (Iterator<XMPNode> it = arrayNode.iterateChildren(); it.hasNext();) {
			XMPNode currItem = it.next();
			if (!currItem.hasQualifier() || !XMPConst.XML_LANG.equals(currItem.getQualifier(1).getName()))
				throw new XMPException("Language qualifier must be first", XMPError.BADXPATH);
			else if (XMPConst.X_DEFAULT.equals(currItem.getQualifier(1).getValue())) {
				xdItem = currItem;
				haveXDefault = true;
				break;
			}
		}
		if (xdItem != null && arrayNode.getChildrenLength() > 1) {
			arrayNode.removeChild(xdItem);
			arrayNode.addChild(1, xdItem);
		}
		Object[] result = XMPNodeUtils.chooseLocalizedText(arrayNode, genericLang, specificLang);
		int match = ((Integer) result[0]).intValue();
		XMPNode itemNode = (XMPNode) result[1];
		boolean specificXDefault = XMPConst.X_DEFAULT.equals(specificLang);
		switch (match) {
		case XMPNodeUtils.CLT_NO_VALUES:
			XMPNodeUtils.appendLangItem(arrayNode, XMPConst.X_DEFAULT, itemValue);
			haveXDefault = true;
			if (!specificXDefault) XMPNodeUtils.appendLangItem(arrayNode, specificLang, itemValue);
			break;
		case XMPNodeUtils.CLT_SPECIFIC_MATCH:
			if (!specificXDefault) {
				if (haveXDefault && xdItem != itemNode && xdItem != null && xdItem.getValue().equals(itemNode.getValue()))
					xdItem.setValue(itemValue);
				itemNode.setValue(itemValue);
			} else {
				assert haveXDefault && xdItem == itemNode;
				for (Iterator<XMPNode> it = arrayNode.iterateChildren(); it.hasNext();) {
					XMPNode currItem = it.next();
					if (currItem == xdItem || !currItem.getValue().equals(xdItem != null ? xdItem.getValue() : null)) continue;
					currItem.setValue(itemValue);
				}
				if (xdItem != null) xdItem.setValue(itemValue);
			}
			break;
		case XMPNodeUtils.CLT_SINGLE_GENERIC:
			if (haveXDefault && xdItem != itemNode && xdItem != null && xdItem.getValue().equals(itemNode.getValue())) xdItem.setValue(itemValue);
			itemNode.setValue(itemValue);
			break;
		case XMPNodeUtils.CLT_MULTIPLE_GENERIC:
			XMPNodeUtils.appendLangItem(arrayNode, specificLang, itemValue);
			if (specificXDefault) haveXDefault = true;
			break;
		case XMPNodeUtils.CLT_XDEFAULT:
			if (xdItem != null && arrayNode.getChildrenLength() == 1) xdItem.setValue(itemValue);
			XMPNodeUtils.appendLangItem(arrayNode, specificLang, itemValue);
			break;
		case XMPNodeUtils.CLT_FIRST_ITEM:
			XMPNodeUtils.appendLangItem(arrayNode, specificLang, itemValue);
			if (specificXDefault) haveXDefault = true;
			break;
		default:
			throw new XMPException("Unexpected result from ChooseLocalizedText", XMPError.INTERNALFAILURE);
		}
		if (!haveXDefault && arrayNode.getChildrenLength() == 1) XMPNodeUtils.appendLangItem(arrayNode, XMPConst.X_DEFAULT, itemValue);
	}

	public void setLocalizedText(String schemaNS, String altTextName, String genericLang, String specificLang, String itemValue) throws XMPException {
		setLocalizedText(schemaNS, altTextName, genericLang, specificLang, itemValue, null);
	}

	public XMPProperty getProperty(String schemaNS, String propName) throws XMPException {
		return getProperty(schemaNS, propName, VALUE_STRING);
	}

	protected XMPProperty getProperty(String schemaNS, String propName, int valueType) throws XMPException {
		ParameterAsserts.assertSchemaNS(schemaNS);
		ParameterAsserts.assertPropName(propName);
		final XMPNode propNode = XMPNodeUtils.findNode(tree, XMPPathParser.expandXPath(schemaNS, propName), false, null);
		if (propNode != null) {
			if (valueType != VALUE_STRING && propNode.getOptions().isCompositeProperty())
				throw new XMPException("Property must be simple when a value type is requested", XMPError.BADXPATH);
			final Object value = evaluateNodeValue(valueType, propNode);
			return new XMPProperty() {
				public String getValue() {
					return value != null ? value.toString() : null;
				}
				public PropertyOptions getOptions() {
					return propNode.getOptions();
				}
				public String getLanguage() {
					return null;
				}
				public String toString() {
					return value.toString();
				}
			};
		} else return null;
	}

	protected Object getPropertyObject(String schemaNS, String propName, int valueType) throws XMPException {
		ParameterAsserts.assertSchemaNS(schemaNS);
		ParameterAsserts.assertPropName(propName);
		final XMPNode propNode = XMPNodeUtils.findNode(tree, XMPPathParser.expandXPath(schemaNS, propName), false, null);
		if (propNode != null) {
			if (valueType != VALUE_STRING && propNode.getOptions().isCompositeProperty())
				throw new XMPException("Property must be simple when a value type is requested", XMPError.BADXPATH);
			return evaluateNodeValue(valueType, propNode);
		} else return null;
	}

	public Boolean getPropertyBoolean(String schemaNS, String propName) throws XMPException {
		return (Boolean) getPropertyObject(schemaNS, propName, VALUE_BOOLEAN);
	}

	public void setPropertyBoolean(String schemaNS, String propName, boolean propValue, PropertyOptions options) throws XMPException {
		setProperty(schemaNS, propName, propValue ? TRUESTR : FALSESTR, options);
	}

	public void setPropertyBoolean(String schemaNS, String propName, boolean propValue) throws XMPException {
		setProperty(schemaNS, propName, propValue ? TRUESTR : FALSESTR, null);
	}

	public Integer getPropertyInteger(String schemaNS, String propName) throws XMPException {
		return (Integer) getPropertyObject(schemaNS, propName, VALUE_INTEGER);
	}

	public void setPropertyInteger(String schemaNS, String propName, int propValue, PropertyOptions options) throws XMPException {
		setProperty(schemaNS, propName, new Integer(propValue), options);
	}

	public void setPropertyInteger(String schemaNS, String propName, int propValue) throws XMPException {
		setProperty(schemaNS, propName, new Integer(propValue), null);
	}

	public Long getPropertyLong(String schemaNS, String propName) throws XMPException {
		return (Long) getPropertyObject(schemaNS, propName, VALUE_LONG);
	}

	public void setPropertyLong(String schemaNS, String propName, long propValue, PropertyOptions options) throws XMPException {
		setProperty(schemaNS, propName, new Long(propValue), options);
	}

	public void setPropertyLong(String schemaNS, String propName, long propValue) throws XMPException {
		setProperty(schemaNS, propName, new Long(propValue), null);
	}

	public Double getPropertyDouble(String schemaNS, String propName) throws XMPException {
		return (Double) getPropertyObject(schemaNS, propName, VALUE_DOUBLE);
	}

	public void setPropertyDouble(String schemaNS, String propName, double propValue, PropertyOptions options) throws XMPException {
		setProperty(schemaNS, propName, new Double(propValue), options);
	}

	public void setPropertyDouble(String schemaNS, String propName, double propValue) throws XMPException {
		setProperty(schemaNS, propName, new Double(propValue), null);
	}

	public XMPDateTime getPropertyDate(String schemaNS, String propName) throws XMPException {
		return (XMPDateTime) getPropertyObject(schemaNS, propName, VALUE_DATE);
	}

	public void setPropertyDate(String schemaNS, String propName, XMPDateTime propValue, PropertyOptions options) throws XMPException {
		setProperty(schemaNS, propName, propValue, options);
	}

	public void setPropertyDate(String schemaNS, String propName, XMPDateTime propValue) throws XMPException {
		setProperty(schemaNS, propName, propValue, null);
	}

	public Calendar getPropertyCalendar(String schemaNS, String propName) throws XMPException {
		return (Calendar) getPropertyObject(schemaNS, propName, VALUE_CALENDAR);
	}

	public void setPropertyCalendar(String schemaNS, String propName, Calendar propValue, PropertyOptions options) throws XMPException {
		setProperty(schemaNS, propName, propValue, options);
	}

	public void setPropertyCalendar(String schemaNS, String propName, Calendar propValue) throws XMPException {
		setProperty(schemaNS, propName, propValue, null);
	}

	public byte[] getPropertyBase64(String schemaNS, String propName) throws XMPException {
		return (byte[]) getPropertyObject(schemaNS, propName, VALUE_BASE64);
	}

	public String getPropertyString(String schemaNS, String propName) throws XMPException {
		return (String) getPropertyObject(schemaNS, propName, VALUE_STRING);
	}

	public void setPropertyBase64(String schemaNS, String propName, byte[] propValue, PropertyOptions options) throws XMPException {
		setProperty(schemaNS, propName, propValue, options);
	}

	public void setPropertyBase64(String schemaNS, String propName, byte[] propValue) throws XMPException {
		setProperty(schemaNS, propName, propValue, null);
	}

	public XMPProperty getQualifier(String schemaNS, String propName, String qualNS, String qualName) throws XMPException {
		ParameterAsserts.assertSchemaNS(schemaNS);
		ParameterAsserts.assertPropName(propName);
		return getProperty(schemaNS, propName + XMPPathFactory.composeQualifierPath(qualNS, qualName));
	}

	public XMPProperty getStructField(String schemaNS, String structName, String fieldNS, String fieldName) throws XMPException {
		ParameterAsserts.assertSchemaNS(schemaNS);
		ParameterAsserts.assertStructName(structName);
		return getProperty(schemaNS, structName + XMPPathFactory.composeStructFieldPath(fieldNS, fieldName));
	}

	public XMPIterator iterator() throws XMPException {
		return iterator(null, null, null);
	}

	public XMPIterator iterator(IteratorOptions options) throws XMPException {
		return iterator(null, null, options);
	}

	public XMPIterator iterator(String schemaNS, String propName, IteratorOptions options) throws XMPException {
		return new XMPIteratorImpl(this, schemaNS, propName, options);
	}

	public void setArrayItem(String schemaNS, String arrayName, int itemIndex, String itemValue, PropertyOptions options) throws XMPException {
		ParameterAsserts.assertSchemaNS(schemaNS);
		ParameterAsserts.assertArrayName(arrayName);
		XMPNode arrayNode = XMPNodeUtils.findNode(tree, XMPPathParser.expandXPath(schemaNS, arrayName), false, null);
		if (arrayNode != null) doSetArrayItem(arrayNode, itemIndex, itemValue, options, false);
		else throw new XMPException("Specified array does not exist", XMPError.BADXPATH);
	}

	public void setArrayItem(String schemaNS, String arrayName, int itemIndex, String itemValue) throws XMPException {
		setArrayItem(schemaNS, arrayName, itemIndex, itemValue, null);
	}

	public void insertArrayItem(String schemaNS, String arrayName, int itemIndex, String itemValue, PropertyOptions options) throws XMPException {
		ParameterAsserts.assertSchemaNS(schemaNS);
		ParameterAsserts.assertArrayName(arrayName);
		XMPNode arrayNode = XMPNodeUtils.findNode(tree, XMPPathParser.expandXPath(schemaNS, arrayName), false, null);
		if (arrayNode != null) doSetArrayItem(arrayNode, itemIndex, itemValue, options, true);
		else throw new XMPException("Specified array does not exist", XMPError.BADXPATH);
	}

	public void insertArrayItem(String schemaNS, String arrayName, int itemIndex, String itemValue) throws XMPException {
		insertArrayItem(schemaNS, arrayName, itemIndex, itemValue, null);
	}

	public void setProperty(String schemaNS, String propName, Object propValue, PropertyOptions options) throws XMPException {
		ParameterAsserts.assertSchemaNS(schemaNS);
		ParameterAsserts.assertPropName(propName);
		options = XMPNodeUtils.verifySetOptions(options, propValue);
		XMPNode propNode = XMPNodeUtils.findNode(tree, XMPPathParser.expandXPath(schemaNS, propName), true, options);
		if (propNode != null) setNode(propNode, propValue, options, false);
		else throw new XMPException("Specified property does not exist", XMPError.BADXPATH);
	}

	public void setProperty(String schemaNS, String propName, Object propValue) throws XMPException {
		setProperty(schemaNS, propName, propValue, null);
	}

	public void setQualifier(String schemaNS, String propName, String qualNS, String qualName, String qualValue, PropertyOptions options) throws XMPException {
		ParameterAsserts.assertSchemaNS(schemaNS);
		ParameterAsserts.assertPropName(propName);
		if (!doesPropertyExist(schemaNS, propName)) throw new XMPException("Specified property does not exist!", XMPError.BADXPATH);
		String qualPath = propName + XMPPathFactory.composeQualifierPath(qualNS, qualName);
		setProperty(schemaNS, qualPath, qualValue, options);
	}

	public void setQualifier(String schemaNS, String propName, String qualNS, String qualName, String qualValue) throws XMPException {
		setQualifier(schemaNS, propName, qualNS, qualName, qualValue, null);
	}

	public void setStructField(String schemaNS, String structName, String fieldNS, String fieldName, String fieldValue, PropertyOptions options)
	throws XMPException {
		ParameterAsserts.assertSchemaNS(schemaNS);
		ParameterAsserts.assertStructName(structName);
		setProperty(schemaNS, structName + XMPPathFactory.composeStructFieldPath(fieldNS, fieldName), fieldValue, options);
	}

	public void setStructField(String schemaNS, String structName, String fieldNS, String fieldName, String fieldValue) throws XMPException {
		setStructField(schemaNS, structName, fieldNS, fieldName, fieldValue, null);
	}

	public String getObjectName() {
		return tree.getName() != null ? tree.getName() : "";
	}

	public void setObjectName(String name) {
		tree.setName(name);
	}

	public String getPacketHeader() {
		return packetHeader;
	}

	public void setPacketHeader(String packetHeader) {
		this.packetHeader = packetHeader;
	}

	public Object clone() {
		return new XMPMetaImpl((XMPNode) tree.clone());
	}

	public String dumpObject() {
		return getRoot().dumpNode(true);
	}

	public void sort() {
		this.tree.sort();
	}

	public void normalize(ParseOptions options) throws XMPException {
		XMPNormalizer.process(this, options != null? options : new ParseOptions());
	}

	public XMPNode getRoot() {
		return tree;
	}

	private void doSetArrayItem(XMPNode arrayNode, int itemIndex, String itemValue, PropertyOptions itemOptions, boolean insert) throws XMPException {
		XMPNode itemNode = new XMPNode(ARRAY_ITEM_NAME, null);
		itemOptions = XMPNodeUtils.verifySetOptions(itemOptions, itemValue);
		int maxIndex = insert ? arrayNode.getChildrenLength() + 1 : arrayNode.getChildrenLength();
		if (itemIndex == ARRAY_LAST_ITEM) itemIndex = maxIndex;
		if (1 <= itemIndex && itemIndex <= maxIndex) {
			if (!insert) arrayNode.removeChild(itemIndex);
			arrayNode.addChild(itemIndex, itemNode);
			setNode(itemNode, itemValue, itemOptions, false);
		} else throw new XMPException("Array index out of bounds", XMPError.BADINDEX);
	}

	void setNode(XMPNode node, Object value, PropertyOptions newOptions, boolean deleteExisting) throws XMPException {
		if (deleteExisting) node.clear();
		node.getOptions().mergeWith(newOptions);
		if (!node.getOptions().isCompositeProperty()) XMPNodeUtils.setNodeValue(node, value);
		else {
			if (value != null && value.toString().length() > 0) throw new XMPException("Composite nodes can't have values", XMPError.BADXPATH);
			node.removeChildren();
		}

	}

	private Object evaluateNodeValue(int valueType, final XMPNode propNode) throws XMPException {
		final Object value;
		String rawValue = propNode.getValue();
		switch (valueType) {
			case VALUE_BOOLEAN: value = new Boolean(XMPUtils.convertToBoolean(rawValue));
			break;
			case VALUE_INTEGER: value = new Integer(XMPUtils.convertToInteger(rawValue));
			break;
			case VALUE_LONG: value = new Long(XMPUtils.convertToLong(rawValue));
			break;
			case VALUE_DOUBLE: value = new Double(XMPUtils.convertToDouble(rawValue));
			break;
			case VALUE_DATE: value = XMPUtils.convertToDate(rawValue);
			break;
			case VALUE_CALENDAR: value = XMPUtils.convertToDate(rawValue).getCalendar();
			break;
			case VALUE_BASE64: value = XMPUtils.decodeBase64(rawValue);
			break;
			case VALUE_STRING:
			default: value = rawValue != null || propNode.getOptions().isCompositeProperty() ? rawValue : "";
			break;
		}
		return value;
	}

}