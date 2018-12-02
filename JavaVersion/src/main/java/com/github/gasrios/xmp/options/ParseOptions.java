// =================================================================================================
// ADOBE SYSTEMS INCORPORATED
// Copyright 2006 Adobe Systems Incorporated
// All Rights Reserved
//
// NOTICE:  Adobe permits you to use, modify, and distribute this file in accordance with the terms
// of the Adobe license agreement accompanying it.
// =================================================================================================

package com.github.gasrios.xmp.options;

public final class ParseOptions extends Options {

	public static final int REQUIRE_XMP_META = 0x0001;

	public static final int STRICT_ALIASING = 0x0004;

	public static final int FIX_CONTROL_CHARS = 0x0008;

	public static final int ACCEPT_LATIN_1 = 0x0010;

	public static final int OMIT_NORMALIZATION = 0x0020;

	public static final int DISALLOW_DOCTYPE = 0x0040;

	public ParseOptions() {
		setOption(FIX_CONTROL_CHARS | ACCEPT_LATIN_1 | DISALLOW_DOCTYPE, true);
	}

	public boolean getRequireXMPMeta() {
		return getOption(REQUIRE_XMP_META);
	}

	public ParseOptions setRequireXMPMeta(boolean value) {
		setOption(REQUIRE_XMP_META, value);
		return this;
	}

	public boolean getStrictAliasing() {
		return getOption(STRICT_ALIASING);
	}

	public ParseOptions setStrictAliasing(boolean value) {
		setOption(STRICT_ALIASING, value);
		return this;
	}

	public boolean getFixControlChars() {
		return getOption(FIX_CONTROL_CHARS);
	}

	public ParseOptions setFixControlChars(boolean value) {
		setOption(FIX_CONTROL_CHARS, value);
		return this;
	}

	public boolean getAcceptLatin1() {
		return getOption(ACCEPT_LATIN_1);
	}

	public ParseOptions setOmitNormalization(boolean value) {
		setOption(OMIT_NORMALIZATION, value);
		return this;
	}

	public boolean getOmitNormalization() {
		return getOption(OMIT_NORMALIZATION);
	}

	public ParseOptions setDisallowDoctype(boolean value) {
		setOption(DISALLOW_DOCTYPE, value);
		return this;
	}

	public boolean getDisallowDoctype() {

		return getOption(DISALLOW_DOCTYPE);
	}

	public ParseOptions setAcceptLatin1(boolean value) {
		setOption(ACCEPT_LATIN_1, value);
		return this;
	}

	protected String defineOptionName(int option) {
		switch (option) {
		case REQUIRE_XMP_META:
			return "REQUIRE_XMP_META";
		case STRICT_ALIASING:
			return "STRICT_ALIASING";
		case FIX_CONTROL_CHARS:
			return "FIX_CONTROL_CHARS";
		case ACCEPT_LATIN_1:
			return "ACCEPT_LATIN_1";
		case OMIT_NORMALIZATION:
			return "OMIT_NORMALIZATION";
		case DISALLOW_DOCTYPE:
			return "DISALLOW_DOCTYPE";
		default:
			return null;
		}
	}

	protected int getValidOptions() {
		return REQUIRE_XMP_META | STRICT_ALIASING | FIX_CONTROL_CHARS | ACCEPT_LATIN_1 | OMIT_NORMALIZATION | DISALLOW_DOCTYPE;
	}

}