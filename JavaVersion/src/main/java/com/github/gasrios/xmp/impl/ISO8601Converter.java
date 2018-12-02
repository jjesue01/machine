// =================================================================================================
// ADOBE SYSTEMS INCORPORATED
// Copyright 2006 Adobe Systems Incorporated
// All Rights Reserved
//
// NOTICE:  Adobe permits you to use, modify, and distribute this file in accordance with the terms
// of the Adobe license agreement accompanying it.
// =================================================================================================

package com.github.gasrios.xmp.impl;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.SimpleTimeZone;

import com.github.gasrios.xmp.XMPDateTime;
import com.github.gasrios.xmp.XMPError;
import com.github.gasrios.xmp.XMPException;

public final class ISO8601Converter {

	private ISO8601Converter() {}

	public static XMPDateTime parse(String iso8601String) throws XMPException {
		return parse(iso8601String, new XMPDateTimeImpl());
	}

	public static XMPDateTime parse(String iso8601String, XMPDateTime binValue) throws XMPException {
		if (iso8601String == null) throw new XMPException("Parameter must not be null", XMPError.BADPARAM);
		else if (iso8601String.length() == 0) return binValue;
		ParseState input = new ParseState(iso8601String);
		int value;
		if (input.ch(0) == '-') input.skip();
		value = input.gatherInt("Invalid year in date string", 9999);
		if (input.hasNext() && input.ch() != '-') throw new XMPException("Invalid date string, after year", XMPError.BADVALUE);
		if (input.ch(0) == '-') value = -value;
		binValue.setYear(value);
		if (!input.hasNext()) return binValue;
		input.skip();
		value = input.gatherInt("Invalid month in date string", 12);
		if (input.hasNext() && input.ch() != '-') throw new XMPException("Invalid date string, after month", XMPError.BADVALUE);
		binValue.setMonth(value);
		if (!input.hasNext()) return binValue;
		input.skip();
		value = input.gatherInt("Invalid day in date string", 31);
		if (input.hasNext() && input.ch() != 'T') throw new XMPException("Invalid date string, after day", XMPError.BADVALUE);
		binValue.setDay(value);
		if (!input.hasNext()) return binValue;
		input.skip();
		value = input.gatherInt("Invalid hour in date string", 23);
		binValue.setHour(value);
		if (!input.hasNext()) return binValue;
		if (input.ch() == ':') {
			input.skip();
			value = input.gatherInt("Invalid minute in date string", 59);
			if (input.hasNext() && input.ch() != ':' && input.ch() != 'Z' && input.ch() != '+' && input.ch() != '-')
				throw new XMPException("Invalid date string, after minute", XMPError.BADVALUE);
			binValue.setMinute(value);
		}
		if (!input.hasNext()) return binValue;
		else if (input.hasNext() && input.ch() == ':') {
			input.skip();
			value = input.gatherInt("Invalid whole seconds in date string", 59);
			if (input.hasNext() && input.ch() != '.' && input.ch() != 'Z' && input.ch() != '+' && input.ch() != '-')
				throw new XMPException("Invalid date string, after whole seconds", XMPError.BADVALUE);
			binValue.setSecond(value);
			if (input.ch() == '.') {
				input.skip();
				int digits = input.pos();
				value = input.gatherInt("Invalid fractional seconds in date string", 999999999);
				if (input.hasNext() && (input.ch() != 'Z' && input.ch() != '+' && input.ch() != '-'))
					throw new XMPException("Invalid date string, after fractional second", XMPError.BADVALUE);
				digits = input.pos() - digits;
				for (; digits > 9; --digits) value = value / 10;
				for (; digits < 9; ++digits) value = value * 10;
				binValue.setNanoSecond(value);
			}
		} else if (input.ch() != 'Z' && input.ch() != '+' && input.ch() != '-')
			throw new XMPException("Invalid date string, after time", XMPError.BADVALUE);
		int tzSign = 0;
		int tzHour = 0;
		int tzMinute = 0;
		if (!input.hasNext()) return binValue;
		else if (input.ch() == 'Z') input.skip();
		else if (input.hasNext()) {
			if (input.ch() == '+') tzSign = 1;
			else if (input.ch() == '-') tzSign = -1;
			else throw new XMPException("Time zone must begin with 'Z', '+', or '-'", XMPError.BADVALUE);
			input.skip();
			tzHour = input.gatherInt("Invalid time zone hour in date string", 23);
			if (input.hasNext()) {
				if (input.ch() != ':') throw new XMPException("Invalid date string, after time zone hour", XMPError.BADVALUE);
				else {
					input.skip();
					tzMinute = input.gatherInt("Invalid time zone minute in date string", 59);
				}
			}
		}
		binValue.setTimeZone(new SimpleTimeZone((tzHour * 3600 * 1000 + tzMinute * 60 * 1000) * tzSign, ""));
		if (input.hasNext()) throw new XMPException("Invalid date string, extra chars at end", XMPError.BADVALUE);
		return binValue;
	}

	public static String render(XMPDateTime dateTime) {
		StringBuffer buffer = new StringBuffer();
		if (dateTime.hasDate()) {
			DecimalFormat df = new DecimalFormat("0000", new DecimalFormatSymbols(Locale.ENGLISH));
			buffer.append(df.format(dateTime.getYear()));
			if (dateTime.getMonth() == 0) return buffer.toString();
			df.applyPattern("'-'00");
			buffer.append(df.format(dateTime.getMonth()));
			if (dateTime.getDay() == 0) return buffer.toString();
			buffer.append(df.format(dateTime.getDay()));
			if (dateTime.hasTime()) {
				buffer.append('T');
				df.applyPattern("00");
				buffer.append(df.format(dateTime.getHour()));
				buffer.append(':');
				buffer.append(df.format(dateTime.getMinute()));
				if (dateTime.getSecond() != 0 || dateTime.getNanoSecond() != 0) {
					df.applyPattern(":00.#########");
					buffer.append(df.format(dateTime.getSecond() + dateTime.getNanoSecond() / 1e9d));
				}
				if (dateTime.hasTimeZone()) {
					int offset = dateTime.getTimeZone().getOffset(dateTime.getCalendar().getTimeInMillis());
					if (offset == 0) buffer.append('Z');
					else {
						int thours = offset / 3600000;
						int tminutes = Math.abs(offset % 3600000 / 60000);
						df.applyPattern("+00;-00");
						buffer.append(df.format(thours));
						df.applyPattern(":00");
						buffer.append(df.format(tminutes));
					}
				}
			}
		}
		return buffer.toString();
	}

}

class ParseState {

	private String str;

	private int pos = 0;

	public ParseState(String str) {
		this.str = str;
	}

	public int length() {
		return str.length();
	}

	public boolean hasNext() {
		return pos < str.length();
	}

	public char ch(int index) {
		return index < str.length() ? str.charAt(index) : 0x0000;
	}

	public char ch() {
		return pos < str.length() ? str.charAt(pos) : 0x0000;
	}

	public void skip() {
		pos++;
	}

	public int pos() {
		return pos;
	}

	public int gatherInt(String errorMsg, int maxValue) throws XMPException {
		int value = 0;
		boolean success = false;
		char ch = ch(pos);
		while ('0' <= ch && ch <= '9') {
			value = (value * 10) + (ch - '0');
			success = true;
			pos++;
			ch = ch(pos);
		}
		if (success) {
			if (value > maxValue) {
				return maxValue;
			} else if (value < 0) {
				return 0;
			} else {
				return value;
			}
		} else throw new XMPException(errorMsg, XMPError.BADVALUE);
	}

}