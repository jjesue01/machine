// =================================================================================================
// ADOBE SYSTEMS INCORPORATED
// Copyright 2006 Adobe Systems Incorporated
// All Rights Reserved
//
// NOTICE:  Adobe permits you to use, modify, and distribute this file in accordance with the terms
// of the Adobe license agreement accompanying it.
// =================================================================================================

package com.github.gasrios.xmp;

import com.github.gasrios.xmp.impl.Base64;
import com.github.gasrios.xmp.impl.ISO8601Converter;
import com.github.gasrios.xmp.impl.XMPUtilsImpl;
import com.github.gasrios.xmp.options.PropertyOptions;

public class XMPUtils {

	private XMPUtils() {}

	public static String catenateArrayItems(XMPMeta xmp, String schemaNS, String arrayName, String separator, String quotes, boolean allowCommas)
	throws XMPException {
		return XMPUtilsImpl.catenateArrayItems(xmp, schemaNS, arrayName, separator, quotes, allowCommas);
	}

	public static void separateArrayItems(
		XMPMeta xmp,
		String schemaNS,
		String arrayName,
		String catedStr,
		PropertyOptions arrayOptions,
		boolean preserveCommas)
	throws XMPException {
		XMPUtilsImpl.separateArrayItems(xmp, schemaNS, arrayName, catedStr, arrayOptions, preserveCommas);
	}

	public static void removeProperties(XMPMeta xmp, String schemaNS, String propName, boolean doAllProperties, boolean includeAliases)
	throws XMPException {
		XMPUtilsImpl.removeProperties(xmp, schemaNS, propName, doAllProperties, includeAliases);
	}

	public static void appendProperties(XMPMeta source, XMPMeta dest, boolean doAllProperties, boolean replaceOldValues) throws XMPException {
		appendProperties(source, dest, doAllProperties, replaceOldValues, false);
	}

	public static void appendProperties(XMPMeta source, XMPMeta dest, boolean doAllProperties, boolean replaceOldValues, boolean deleteEmptyValues)
	throws XMPException {
		XMPUtilsImpl.appendProperties(source, dest, doAllProperties, replaceOldValues, deleteEmptyValues);
	}

	public static boolean convertToBoolean(String value) throws XMPException {
		if (value == null || value.length() == 0) throw new XMPException("Empty convert-string", XMPError.BADVALUE);
		value = value.toLowerCase();
		try {
			return Integer.parseInt(value) != 0;
		} catch (NumberFormatException e) {
			return "true".equals(value) || "t".equals(value) || "on".equals(value) || "yes".equals(value);
		}
	}

	public static String convertFromBoolean(boolean value) {
		return value ? XMPConst.TRUESTR : XMPConst.FALSESTR;
	}

	public static int convertToInteger(String rawValue) throws XMPException {
		try {
			if (rawValue == null || rawValue.length() == 0) throw new XMPException("Empty convert-string", XMPError.BADVALUE);
			if (rawValue.startsWith("0x")) return Integer.parseInt(rawValue.substring(2), 16);
			else return Integer.parseInt(rawValue);
		} catch (NumberFormatException e) {
			throw new XMPException("Invalid integer string", XMPError.BADVALUE);
		}
	}

	public static String convertFromInteger(int value) {
		return String.valueOf(value);
	}

	public static long convertToLong(String rawValue) throws XMPException {
		try {
			if (rawValue == null || rawValue.length() == 0) throw new XMPException("Empty convert-string", XMPError.BADVALUE);
			if (rawValue.startsWith("0x")) return Long.parseLong(rawValue.substring(2), 16);
			else return Long.parseLong(rawValue);
		} catch (NumberFormatException e) {
			throw new XMPException("Invalid long string", XMPError.BADVALUE);
		}
	}

	public static String convertFromLong(long value) {
		return String.valueOf(value);
	}

	public static double convertToDouble(String rawValue) throws XMPException {
		try {
			if (rawValue == null || rawValue.length() == 0) throw new XMPException("Empty convert-string", XMPError.BADVALUE);
			else return Double.parseDouble(rawValue);
		} catch (NumberFormatException e) {
			throw new XMPException("Invalid double string", XMPError.BADVALUE);
		}
	}

	public static String convertFromDouble(double value) {
		return String.valueOf(value);
	}

	public static XMPDateTime convertToDate(String rawValue) throws XMPException {
		if (rawValue == null || rawValue.length() == 0) throw new XMPException("Empty convert-string", XMPError.BADVALUE);
		else return ISO8601Converter.parse(rawValue);
	}

	public static String convertFromDate(XMPDateTime value) {
		return ISO8601Converter.render(value);
	}

	public static String encodeBase64(byte[] buffer) {
		return new String(Base64.encode(buffer));
	}

	public static byte[] decodeBase64(String base64String) throws XMPException {
		try {
			return Base64.decode(base64String.getBytes());
		} catch (Throwable e) {
			throw new XMPException("Invalid base64 string", XMPError.BADVALUE, e);
		}
	}

}