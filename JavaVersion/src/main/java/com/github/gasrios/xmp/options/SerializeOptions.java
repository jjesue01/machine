// =================================================================================================
// ADOBE SYSTEMS INCORPORATED
// Copyright 2006 Adobe Systems Incorporated
// All Rights Reserved
//
// NOTICE:  Adobe permits you to use, modify, and distribute this file in accordance with the terms
// of the Adobe license agreement accompanying it.
// =================================================================================================

package com.github.gasrios.xmp.options;

import com.github.gasrios.xmp.XMPException;

public final class SerializeOptions extends Options {

	public static final int OMIT_PACKET_WRAPPER = 0x0010;

	public static final int READONLY_PACKET = 0x0020;

	public static final int USE_COMPACT_FORMAT = 0x0040;

	public static final int USE_CANONICAL_FORMAT = 0x0080;

	public static final int INCLUDE_THUMBNAIL_PAD = 0x0100;

	public static final int EXACT_PACKET_LENGTH = 0x0200;

	public static final int OMIT_XMPMETA_ELEMENT = 0x1000;

	public static final int SORT = 0x2000;

	private static final int LITTLEENDIAN_BIT = 0x0001;

	private static final int UTF16_BIT = 0x0002;

	public static final int ENCODE_UTF8 = 0;

	public static final int ENCODE_UTF16BE = UTF16_BIT;

	public static final int ENCODE_UTF16LE = UTF16_BIT | LITTLEENDIAN_BIT;

	private static final int ENCODING_MASK = UTF16_BIT | LITTLEENDIAN_BIT;

	private int padding = 2048;

	private String newline = "\n";

	private String indent = "  ";

	private int baseIndent = 0;

	private boolean omitVersionAttribute = false;

	public SerializeOptions() {}

	public SerializeOptions(int options) throws XMPException {
		super(options);
	}

	public boolean getOmitPacketWrapper() {
		return getOption(OMIT_PACKET_WRAPPER);
	}

	public SerializeOptions setOmitPacketWrapper(boolean value) {
		setOption(OMIT_PACKET_WRAPPER, value);
		return this;
	}

	public boolean getOmitXmpMetaElement() {
		return getOption(OMIT_XMPMETA_ELEMENT);
	}

	public SerializeOptions setOmitXmpMetaElement(boolean value) {
		setOption(OMIT_XMPMETA_ELEMENT, value);
		return this;
	}

	public boolean getReadOnlyPacket() {
		return getOption(READONLY_PACKET);
	}

	public SerializeOptions setReadOnlyPacket(boolean value) {
		setOption(READONLY_PACKET, value);
		return this;
	}

	public boolean getUseCompactFormat() {
		return getOption(USE_COMPACT_FORMAT);
	}

	public SerializeOptions setUseCompactFormat(boolean value) {
		setOption(USE_COMPACT_FORMAT, value);
		return this;
	}

	public boolean getUseCanonicalFormat() {
		return getOption(USE_CANONICAL_FORMAT);
	}

	public SerializeOptions setUseCanonicalFormat(boolean value) {
		setOption(USE_CANONICAL_FORMAT, value);
		return this;
	}

	public boolean getIncludeThumbnailPad() {
		return getOption(INCLUDE_THUMBNAIL_PAD);
	}

	public SerializeOptions setIncludeThumbnailPad(boolean value) {
		setOption(INCLUDE_THUMBNAIL_PAD, value);
		return this;
	}

	public boolean getExactPacketLength() {
		return getOption(EXACT_PACKET_LENGTH);
	}

	public SerializeOptions setExactPacketLength(boolean value) {
		setOption(EXACT_PACKET_LENGTH, value);
		return this;
	}

	public boolean getSort() {
		return getOption(SORT);
	}

	public SerializeOptions setSort(boolean value) {
		setOption(SORT, value);
		return this;
	}

	public boolean getEncodeUTF16BE() {
		return (getOptions() & ENCODING_MASK) == ENCODE_UTF16BE;
	}

	public SerializeOptions setEncodeUTF16BE(boolean value) {
		setOption(UTF16_BIT | LITTLEENDIAN_BIT, false);
		setOption(ENCODE_UTF16BE, value);
		return this;
	}

	public boolean getEncodeUTF16LE() {
		return (getOptions() & ENCODING_MASK) == ENCODE_UTF16LE;
	}

	public SerializeOptions setEncodeUTF16LE(boolean value) {
		setOption(UTF16_BIT | LITTLEENDIAN_BIT, false);
		setOption(ENCODE_UTF16LE, value);
		return this;
	}

	public int getBaseIndent() {
		return baseIndent;
	}

	public SerializeOptions setBaseIndent(int baseIndent) {
		this.baseIndent = baseIndent;
		return this;
	}

	public String getIndent() {
		return indent;
	}

	public SerializeOptions setIndent(String indent) {
		this.indent = indent;
		return this;
	}

	public String getNewline() {
		return newline;
	}

	public SerializeOptions setNewline(String newline) {
		this.newline = newline;
		return this;
	}

	public int getPadding() {
		return padding;
	}

	public SerializeOptions setPadding(int padding) {
		this.padding = padding;
		return this;
	}

	public boolean getOmitVersionAttribute() {
		return omitVersionAttribute;
	}

	public String getEncoding() {
		if (getEncodeUTF16BE()) {
			return "UTF-16BE";
		} else if (getEncodeUTF16LE()) {
			return "UTF-16LE";
		} else {
			return "UTF-8";
		}
	}

	public Object clone() throws CloneNotSupportedException {
		SerializeOptions clone = new SerializeOptions(getOptions());
		clone.setBaseIndent(baseIndent);
		clone.setIndent(indent);
		clone.setNewline(newline);
		clone.setPadding(padding);
		return clone;
	}

	protected String defineOptionName(int option) {
		switch (option) {
		case OMIT_PACKET_WRAPPER:
			return "OMIT_PACKET_WRAPPER";
		case READONLY_PACKET:
			return "READONLY_PACKET";
		case USE_COMPACT_FORMAT:
			return "USE_COMPACT_FORMAT";
		case INCLUDE_THUMBNAIL_PAD:
			return "INCLUDE_THUMBNAIL_PAD";
		case EXACT_PACKET_LENGTH:
			return "EXACT_PACKET_LENGTH";
		case OMIT_XMPMETA_ELEMENT:
			return "OMIT_XMPMETA_ELEMENT";
		case SORT:
			return "NORMALIZED";
		default:
			return null;
		}
	}

	protected int getValidOptions() {
		return OMIT_PACKET_WRAPPER | READONLY_PACKET | USE_COMPACT_FORMAT | INCLUDE_THUMBNAIL_PAD | OMIT_XMPMETA_ELEMENT | EXACT_PACKET_LENGTH | SORT;
	}

}