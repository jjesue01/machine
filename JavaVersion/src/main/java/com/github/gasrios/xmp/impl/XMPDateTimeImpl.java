// =================================================================================================
// ADOBE SYSTEMS INCORPORATED
// Copyright 2006 Adobe Systems Incorporated
// All Rights Reserved
//
// NOTICE:  Adobe permits you to use, modify, and distribute this file in accordance with the terms
// of the Adobe license agreement accompanying it.
// =================================================================================================

package com.github.gasrios.xmp.impl;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

import com.github.gasrios.xmp.XMPDateTime;
import com.github.gasrios.xmp.XMPException;

public class XMPDateTimeImpl implements XMPDateTime {

	private int year = 0;

	private int month = 0;

	private int day = 0;

	private int hour = 0;

	private int minute = 0;

	private int second = 0;

	private TimeZone timeZone = null;

	private int nanoSeconds;

	private boolean hasDate = false;

	private boolean hasTime = false;

	private boolean hasTimeZone = false;

	public XMPDateTimeImpl() {}

	public XMPDateTimeImpl(Calendar calendar) {
		GregorianCalendar intCalendar = (GregorianCalendar) Calendar.getInstance(Locale.US);
		intCalendar.setGregorianChange(new Date(Long.MIN_VALUE));
		intCalendar.setTimeZone(calendar.getTimeZone());
		intCalendar.setTime(calendar.getTime());
		this.year = intCalendar.get(Calendar.YEAR);
		this.month = intCalendar.get(Calendar.MONTH) + 1;
		this.day = intCalendar.get(Calendar.DAY_OF_MONTH);
		this.hour = intCalendar.get(Calendar.HOUR_OF_DAY);
		this.minute = intCalendar.get(Calendar.MINUTE);
		this.second = intCalendar.get(Calendar.SECOND);
		this.nanoSeconds = intCalendar.get(Calendar.MILLISECOND) * 1000000;
		this.timeZone = intCalendar.getTimeZone();
		hasDate = hasTime = hasTimeZone = true;
	}

	public XMPDateTimeImpl(Date date, TimeZone timeZone) {
		GregorianCalendar calendar = new GregorianCalendar(timeZone);
		calendar.setTime(date);
		this.year = calendar.get(Calendar.YEAR);
		this.month = calendar.get(Calendar.MONTH) + 1;
		this.day = calendar.get(Calendar.DAY_OF_MONTH);
		this.hour = calendar.get(Calendar.HOUR_OF_DAY);
		this.minute = calendar.get(Calendar.MINUTE);
		this.second = calendar.get(Calendar.SECOND);
		this.nanoSeconds = calendar.get(Calendar.MILLISECOND) * 1000000;
		this.timeZone = timeZone;
		hasDate = hasTime = hasTimeZone = true;
	}

	public XMPDateTimeImpl(String strValue) throws XMPException {
		ISO8601Converter.parse(strValue, this);
	}

	public int getYear() {
		return year;
	}

	public void setYear(int year) {
		this.year = Math.min(Math.abs(year), 9999);
		this.hasDate = true;
	}

	public int getMonth() {
		return month;
	}

	public void setMonth(int month) {
		if (month < 1) this.month = 1;
		else if (month > 12) this.month = 12;
		else this.month = month;
		this.hasDate = true;
	}

	public int getDay() {
		return day;
	}

	public void setDay(int day) {
		if (day < 1) this.day = 1;
		else if (day > 31) this.day = 31;
		else this.day = day;
		this.hasDate = true;
	}

	public int getHour() {
		return hour;
	}

	public void setHour(int hour) {
		this.hour = Math.min(Math.abs(hour), 23);
		this.hasTime = true;
	}

	public int getMinute() {
		return minute;
	}

	public void setMinute(int minute) {
		this.minute = Math.min(Math.abs(minute), 59);
		this.hasTime = true;
	}

	public int getSecond() {
		return second;
	}

	public void setSecond(int second) {
		this.second = Math.min(Math.abs(second), 59);
		this.hasTime = true;
	}

	public int getNanoSecond() {
		return nanoSeconds;
	}

	public void setNanoSecond(int nanoSecond) {
		this.nanoSeconds = nanoSecond;
		this.hasTime = true;
	}

	public int compareTo(XMPDateTime dt) {
		long d = getCalendar().getTimeInMillis() - ((XMPDateTime) dt).getCalendar().getTimeInMillis();
		return (int) Math.signum(d != 0? d : nanoSeconds - ((XMPDateTime) dt).getNanoSecond());
	}

	public TimeZone getTimeZone() {
		return timeZone;
	}

	public void setTimeZone(TimeZone timeZone) {
		this.timeZone = timeZone;
		this.hasTime = true;
		this.hasTimeZone = true;
	}

	public boolean hasDate() {
		return this.hasDate;
	}

	public boolean hasTime() {
		return this.hasTime;
	}

	public boolean hasTimeZone() {
		return this.hasTimeZone;
	}

	public Calendar getCalendar() {
		GregorianCalendar calendar = (GregorianCalendar) Calendar.getInstance(Locale.US);
		calendar.setGregorianChange(new Date(Long.MIN_VALUE));
		if (hasTimeZone) calendar.setTimeZone(timeZone);
		calendar.set(Calendar.YEAR, year);
		calendar.set(Calendar.MONTH, month - 1);
		calendar.set(Calendar.DAY_OF_MONTH, day);
		calendar.set(Calendar.HOUR_OF_DAY, hour);
		calendar.set(Calendar.MINUTE, minute);
		calendar.set(Calendar.SECOND, second);
		calendar.set(Calendar.MILLISECOND, nanoSeconds / 1000000);
		return calendar;
	}

	public String getISO8601String() {
		return ISO8601Converter.render(this);
	}

	public String toString() {
		return getISO8601String();
	}

}