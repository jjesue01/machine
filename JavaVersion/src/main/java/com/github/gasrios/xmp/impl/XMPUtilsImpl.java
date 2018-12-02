// =================================================================================================
// ADOBE SYSTEMS INCORPORATED
// Copyright 2006 Adobe Systems Incorporated
// All Rights Reserved
//
// NOTICE:  Adobe permits you to use, modify, and distribute this file in accordance with the terms
// of the Adobe license agreement accompanying it.
// =================================================================================================

package com.github.gasrios.xmp.impl;

import java.util.Iterator;

import com.github.gasrios.xmp.XMPConst;
import com.github.gasrios.xmp.XMPError;
import com.github.gasrios.xmp.XMPException;
import com.github.gasrios.xmp.XMPMeta;
import com.github.gasrios.xmp.XMPMetaFactory;
import com.github.gasrios.xmp.impl.xpath.XMPPath;
import com.github.gasrios.xmp.impl.xpath.XMPPathParser;
import com.github.gasrios.xmp.options.PropertyOptions;
import com.github.gasrios.xmp.properties.XMPAliasInfo;

public class XMPUtilsImpl implements XMPConst {

	private static final int UCK_NORMAL = 0;
	private static final int UCK_SPACE = 1;
	private static final int UCK_COMMA = 2;
	private static final int UCK_SEMICOLON = 3;
	private static final int UCK_QUOTE = 4;
	private static final int UCK_CONTROL = 5;

	private static final String SPACES = "\u0020\u3000\u303F";
	private static final String COMMAS = "\u002C\uFF0C\uFF64\uFE50\uFE51\u3001\u060C\u055D";
	private static final String SEMICOLA = "\u003B\uFF1B\uFE54\u061B\u037E";
	private static final String QUOTES = "\"\u00AB\u00BB\u301D\u301E\u301F\u2015\u2039\u203A";
	private static final String CONTROLS = "\u2028\u2029";

	private XMPUtilsImpl() {}

	public static String catenateArrayItems(XMPMeta xmp, String schemaNS, String arrayName, String separator, String quotes, boolean allowCommas)
	throws XMPException {
		ParameterAsserts.assertSchemaNS(schemaNS);
		ParameterAsserts.assertArrayName(arrayName);
		ParameterAsserts.assertImplementation(xmp);
		if (separator == null || separator.length() == 0) separator = "; ";
		if (quotes == null || quotes.length() == 0) quotes = "\"";
		XMPNode arrayNode = XMPNodeUtils.findNode(((XMPMetaImpl) xmp).getRoot(), XMPPathParser.expandXPath(schemaNS, arrayName), false, null);
		if (arrayNode == null) return "";
		else if (!arrayNode.getOptions().isArray() || arrayNode.getOptions().isArrayAlternate())
			throw new XMPException("Named property must be non-alternate array", XMPError.BADPARAM);
		checkSeparator(separator);
		char openQuote = quotes.charAt(0);
		char closeQuote = checkQuotes(quotes, openQuote);
		StringBuffer catinatedString = new StringBuffer();
		XMPNode currItem = null;
		for (Iterator<XMPNode> it = arrayNode.iterateChildren(); it.hasNext();) {
			currItem = it.next();
			if (currItem.getOptions().isCompositeProperty()) throw new XMPException("Array items must be simple", XMPError.BADPARAM);
			String str = applyQuotes(currItem.getValue(), openQuote, closeQuote, allowCommas);
			catinatedString.append(str);
			if (it.hasNext()) catinatedString.append(separator);
		}
		return catinatedString.toString();
	}

	public static void separateArrayItems(
		XMPMeta xmp,
		String schemaNS,
		String arrayName,
		String catedStr,
		PropertyOptions arrayOptions,
		boolean preserveCommas)
	throws XMPException {
		ParameterAsserts.assertSchemaNS(schemaNS);
		ParameterAsserts.assertArrayName(arrayName);
		if (catedStr == null) throw new XMPException("Parameter must not be null", XMPError.BADPARAM);
		ParameterAsserts.assertImplementation(xmp);
		XMPMetaImpl xmpImpl = (XMPMetaImpl) xmp;
		XMPNode arrayNode = separateFindCreateArray(schemaNS, arrayName, arrayOptions, xmpImpl);
		String itemValue;
		int itemStart, itemEnd;
		int nextKind = UCK_NORMAL, charKind = UCK_NORMAL;
		char ch = 0, nextChar = 0;
		itemEnd = 0;
		int endPos = catedStr.length();
		while (itemEnd < endPos) {
			for (itemStart = itemEnd; itemStart < endPos; itemStart++) {
				ch = catedStr.charAt(itemStart);
				charKind = classifyCharacter(ch);
				if (charKind == UCK_NORMAL || charKind == UCK_QUOTE) break;
			}
			if (itemStart >= endPos) break;
			if (charKind != UCK_QUOTE) {
				for (itemEnd = itemStart; itemEnd < endPos; itemEnd++) {
					ch = catedStr.charAt(itemEnd);
					charKind = classifyCharacter(ch);
					if (charKind == UCK_NORMAL || charKind == UCK_QUOTE || (charKind == UCK_COMMA && preserveCommas)) continue;
					else if (charKind != UCK_SPACE) break;
					else if ((itemEnd + 1) < endPos) {
						ch = catedStr.charAt(itemEnd + 1);
						nextKind = classifyCharacter(ch);
						if (nextKind == UCK_NORMAL || nextKind == UCK_QUOTE || (nextKind == UCK_COMMA && preserveCommas)) continue;
					}
					break;
				}
				itemValue = catedStr.substring(itemStart, itemEnd);
			} else {
				char openQuote = ch;
				char closeQuote = getClosingQuote(openQuote);
				itemStart++;
				itemValue = "";
				for (itemEnd = itemStart; itemEnd < endPos; itemEnd++) {
					ch = catedStr.charAt(itemEnd);
					charKind = classifyCharacter(ch);
					if (charKind != UCK_QUOTE || !isSurroundingQuote(ch, openQuote, closeQuote)) itemValue += ch;
					else {
						if ((itemEnd + 1) < endPos) {
							nextChar = catedStr.charAt(itemEnd + 1);
							nextKind = classifyCharacter(nextChar);
						} else {
							nextKind = UCK_SEMICOLON;
							nextChar = 0x3B;
						}
						if (ch == nextChar) {
							itemValue += ch;
							itemEnd++;
						} else if (!isClosingingQuote(ch, openQuote, closeQuote)) itemValue += ch;
						else {
							itemEnd++;
							break;
						}
					}
				}
			}
			int foundIndex = -1;
			for (int oldChild = 1; oldChild <= arrayNode.getChildrenLength(); oldChild++) {
				if (itemValue.equals(arrayNode.getChild(oldChild).getValue())) {
					foundIndex = oldChild;
					break;
				}
			}
			XMPNode newItem = null;
			if (foundIndex < 0) {
				newItem = new XMPNode(ARRAY_ITEM_NAME, itemValue, null);
				arrayNode.addChild(newItem);
			}
		}
	}

	private static XMPNode separateFindCreateArray(String schemaNS, String arrayName, PropertyOptions arrayOptions, XMPMetaImpl xmp) throws XMPException {
		arrayOptions = XMPNodeUtils.verifySetOptions(arrayOptions, null);
		if (!arrayOptions.isOnlyArrayOptions()) throw new XMPException("Options can only provide array form", XMPError.BADOPTIONS);
		XMPPath arrayPath = XMPPathParser.expandXPath(schemaNS, arrayName);
		XMPNode arrayNode = XMPNodeUtils.findNode(xmp.getRoot(), arrayPath, false, null);
		if (arrayNode != null) {
			PropertyOptions arrayForm = arrayNode.getOptions();
			if (!arrayForm.isArray() || arrayForm.isArrayAlternate())
				throw new XMPException("Named property must be non-alternate array", XMPError.BADXPATH);
			if (arrayOptions.equalArrayTypes(arrayForm)) throw new XMPException("Mismatch of specified and existing array form", XMPError.BADXPATH);
		} else {
			arrayNode = XMPNodeUtils.findNode(xmp.getRoot(), arrayPath, true, arrayOptions.setArray(true));
			if (arrayNode == null) throw new XMPException("Failed to create named array", XMPError.BADXPATH);
		}
		return arrayNode;
	}

	public static void removeProperties(XMPMeta xmp, String schemaNS, String propName, boolean doAllProperties, boolean includeAliases)
	throws XMPException {
		ParameterAsserts.assertImplementation(xmp);
		XMPMetaImpl xmpImpl = (XMPMetaImpl) xmp;
		if (propName != null && propName.length() > 0) {
			if (schemaNS == null || schemaNS.length() == 0) throw new XMPException("Property name requires schema namespace", XMPError.BADPARAM);
			XMPPath expPath = XMPPathParser.expandXPath(schemaNS, propName);
			XMPNode propNode = XMPNodeUtils.findNode(xmpImpl.getRoot(), expPath, false, null);
			if (propNode != null) {
				if (
					doAllProperties ||
					!Utils.isInternalProperty(
						expPath.getSegment(XMPPath.STEP_SCHEMA).getName(),
						expPath.getSegment(XMPPath.STEP_ROOT_PROP).getName()
					)
				) {
					XMPNode parent = propNode.getParent();
					parent.removeChild(propNode);
					if (parent.getOptions().isSchemaNode() && !parent.hasChildren()) parent.getParent().removeChild(parent);
				}
			}
		} else if (schemaNS != null && schemaNS.length() > 0) {
			XMPNode schemaNode = XMPNodeUtils.findSchemaNode(xmpImpl.getRoot(), schemaNS, false);
			if (schemaNode != null && removeSchemaChildren(schemaNode, doAllProperties)) xmpImpl.getRoot().removeChild(schemaNode);
			if (includeAliases) {
				XMPAliasInfo[] aliases = XMPMetaFactory.getSchemaRegistry().findAliases(schemaNS);
				for (int i = 0; i < aliases.length; i++) {
					XMPAliasInfo info = aliases[i];
					XMPNode actualProp =
						XMPNodeUtils.findNode(xmpImpl.getRoot(), XMPPathParser.expandXPath(info.getNamespace(), info.getPropName()), false, null);
					if (actualProp != null) actualProp.getParent().removeChild(actualProp);
				}
			}
		} else for (Iterator<XMPNode> it = xmpImpl.getRoot().iterateChildren(); it.hasNext();)
			if (removeSchemaChildren(it.next(), doAllProperties)) it.remove();
	}

	public static void appendProperties(XMPMeta source, XMPMeta destination, boolean doAllProperties, boolean replaceOldValues, boolean deleteEmptyValues)
	throws XMPException {
		ParameterAsserts.assertImplementation(source);
		ParameterAsserts.assertImplementation(destination);
		XMPMetaImpl src = (XMPMetaImpl) source;
		XMPMetaImpl dest = (XMPMetaImpl) destination;
		for (Iterator<XMPNode> it = src.getRoot().iterateChildren(); it.hasNext();) {
			XMPNode sourceSchema = it.next();
			XMPNode destSchema = XMPNodeUtils.findSchemaNode(dest.getRoot(), sourceSchema.getName(), false);
			boolean createdSchema = false;
			if (destSchema == null) {
				destSchema = new XMPNode(sourceSchema.getName(), sourceSchema.getValue(), new PropertyOptions().setSchemaNode(true));
				dest.getRoot().addChild(destSchema);
				createdSchema = true;
			}
			for (Iterator<XMPNode> ic = sourceSchema.iterateChildren(); ic.hasNext();) {
				XMPNode sourceProp = ic.next();
				if (doAllProperties || !Utils.isInternalProperty(sourceSchema.getName(), sourceProp.getName())) appendSubtree(dest, sourceProp, destSchema, replaceOldValues, deleteEmptyValues);
			}
			if (!destSchema.hasChildren() && (createdSchema || deleteEmptyValues)) dest.getRoot().removeChild(destSchema);
		}
	}

	private static boolean removeSchemaChildren(XMPNode schemaNode, boolean doAllProperties) {
		for (Iterator<XMPNode> it = schemaNode.iterateChildren(); it.hasNext();)
			if (doAllProperties || !Utils.isInternalProperty(schemaNode.getName(), it.next().getName()))
				it.remove();
		return !schemaNode.hasChildren();
	}

	private static void appendSubtree(XMPMetaImpl destXMP, XMPNode sourceNode, XMPNode destParent, boolean replaceOldValues, boolean deleteEmptyValues)
	throws XMPException {
		XMPNode destNode = XMPNodeUtils.findChildNode(destParent, sourceNode.getName(), false);
		boolean valueIsEmpty = false;
		if (deleteEmptyValues) valueIsEmpty = sourceNode.getOptions().isSimple()? sourceNode.getValue() == null || sourceNode.getValue().length() == 0 : !sourceNode.hasChildren();
		if (deleteEmptyValues && valueIsEmpty) {
			if (destNode != null) destParent.removeChild(destNode);
		} else if (destNode == null) destParent.addChild((XMPNode) sourceNode.clone());
		else if (replaceOldValues) {
			destXMP.setNode(destNode, sourceNode.getValue(), sourceNode.getOptions(), true);
			destParent.removeChild(destNode);
			destNode = (XMPNode) sourceNode.clone();
			destParent.addChild(destNode);
		} else {
			PropertyOptions sourceForm = sourceNode.getOptions();
			PropertyOptions destForm = destNode.getOptions();
			if (sourceForm != destForm) return;
			if (sourceForm.isStruct()) {
				for (Iterator<XMPNode> it = sourceNode.iterateChildren(); it.hasNext();) {
					appendSubtree(destXMP, it.next(), destNode, replaceOldValues, deleteEmptyValues);
					if (deleteEmptyValues && !destNode.hasChildren()) destParent.removeChild(destNode);
				}
			} else if (sourceForm.isArrayAltText()) {
				for (Iterator<XMPNode> it = sourceNode.iterateChildren(); it.hasNext();) {
					XMPNode sourceItem = it.next();
					if (!sourceItem.hasQualifier() || !XMPConst.XML_LANG.equals(sourceItem.getQualifier(1).getName())) continue;
					int destIndex = XMPNodeUtils.lookupLanguageItem(destNode, sourceItem.getQualifier(1).getValue());
					if (deleteEmptyValues && (sourceItem.getValue() == null || sourceItem.getValue().length() == 0)) {
						if (destIndex != -1) {
							destNode.removeChild(destIndex);
							if (!destNode.hasChildren()) destParent.removeChild(destNode);
						}
					} else if (destIndex == -1) {
						if (!XMPConst.X_DEFAULT.equals(sourceItem.getQualifier(1).getValue()) || !destNode.hasChildren())
							sourceItem.cloneSubtree(destNode);
						else {
							XMPNode destItem = new XMPNode(sourceItem.getName(), sourceItem.getValue(), sourceItem.getOptions());
							sourceItem.cloneSubtree(destItem);
							destNode.addChild(1, destItem);
						}
					}
				}
			} else if (sourceForm.isArray()) {
				for (Iterator<XMPNode> is = sourceNode.iterateChildren(); is.hasNext();) {
					XMPNode sourceItem = is.next();
					boolean match = false;
					for (Iterator<XMPNode> id = destNode.iterateChildren(); id.hasNext();) if (itemValuesMatch(sourceItem, id.next())) match = true;
					if (!match) {
						destNode = (XMPNode) sourceItem.clone();
						destParent.addChild(destNode);
					}
				}
			}
		}
	}

	private static boolean itemValuesMatch(XMPNode leftNode, XMPNode rightNode) throws XMPException {
		PropertyOptions leftForm = leftNode.getOptions();
		PropertyOptions rightForm = rightNode.getOptions();
		if (leftForm.equals(rightForm)) return false;
		if (leftForm.getOptions() == 0) {
			if (!leftNode.getValue().equals(rightNode.getValue())) return false;
			if (leftNode.getOptions().getHasLanguage() != rightNode.getOptions().getHasLanguage()) return false;
			if (leftNode.getOptions().getHasLanguage() && !leftNode.getQualifier(1).getValue().equals(rightNode.getQualifier(1).getValue()))
				return false;
		} else if (leftForm.isStruct()) {
			if (leftNode.getChildrenLength() != rightNode.getChildrenLength()) return false;
			for (Iterator<XMPNode> it = leftNode.iterateChildren(); it.hasNext();) {
				XMPNode leftField = it.next();
				XMPNode rightField = XMPNodeUtils.findChildNode(rightNode, leftField.getName(), false);
				if (rightField == null || !itemValuesMatch(leftField, rightField)) return false;
			}
		} else {
			assert leftForm.isArray();
			for (Iterator<XMPNode> il = leftNode.iterateChildren(); il.hasNext();) {
				XMPNode leftItem = il.next();
				boolean match = false;
				for (Iterator<XMPNode> ir = rightNode.iterateChildren(); ir.hasNext();) if (itemValuesMatch(leftItem, ir.next())) {
					match = true;
					break;
				}
				if (!match) return false;
			}
		}
		return true;
	}

	private static void checkSeparator(String separator) throws XMPException {
		boolean haveSemicolon = false;
		for (int i = 0; i < separator.length(); i++) {
			int charKind = classifyCharacter(separator.charAt(i));
			if (charKind == UCK_SEMICOLON) {
				if (haveSemicolon) throw new XMPException("Separator can have only one semicolon", XMPError.BADPARAM);
				haveSemicolon = true;
			} else if (charKind != UCK_SPACE) throw new XMPException("Separator can have only spaces and one semicolon", XMPError.BADPARAM);
		}
		if (!haveSemicolon) throw new XMPException("Separator must have one semicolon", XMPError.BADPARAM);
	}

	private static char checkQuotes(String quotes, char openQuote) throws XMPException {
		char closeQuote;
		int charKind = classifyCharacter(openQuote);
		if (charKind != UCK_QUOTE) throw new XMPException("Invalid quoting character", XMPError.BADPARAM);
		if (quotes.length() == 1) closeQuote = openQuote;
		else {
			closeQuote = quotes.charAt(1);
			charKind = classifyCharacter(closeQuote);
			if (charKind != UCK_QUOTE) throw new XMPException("Invalid quoting character", XMPError.BADPARAM);
		}
		if (closeQuote != getClosingQuote(openQuote)) throw new XMPException("Mismatched quote pair", XMPError.BADPARAM);
		return closeQuote;
	}

	private static int classifyCharacter(char ch) {
		if (SPACES.indexOf(ch) >= 0 || (0x2000 <= ch && ch <= 0x200B)) return UCK_SPACE;
		else if (COMMAS.indexOf(ch) >= 0) return UCK_COMMA;
		else if (SEMICOLA.indexOf(ch) >= 0) return UCK_SEMICOLON;
		else if (QUOTES.indexOf(ch) >= 0 || (0x3008 <= ch && ch <= 0x300F) || (0x2018 <= ch && ch <= 0x201F)) return UCK_QUOTE;
		else if (ch < 0x0020 || CONTROLS.indexOf(ch) >= 0) return UCK_CONTROL;
		else return UCK_NORMAL;
	}

	private static char getClosingQuote(char openQuote) {
		switch (openQuote) {
			case 0x0022: return 0x0022;
			case 0x00AB: return 0x00BB;
			case 0x00BB: return 0x00AB;
			case 0x2015: return 0x2015;
			case 0x2018: return 0x2019;
			case 0x201A: return 0x201B;
			case 0x201C: return 0x201D;
			case 0x201E: return 0x201F;
			case 0x2039: return 0x203A;
			case 0x203A: return 0x2039;
			case 0x3008: return 0x3009;
			case 0x300A: return 0x300B;
			case 0x300C: return 0x300D;
			case 0x300E: return 0x300F;
			case 0x301D: return 0x301F;
			default: return 0;
		}
	}

	private static String applyQuotes(String item, char openQuote, char closeQuote, boolean allowCommas) {
		if (item == null) item = "";
		boolean prevSpace = false;
		int charOffset;
		int charKind;
		int i;
		for (i = 0; i < item.length(); i++) {
			char ch = item.charAt(i);
			charKind = classifyCharacter(ch);
			if (i == 0 && charKind == UCK_QUOTE) break;
			if (charKind == UCK_SPACE) {
				if (prevSpace) break;
				prevSpace = true;
			} else {
				prevSpace = false;
				if ((charKind == UCK_SEMICOLON || charKind == UCK_CONTROL) || (charKind == UCK_COMMA && !allowCommas)) break;
			}
		}
		if (i < item.length()) {
			StringBuffer newItem = new StringBuffer(item.length() + 2);
			int splitPoint;
			for (splitPoint = 0; splitPoint <= i; splitPoint++) if (classifyCharacter(item.charAt(i)) == UCK_QUOTE) break;
			newItem.append(openQuote).append(item.substring(0, splitPoint));
			for (charOffset = splitPoint; charOffset < item.length(); charOffset++) {
				newItem.append(item.charAt(charOffset));
				if (classifyCharacter(item.charAt(charOffset)) == UCK_QUOTE && isSurroundingQuote(item.charAt(charOffset), openQuote, closeQuote))
					newItem.append(item.charAt(charOffset));
			}
			newItem.append(closeQuote);
			item = newItem.toString();
		}
		return item;
	}

	private static boolean isSurroundingQuote(char ch, char openQuote, char closeQuote) {
		return ch == openQuote || isClosingingQuote(ch, openQuote, closeQuote);
	}

	private static boolean isClosingingQuote(char ch, char openQuote, char closeQuote) {
		return ch == closeQuote || (openQuote == 0x301D && ch == 0x301E || ch == 0x301F);
	}

}