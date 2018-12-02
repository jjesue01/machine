// =================================================================================================
// ADOBE SYSTEMS INCORPORATED
// Copyright 2006 Adobe Systems Incorporated
// All Rights Reserved
//
// NOTICE:  Adobe permits you to use, modify, and distribute this file in accordance with the terms
// of the Adobe license agreement accompanying it.
// =================================================================================================

package com.github.gasrios.xmp.options;

import com.github.gasrios.xmp.XMPError;
import com.github.gasrios.xmp.XMPException;

public final class PropertyOptions extends Options {

	public static final int NO_OPTIONS = 0x00000000;

	public static final int URI = 0x00000002;

	public static final int HAS_QUALIFIERS = 0x00000010;

	public static final int QUALIFIER = 0x00000020;

	public static final int HAS_LANGUAGE = 0x00000040;

	public static final int HAS_TYPE = 0x00000080;

	public static final int STRUCT = 0x00000100;

	public static final int ARRAY = 0x00000200;

	public static final int ARRAY_ORDERED = 0x00000400;

	public static final int ARRAY_ALTERNATE = 0x00000800;

	public static final int ARRAY_ALT_TEXT = 0x00001000;

	public static final int SCHEMA_NODE = 0x80000000;

	public static final int DELETE_EXISTING = 0x20000000;

	public PropertyOptions() {
		// reveal default constructor
	}

	public PropertyOptions(int options) throws XMPException {
		super(options);
	}

	public boolean isURI() {
		return getOption(URI);
	}

	public PropertyOptions setURI(boolean value) {
		setOption(URI, value);
		return this;
	}

	public boolean getHasQualifiers() {
		return getOption(HAS_QUALIFIERS);
	}

	public PropertyOptions setHasQualifiers(boolean value) {
		setOption(HAS_QUALIFIERS, value);
		return this;
	}

	public boolean isQualifier() {
		return getOption(QUALIFIER);
	}

	public PropertyOptions setQualifier(boolean value) {
		setOption(QUALIFIER, value);
		return this;
	}

	public boolean getHasLanguage() {
		return getOption(HAS_LANGUAGE);
	}

	public PropertyOptions setHasLanguage(boolean value) {
		setOption(HAS_LANGUAGE, value);
		return this;
	}

	public boolean getHasType() {
		return getOption(HAS_TYPE);
	}

	public PropertyOptions setHasType(boolean value) {
		setOption(HAS_TYPE, value);
		return this;
	}

	public boolean isStruct() {
		return getOption(STRUCT);
	}

	public PropertyOptions setStruct(boolean value) {
		setOption(STRUCT, value);
		return this;
	}

	public boolean isArray() {
		return getOption(ARRAY);
	}

	public PropertyOptions setArray(boolean value) {
		setOption(ARRAY, value);
		return this;
	}

	public boolean isArrayOrdered() {
		return getOption(ARRAY_ORDERED);
	}

	public PropertyOptions setArrayOrdered(boolean value) {
		setOption(ARRAY_ORDERED, value);
		return this;
	}

	public boolean isArrayAlternate() {
		return getOption(ARRAY_ALTERNATE);
	}

	public PropertyOptions setArrayAlternate(boolean value) {
		setOption(ARRAY_ALTERNATE, value);
		return this;
	}

	public boolean isArrayAltText() {
		return getOption(ARRAY_ALT_TEXT);
	}

	public PropertyOptions setArrayAltText(boolean value) {
		setOption(ARRAY_ALT_TEXT, value);
		return this;
	}

	public boolean isSchemaNode() {
		return getOption(SCHEMA_NODE);
	}

	public PropertyOptions setSchemaNode(boolean value) {
		setOption(SCHEMA_NODE, value);
		return this;
	}

	public boolean isCompositeProperty() {
		return (getOptions() & (ARRAY | STRUCT)) > 0;
	}

	public boolean isSimple() {
		return (getOptions() & (ARRAY | STRUCT)) == 0;
	}

	public boolean equalArrayTypes(PropertyOptions options) {
		return isArray() == options.isArray() && isArrayOrdered() == options.isArrayOrdered()
				&& isArrayAlternate() == options.isArrayAlternate() && isArrayAltText() == options.isArrayAltText();
	}

	public void mergeWith(PropertyOptions options) throws XMPException {
		if (options != null) {
			setOptions(getOptions() | options.getOptions());
		}
	}

	public boolean isOnlyArrayOptions() {
		return (getOptions() & ~(ARRAY | ARRAY_ORDERED | ARRAY_ALTERNATE | ARRAY_ALT_TEXT)) == 0;
	}

	protected int getValidOptions() {
		return URI | HAS_QUALIFIERS | QUALIFIER | HAS_LANGUAGE | HAS_TYPE | STRUCT | ARRAY | ARRAY_ORDERED
				| ARRAY_ALTERNATE | ARRAY_ALT_TEXT | SCHEMA_NODE;
	}

	protected String defineOptionName(int option) {
		switch (option) {
		case URI:
			return "URI";
		case HAS_QUALIFIERS:
			return "HAS_QUALIFIER";
		case QUALIFIER:
			return "QUALIFIER";
		case HAS_LANGUAGE:
			return "HAS_LANGUAGE";
		case HAS_TYPE:
			return "HAS_TYPE";
		case STRUCT:
			return "STRUCT";
		case ARRAY:
			return "ARRAY";
		case ARRAY_ORDERED:
			return "ARRAY_ORDERED";
		case ARRAY_ALTERNATE:
			return "ARRAY_ALTERNATE";
		case ARRAY_ALT_TEXT:
			return "ARRAY_ALT_TEXT";
		case SCHEMA_NODE:
			return "SCHEMA_NODE";
		default:
			return null;
		}
	}

	public void assertConsistency(int options) throws XMPException {
		if ((options & STRUCT) > 0 && (options & ARRAY) > 0) {
			throw new XMPException("IsStruct and IsArray options are mutually exclusive", XMPError.BADOPTIONS);
		} else if ((options & URI) > 0 && (options & (ARRAY | STRUCT)) > 0) {
			throw new XMPException("Structs and arrays can't have \"value\" options", XMPError.BADOPTIONS);
		}
	}

}