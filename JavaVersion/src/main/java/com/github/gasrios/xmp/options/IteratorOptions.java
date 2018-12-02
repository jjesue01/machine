// =================================================================================================
// ADOBE SYSTEMS INCORPORATED
// Copyright 2006 Adobe Systems Incorporated
// All Rights Reserved
//
// NOTICE:  Adobe permits you to use, modify, and distribute this file in accordance with the terms
// of the Adobe license agreement accompanying it.
// =================================================================================================

package com.github.gasrios.xmp.options;

public final class IteratorOptions extends Options {

	public static final int JUST_CHILDREN = 0x0100;

	public static final int JUST_LEAFNODES = 0x0200;

	public static final int JUST_LEAFNAME = 0x0400;

	public static final int OMIT_QUALIFIERS = 0x1000;

	public boolean isJustChildren() {
		return getOption(JUST_CHILDREN);
	}

	public boolean isJustLeafname() {
		return getOption(JUST_LEAFNAME);
	}

	public boolean isJustLeafnodes() {
		return getOption(JUST_LEAFNODES);
	}

	public boolean isOmitQualifiers() {
		return getOption(OMIT_QUALIFIERS);
	}

	public IteratorOptions setJustChildren(boolean value) {
		setOption(JUST_CHILDREN, value);
		return this;
	}

	public IteratorOptions setJustLeafname(boolean value) {
		setOption(JUST_LEAFNAME, value);
		return this;
	}

	public IteratorOptions setJustLeafnodes(boolean value) {
		setOption(JUST_LEAFNODES, value);
		return this;
	}

	public IteratorOptions setOmitQualifiers(boolean value) {
		setOption(OMIT_QUALIFIERS, value);
		return this;
	}

	protected String defineOptionName(int option) {
		switch (option) {
		case JUST_CHILDREN:
			return "JUST_CHILDREN";
		case JUST_LEAFNODES:
			return "JUST_LEAFNODES";
		case JUST_LEAFNAME:
			return "JUST_LEAFNAME";
		case OMIT_QUALIFIERS:
			return "OMIT_QUALIFIERS";
		default:
			return null;
		}
	}

	protected int getValidOptions() {
		return JUST_CHILDREN | JUST_LEAFNODES | JUST_LEAFNAME | OMIT_QUALIFIERS;
	}

}