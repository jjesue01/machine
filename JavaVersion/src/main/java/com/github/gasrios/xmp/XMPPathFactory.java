// =================================================================================================
// ADOBE SYSTEMS INCORPORATED
// Copyright 2006 Adobe Systems Incorporated
// All Rights Reserved
//
// NOTICE:  Adobe permits you to use, modify, and distribute this file in accordance with the terms
// of the Adobe license agreement accompanying it.
// =================================================================================================

package com.github.gasrios.xmp;

import com.github.gasrios.xmp.impl.Utils;
import com.github.gasrios.xmp.impl.xpath.XMPPath;
import com.github.gasrios.xmp.impl.xpath.XMPPathParser;

public final class XMPPathFactory {

	private XMPPathFactory() {}

	public static String composeArrayItemPath(String arrayName, int itemIndex) throws XMPException {
		if (itemIndex > 0) return arrayName + '[' + itemIndex + ']';
		else if (itemIndex == XMPConst.ARRAY_LAST_ITEM) return arrayName + "[last()]";
		else throw new XMPException("Array index must be larger than zero", XMPError.BADINDEX);
	}

	public static String composeStructFieldPath(String fieldNS, String fieldName) throws XMPException {
		assertFieldNS(fieldNS);
		assertFieldName(fieldName);
		XMPPath fieldPath = XMPPathParser.expandXPath(fieldNS, fieldName);
		if (fieldPath.size() != 2) throw new XMPException("The field name must be simple", XMPError.BADXPATH);
		return "/" + fieldPath.getSegment(XMPPath.STEP_ROOT_PROP).getName();
	}

	public static String composeQualifierPath(String qualNS, String qualName) throws XMPException {
		assertQualNS(qualNS);
		assertQualName(qualName);
		XMPPath qualPath = XMPPathParser.expandXPath(qualNS, qualName);
		if (qualPath.size() != 2) throw new XMPException("The qualifier name must be simple", XMPError.BADXPATH);
		return "/?" + qualPath.getSegment(XMPPath.STEP_ROOT_PROP).getName();
	}

	public static String composeLangSelector(String arrayName, String langName) {
		return arrayName + "[?xml:lang=\"" + Utils.normalizeLangValue(langName) + "\"]";
	}

	public static String composeFieldSelector(String arrayName, String fieldNS, String fieldName, String fieldValue) throws XMPException {
		XMPPath fieldPath = XMPPathParser.expandXPath(fieldNS, fieldName);
		if (fieldPath.size() != 2) throw new XMPException("The fieldName name must be simple", XMPError.BADXPATH);
		return arrayName + '[' + fieldPath.getSegment(XMPPath.STEP_ROOT_PROP).getName() + "=\"" + fieldValue + "\"]";
	}

	private static void assertQualNS(String qualNS) throws XMPException {
		if (qualNS == null || qualNS.length() == 0) throw new XMPException("Empty qualifier namespace URI", XMPError.BADSCHEMA);
	}

	private static void assertQualName(String qualName) throws XMPException {
		if (qualName == null || qualName.length() == 0) throw new XMPException("Empty qualifier name", XMPError.BADXPATH);
	}

	private static void assertFieldNS(String fieldNS) throws XMPException {
		if (fieldNS == null || fieldNS.length() == 0) throw new XMPException("Empty field namespace URI", XMPError.BADSCHEMA);
	}

	private static void assertFieldName(String fieldName) throws XMPException {
		if (fieldName == null || fieldName.length() == 0) throw new XMPException("Empty f name", XMPError.BADXPATH);
	}

}