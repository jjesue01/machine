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
import com.github.gasrios.xmp.XMPError;
import com.github.gasrios.xmp.XMPException;
import com.github.gasrios.xmp.XMPMeta;

class ParameterAsserts implements XMPConst {

	private ParameterAsserts() {}

	public static void assertArrayName(String arrayName) throws XMPException {
		if (arrayName == null || arrayName.length() == 0) throw new XMPException("Empty array name", XMPError.BADPARAM);
	}

	public static void assertPropName(String propName) throws XMPException {
		if (propName == null || propName.length() == 0) throw new XMPException("Empty property name", XMPError.BADPARAM);
	}

	public static void assertSchemaNS(String schemaNS) throws XMPException {
		if (schemaNS == null || schemaNS.length() == 0) throw new XMPException("Empty schema namespace URI", XMPError.BADPARAM);
	}

	public static void assertPrefix(String prefix) throws XMPException {
		if (prefix == null || prefix.length() == 0) throw new XMPException("Empty prefix", XMPError.BADPARAM);
	}

	public static void assertSpecificLang(String specificLang) throws XMPException {
		if (specificLang == null || specificLang.length() == 0) throw new XMPException("Empty specific language", XMPError.BADPARAM);
	}

	public static void assertStructName(String structName) throws XMPException {
		if (structName == null || structName.length() == 0) throw new XMPException("Empty array name", XMPError.BADPARAM);
	}

	public static void assertNotNull(Object param) throws XMPException {
		if (param == null) throw new XMPException("Parameter must not be null", XMPError.BADPARAM);
		else if ((param instanceof String) && ((String) param).length() == 0)
			throw new XMPException("Parameter must not be null or empty", XMPError.BADPARAM);
	}

	public static void assertImplementation(XMPMeta xmp) throws XMPException {
		if (xmp == null) throw new XMPException("Parameter must not be null", XMPError.BADPARAM);
		else if (!(xmp instanceof XMPMetaImpl))
			throw new XMPException("The XMPMeta-object is not compatible with this implementation", XMPError.BADPARAM);
	}

}