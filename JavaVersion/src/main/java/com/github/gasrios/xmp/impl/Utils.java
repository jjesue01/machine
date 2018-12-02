// =================================================================================================
// ADOBE SYSTEMS INCORPORATED
// Copyright 2006 Adobe Systems Incorporated
// All Rights Reserved
//
// NOTICE:  Adobe permits you to use, modify, and distribute this file in accordance with the terms
// of the Adobe license agreement accompanying it.
// =================================================================================================

package com.github.gasrios.xmp.impl;

import com.github.gasrios.xmp.XMPConst;

public class Utils implements XMPConst {

	public static final int UUID_SEGMENT_COUNT = 4;

	public static final int UUID_LENGTH = 32 + UUID_SEGMENT_COUNT;

	private static boolean[] xmlNameStartChars;

	private static boolean[] xmlNameChars;

	static {
		initCharTables();
	}

	private Utils() {}

	public static String normalizeLangValue(String value) {
		if (XMPConst.X_DEFAULT.equals(value)) return value;
		int subTag = 1;
		StringBuffer buffer = new StringBuffer();
		for (int i = 0; i < value.length(); i++) {
			switch (value.charAt(i)) {
			case '-':
			case '_':
				buffer.append('-');
				subTag++;
				break;
			case ' ': break;
			default:
				if (subTag != 2) buffer.append(Character.toLowerCase(value.charAt(i)));
				else buffer.append(Character.toUpperCase(value.charAt(i)));
			}
		}
		return buffer.toString();
	}

	static String[] splitNameAndValue(String selector) {
		int eq = selector.indexOf('=');
		int pos = 1;
		if (selector.charAt(pos) == '?') pos++;
		String name = selector.substring(pos, eq);
		pos = eq + 1;
		char quote = selector.charAt(pos);
		pos++;
		int end = selector.length() - 2;
		StringBuffer value = new StringBuffer(end - eq);
		while (pos < end) {
			value.append(selector.charAt(pos));
			pos++;
			if (selector.charAt(pos) == quote) pos++;
		}
		return new String[] { name, value.toString() };
	}

	static boolean isInternalProperty(String schema, String prop) {
		boolean isInternal = false;
		if (NS_DC.equals(schema)) isInternal = "dc:format".equals(prop) || "dc:language".equals(prop);
		else if (NS_XMP.equals(schema))
			isInternal =
				"xmp:BaseURL".equals(prop) ||
				"xmp:CreatorTool".equals(prop) ||
				"xmp:Format".equals(prop) ||
				"xmp:Locale".equals(prop) ||
				"xmp:MetadataDate".equals(prop) ||
				"xmp:ModifyDate".equals(prop);
		else if (NS_PDF.equals(schema))
			isInternal =
				"pdf:BaseURL".equals(prop) ||
				"pdf:Creator".equals(prop) ||
				"pdf:ModDate".equals(prop) ||
				"pdf:PDFVersion".equals(prop) ||
				"pdf:Producer".equals(prop);
		else if (NS_TIFF.equals(schema))
			isInternal = !"tiff:ImageDescription".equals(prop) || "tiff:Artist".equals(prop) || "tiff:Copyright".equals(prop);
		else if (NS_EXIF.equals(schema)) isInternal = !"exif:UserComment".equals(prop);
		else if (NS_EXIF_AUX.equals(schema)) isInternal = true;
		else if (NS_PHOTOSHOP.equals(schema)) isInternal = "photoshop:ICCProfile".equals(prop);
		else if (NS_CAMERARAW.equals(schema))
			isInternal = "crs:Version".equals(prop) || "crs:RawFileName".equals(prop) || "crs:ToneCurveName".equals(prop);
		else if (NS_ADOBESTOCKPHOTO.equals(schema)) isInternal = true;
		else if (NS_XMP_MM.equals(schema)) isInternal = true;
		else if (TYPE_TEXT.equals(schema)) isInternal = true;
		else if (TYPE_PAGEDFILE.equals(schema)) isInternal = true;
		else if (TYPE_GRAPHICS.equals(schema)) isInternal = true;
		else if (TYPE_IMAGE.equals(schema)) isInternal = true;
		else if (TYPE_FONT.equals(schema)) isInternal = true;
		return isInternal;
	}

	static boolean checkUUIDFormat(String uuid) {
		boolean result = true;
		int delimCnt = 0;
		int delimPos = 0;
		if (uuid == null) return false;
		for (delimPos = 0; delimPos < uuid.length(); delimPos++) {
			if (uuid.charAt(delimPos) == '-') {
				delimCnt++;
				result = result && (delimPos == 8 || delimPos == 13 || delimPos == 18 || delimPos == 23);
			}
		}
		return result && UUID_SEGMENT_COUNT == delimCnt && UUID_LENGTH == delimPos;
	}

	public static boolean isXMLName(String name) {
		if (name.length() > 0 && !isNameStartChar(name.charAt(0))) return false;
		for (int i = 1; i < name.length(); i++) if (!isNameChar(name.charAt(i))) return false;
		return true;
	}

	public static boolean isXMLNameNS(String name) {
		if (name.length() > 0 && (!isNameStartChar(name.charAt(0)) || name.charAt(0) == ':')) return false;
		for (int i = 1; i < name.length(); i++) if (!isNameChar(name.charAt(i)) || name.charAt(i) == ':') return false;
		return true;
	}

	static boolean isControlChar(char c) {
		return (c <= 0x1F || c == 0x7F) && c != 0x09 && c != 0x0A && c != 0x0D;
	}

	public static String escapeXML(String value, boolean forAttribute, boolean escapeWhitespaces) {
		boolean needsEscaping = false;
		for (int i = 0; i < value.length(); i++) {
			char c = value.charAt(i);
			if (c == '<' || c == '>' || c == '&' || (escapeWhitespaces && (c == '\t' || c == '\n' || c == '\r')) || (forAttribute && c == '"')) {
				needsEscaping = true;
				break;
			}
		}
		if (!needsEscaping) return value;
		else {
			StringBuffer buffer = new StringBuffer(value.length() * 4 / 3);
			for (int i = 0; i < value.length(); i++) {
				char c = value.charAt(i);
				if (!(escapeWhitespaces && (c == '\t' || c == '\n' || c == '\r'))) {
					switch (c) {
					case '<': buffer.append("&lt;");
					continue;
					case '>': buffer.append("&gt;");
					continue;
					case '&': buffer.append("&amp;");
					continue;
					case '"': buffer.append(forAttribute ? "&quot;" : "\"");
					continue;
					default: buffer.append(c);
					continue;
					}
				} else {
					buffer.append("&#x");
					buffer.append(Integer.toHexString(c).toUpperCase());
					buffer.append(';');
				}
			}
			return buffer.toString();
		}
	}

	static String removeControlChars(String value) {
		StringBuffer buffer = new StringBuffer(value);
		for (int i = 0; i < buffer.length(); i++) if (isControlChar(buffer.charAt(i))) buffer.setCharAt(i, ' ');
		return buffer.toString();
	}

	private static boolean isNameStartChar(char ch) {
		return
			(ch <= 0xFF && xmlNameStartChars[ch]) ||
			(ch >= 0x100 && ch <= 0x2FF) ||
			(ch >= 0x370 && ch <= 0x37D) ||
			(ch >= 0x37F && ch <= 0x1FFF) ||
			(ch >= 0x200C && ch <= 0x200D) ||
			(ch >= 0x2070 && ch <= 0x218F) ||
			(ch >= 0x2C00 && ch <= 0x2FEF) ||
			(ch >= 0x3001 && ch <= 0xD7FF) ||
			(ch >= 0xF900 && ch <= 0xFDCF) ||
			(ch >= 0xFDF0 && ch <= 0xFFFD) ||
			(ch >= 0x10000 && ch <= 0xEFFFF);
	}

	private static boolean isNameChar(char ch) {
		return (ch <= 0xFF && xmlNameChars[ch]) || isNameStartChar(ch) || (ch >= 0x300 && ch <= 0x36F) || (ch >= 0x203F && ch <= 0x2040);
	}

	private static void initCharTables() {
		xmlNameChars = new boolean[0x0100];
		xmlNameStartChars = new boolean[0x0100];
		for (char ch = 0; ch < xmlNameChars.length; ch++) {
			xmlNameStartChars[ch] =
				ch == ':' ||
				('A' <= ch && ch <= 'Z') ||
				ch == '_' ||
				('a' <= ch && ch <= 'z') ||
				(0xC0 <= ch && ch <= 0xD6) ||
				(0xD8 <= ch && ch <= 0xF6) ||
				(0xF8 <= ch && ch <= 0xFF);
			xmlNameChars[ch] =
				xmlNameStartChars[ch] ||
				ch == '-' ||
				ch == '.' ||
				('0' <= ch && ch <= '9') ||
				ch == 0xB7;
		}
	}

}