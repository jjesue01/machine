// =================================================================================================
// ADOBE SYSTEMS INCORPORATED
// Copyright 2006 Adobe Systems Incorporated
// All Rights Reserved
//
// NOTICE:  Adobe permits you to use, modify, and distribute this file in accordance with the terms
// of the Adobe license agreement accompanying it.
// =================================================================================================

package com.github.gasrios.xmp.options;

import java.util.HashMap;
import java.util.Map;

import com.github.gasrios.xmp.XMPError;
import com.github.gasrios.xmp.XMPException;

public abstract class Options {

	private int options = 0;

	private Map<Integer, String> optionNames = null;

	public Options() {}

	public Options(int options) throws XMPException {
		assertOptionsValid(options);
		setOptions(options);
	}

	public void clear() {
		options = 0;
	}

	public boolean isExactly(int optionBits) {
		return getOptions() == optionBits;
	}

	public boolean containsAllOptions(int optionBits) {
		return (getOptions() & optionBits) == optionBits;
	}

	public boolean containsOneOf(int optionBits) {
		return ((getOptions()) & optionBits) != 0;
	}

	protected boolean getOption(int optionBit) {
		return (options & optionBit) != 0;
	}

	public void setOption(int optionBits, boolean value) {
		options = value ? options | optionBits : options & ~optionBits;
	}

	public int getOptions() {
		return options;
	}

	public void setOptions(int options) throws XMPException {
		assertOptionsValid(options);
		this.options = options;
	}

	public boolean equals(Object obj) {
		return getOptions() == ((Options) obj).getOptions();
	}

	public int hashCode() {
		return getOptions();
	}

	public String getOptionsString() {
		if (options != 0) {
			StringBuffer sb = new StringBuffer();
			int theBits = options;
			while (theBits != 0) {
				int oneLessBit = theBits & (theBits - 1); // clear rightmost one bit
				int singleBit = theBits ^ oneLessBit;
				String bitName = getOptionName(singleBit);
				sb.append(bitName);
				if (oneLessBit != 0) {
					sb.append(" | ");
				}
				theBits = oneLessBit;
			}
			return sb.toString();
		} else {
			return "<none>";
		}
	}

	public String toString() {
		return "0x" + Integer.toHexString(options);
	}

	protected abstract int getValidOptions();

	protected abstract String defineOptionName(int option);

	protected void assertConsistency(int options) throws XMPException {}

	private void assertOptionsValid(int options) throws XMPException {
		int invalidOptions = options & ~getValidOptions();
		if (invalidOptions == 0) {
			assertConsistency(options);
		} else {
			throw new XMPException("The option bit(s) 0x" + Integer.toHexString(invalidOptions) + " are invalid!",
					XMPError.BADOPTIONS);
		}
	}

	private String getOptionName(int option) {
		Map<Integer, String> optionsNames = procureOptionNames();

		Integer key = new Integer(option);
		String result = optionsNames.get(key);
		if (result == null) {
			result = defineOptionName(option);
			if (result != null) {
				optionsNames.put(key, result);
			} else {
				result = "<option name not defined>";
			}
		}

		return result;
	}

	private Map<Integer, String> procureOptionNames() {
		if (optionNames == null) {
			optionNames = new HashMap<Integer, String>();
		}
		return optionNames;
	}

}