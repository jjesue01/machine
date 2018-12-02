// =================================================================================================
// ADOBE SYSTEMS INCORPORATED
// Copyright 2006 Adobe Systems Incorporated
// All Rights Reserved
//
// NOTICE:  Adobe permits you to use, modify, and distribute this file in accordance with the terms
// of the Adobe license agreement accompanying it.
// =================================================================================================

package com.github.gasrios.xmp.impl;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import com.github.gasrios.xmp.XMPException;
import com.github.gasrios.xmp.options.SerializeOptions;

public class XMPSerializerHelper {

	public static void serialize(XMPMetaImpl xmp, OutputStream out, SerializeOptions options) throws XMPException {
		if (options == null) options = new SerializeOptions();
		if (options.getSort()) xmp.sort();
		new XMPSerializerRDF().serialize(xmp, out, options);
	}

	public static String serializeToString(XMPMetaImpl xmp, SerializeOptions options) throws XMPException {
		options = options != null ? options : new SerializeOptions();
		options.setEncodeUTF16BE(true);
		ByteArrayOutputStream out = new ByteArrayOutputStream(2048);
		serialize(xmp, out, options);
		try {
			return out.toString(options.getEncoding());
		} catch (UnsupportedEncodingException e) {
			return out.toString();
		}
	}

	public static byte[] serializeToBuffer(XMPMetaImpl xmp, SerializeOptions options) throws XMPException {
		ByteArrayOutputStream out = new ByteArrayOutputStream(2048);
		serialize(xmp, out, options);
		return out.toByteArray();
	}

}