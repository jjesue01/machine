//=================================================================================================
//ADOBE SYSTEMS INCORPORATED
//Copyright 2006-2007 Adobe Systems Incorporated
//All Rights Reserved
//
//NOTICE:  Adobe permits you to use, modify, and distribute this file in accordance with the terms
//of the Adobe license agreement accompanying it.
//=================================================================================================

package com.github.gasrios.xmp;

import java.io.InputStream;
import java.io.OutputStream;

import com.github.gasrios.xmp.impl.XMPMetaImpl;
import com.github.gasrios.xmp.impl.XMPMetaParser;
import com.github.gasrios.xmp.impl.XMPSchemaRegistryImpl;
import com.github.gasrios.xmp.impl.XMPSerializerHelper;
import com.github.gasrios.xmp.options.ParseOptions;
import com.github.gasrios.xmp.options.SerializeOptions;

public final class XMPMetaFactory {

	private static XMPSchemaRegistry schema = new XMPSchemaRegistryImpl();

	private static XMPVersionInfo versionInfo = null;

	private XMPMetaFactory() {}

	public static XMPSchemaRegistry getSchemaRegistry() {
		return schema;
	}

	public static XMPMeta create() {
		return new XMPMetaImpl();
	}

	public static XMPMeta parse(InputStream in) throws XMPException {
		return parse(in, null);
	}

	public static XMPMeta parse(InputStream in, ParseOptions options) throws XMPException {
		return XMPMetaParser.parse(in, options);
	}

	public static XMPMeta parseFromString(String packet) throws XMPException {
		return parseFromString(packet, null);
	}

	public static XMPMeta parseFromString(String packet, ParseOptions options) throws XMPException {
		return XMPMetaParser.parse(packet, options);
	}

	public static XMPMeta parseFromBuffer(byte[] buffer) throws XMPException {
		return parseFromBuffer(buffer, null);
	}

	public static XMPMeta parseFromBuffer(byte[] buffer, ParseOptions options) throws XMPException {
		return XMPMetaParser.parse(buffer, options);
	}

	public static void serialize(XMPMeta xmp, OutputStream out) throws XMPException {
		serialize(xmp, out, null);
	}

	public static void serialize(XMPMeta xmp, OutputStream out, SerializeOptions options) throws XMPException {
		assertImplementation(xmp);
		XMPSerializerHelper.serialize((XMPMetaImpl) xmp, out, options);
	}

	public static byte[] serializeToBuffer(XMPMeta xmp, SerializeOptions options) throws XMPException {
		assertImplementation(xmp);
		return XMPSerializerHelper.serializeToBuffer((XMPMetaImpl) xmp, options);
	}

	public static String serializeToString(XMPMeta xmp, SerializeOptions options) throws XMPException {
		assertImplementation(xmp);
		return XMPSerializerHelper.serializeToString((XMPMetaImpl) xmp, options);
	}

	private static void assertImplementation(XMPMeta xmp) {
		if (!(xmp instanceof XMPMetaImpl))
			throw new UnsupportedOperationException("The serializing service works only" + "with the XMPMeta implementation of this library");
	}

	public static void reset() {
		schema = new XMPSchemaRegistryImpl();
	}

	public static synchronized XMPVersionInfo getVersionInfo() {
		if (versionInfo == null) {
			try {
				final int major = 5;
				final int minor = 1;
				final int micro = 0;
				final int engBuild = 3;
				final boolean debug = false;
				final String message = "Adobe XMP Core 5.1.0-jc003";
				versionInfo = new XMPVersionInfo() {
					public int getMajor() {
						return major;
					}
					public int getMinor() {
						return minor;
					}
					public int getMicro() {
						return micro;
					}
					public boolean isDebug() {
						return debug;
					}
					public int getBuild() {
						return engBuild;
					}
					public String getMessage() {
						return message;
					}
					public String toString() {
						return message;
					}
				};
			} catch (Throwable e) {
				System.out.println(e);
			}
		}
		return versionInfo;
	}

}