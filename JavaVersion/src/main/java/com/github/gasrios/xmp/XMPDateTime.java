// =================================================================================================
// ADOBE SYSTEMS INCORPORATED
// Copyright 2006 Adobe Systems Incorporated
// All Rights Reserved
//
// NOTICE:  Adobe permits you to use, modify, and distribute this file in accordance with the terms
// of the Adobe license agreement accompanying it.
// =================================================================================================

package com.github.gasrios.xmp;

import java.util.Calendar;
import java.util.TimeZone;

public interface XMPDateTime extends Comparable<XMPDateTime> {

	int getYear();

	void setYear(int year);

	int getMonth();

	void setMonth(int month);

	int getDay();

	void setDay(int day);

	int getHour();

	void setHour(int hour);

	int getMinute();

	void setMinute(int minute);

	int getSecond();

	void setSecond(int second);

	int getNanoSecond();

	void setNanoSecond(int nanoSecond);

	TimeZone getTimeZone();

	void setTimeZone(TimeZone tz);

	boolean hasDate();

	boolean hasTime();

	boolean hasTimeZone();

	Calendar getCalendar();

	String getISO8601String();

}