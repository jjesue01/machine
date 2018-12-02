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

public final class AliasOptions extends Options {
	public static final int PROP_DIRECT = 0;

	public static final int PROP_ARRAY = PropertyOptions.ARRAY;

	public static final int PROP_ARRAY_ORDERED = PropertyOptions.ARRAY_ORDERED;

	public static final int PROP_ARRAY_ALTERNATE = PropertyOptions.ARRAY_ALTERNATE;

	public static final int PROP_ARRAY_ALT_TEXT = PropertyOptions.ARRAY_ALT_TEXT;

	public AliasOptions() {}

	public AliasOptions(int options) throws XMPException {
		super(options);
	}

	public boolean isSimple() {
		return getOptions() == PROP_DIRECT;
	}

	public boolean isArray() {
		return getOption(PROP_ARRAY);
	}

	public AliasOptions setArray(boolean value) {
		setOption(PROP_ARRAY, value);
		return this;
	}

	public boolean isArrayOrdered() {
		return getOption(PROP_ARRAY_ORDERED);
	}

	public AliasOptions setArrayOrdered(boolean value) {
		setOption(PROP_ARRAY | PROP_ARRAY_ORDERED, value);
		return this;
	}

	public boolean isArrayAlternate() {
		return getOption(PROP_ARRAY_ALTERNATE);
	}

	public AliasOptions setArrayAlternate(boolean value) {
		setOption(PROP_ARRAY | PROP_ARRAY_ORDERED | PROP_ARRAY_ALTERNATE, value);
		return this;
	}

	public boolean isArrayAltText() {
		return getOption(PROP_ARRAY_ALT_TEXT);
	}

	public AliasOptions setArrayAltText(boolean value) {
		setOption(PROP_ARRAY | PROP_ARRAY_ORDERED | PROP_ARRAY_ALTERNATE | PROP_ARRAY_ALT_TEXT, value);
		return this;
	}

	public PropertyOptions toPropertyOptions() throws XMPException {
		return new PropertyOptions(getOptions());
	}

	protected String defineOptionName(int option) {
		switch (option) {
		case PROP_DIRECT:
			return "PROP_DIRECT";
		case PROP_ARRAY:
			return "ARRAY";
		case PROP_ARRAY_ORDERED:
			return "ARRAY_ORDERED";
		case PROP_ARRAY_ALTERNATE:
			return "ARRAY_ALTERNATE";
		case PROP_ARRAY_ALT_TEXT:
			return "ARRAY_ALT_TEXT";
		default:
			return null;
		}
	}

	protected int getValidOptions() {
		return PROP_DIRECT | PROP_ARRAY | PROP_ARRAY_ORDERED | PROP_ARRAY_ALTERNATE | PROP_ARRAY_ALT_TEXT;
	}

}