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

import com.github.gasrios.xmp.options.IteratorOptions;
import com.github.gasrios.xmp.options.ParseOptions;
import com.github.gasrios.xmp.options.PropertyOptions;
import com.github.gasrios.xmp.properties.XMPProperty;
import com.github.gasrios.xmp.properties.XMPPropertyInfo;

public interface XMPMeta extends Cloneable, Iterable<XMPPropertyInfo> {

	XMPProperty getProperty(String schemaNS, String propName) throws XMPException;

	XMPProperty getArrayItem(String schemaNS, String arrayName, int itemIndex) throws XMPException;

	int countArrayItems(String schemaNS, String arrayName) throws XMPException;

	XMPProperty getStructField(String schemaNS, String structName, String fieldNS, String fieldName) throws XMPException;

	XMPProperty getQualifier(String schemaNS, String propName, String qualNS, String qualName) throws XMPException;

	void setProperty(String schemaNS, String propName, Object propValue, PropertyOptions options) throws XMPException;

	void setProperty(String schemaNS, String propName, Object propValue) throws XMPException;

	void setArrayItem(String schemaNS, String arrayName, int itemIndex, String itemValue, PropertyOptions options) throws XMPException;

	void setArrayItem(String schemaNS, String arrayName, int itemIndex, String itemValue) throws XMPException;

	void insertArrayItem(String schemaNS, String arrayName, int itemIndex, String itemValue, PropertyOptions options) throws XMPException;

	void insertArrayItem(String schemaNS, String arrayName, int itemIndex, String itemValue) throws XMPException;

	void appendArrayItem(String schemaNS, String arrayName, PropertyOptions arrayOptions, String itemValue, PropertyOptions itemOptions)
		throws XMPException;

	void appendArrayItem(String schemaNS, String arrayName, String itemValue) throws XMPException;

	void setStructField(String schemaNS, String structName, String fieldNS, String fieldName, String fieldValue, PropertyOptions options)
		throws XMPException;

	void setStructField(String schemaNS, String structName, String fieldNS, String fieldName, String fieldValue) throws XMPException;

	void setQualifier(String schemaNS, String propName, String qualNS, String qualName, String qualValue, PropertyOptions options) throws XMPException;

	void setQualifier(String schemaNS, String propName, String qualNS, String qualName, String qualValue) throws XMPException;

	void deleteProperty(String schemaNS, String propName);

	void deleteArrayItem(String schemaNS, String arrayName, int itemIndex);

	void deleteStructField(String schemaNS, String structName, String fieldNS, String fieldName);

	void deleteQualifier(String schemaNS, String propName, String qualNS, String qualName);

	boolean doesPropertyExist(String schemaNS, String propName);

	boolean doesArrayItemExist(String schemaNS, String arrayName, int itemIndex);

	boolean doesStructFieldExist(String schemaNS, String structName, String fieldNS, String fieldName);

	boolean doesQualifierExist(String schemaNS, String propName, String qualNS, String qualName);

	XMPProperty getLocalizedText(String schemaNS, String altTextName, String genericLang, String specificLang) throws XMPException;

	void setLocalizedText(String schemaNS, String altTextName, String genericLang, String specificLang, String itemValue, PropertyOptions options)
		throws XMPException;

	void setLocalizedText(String schemaNS, String altTextName, String genericLang, String specificLang, String itemValue) throws XMPException;

	Boolean getPropertyBoolean(String schemaNS, String propName) throws XMPException;

	Integer getPropertyInteger(String schemaNS, String propName) throws XMPException;

	Long getPropertyLong(String schemaNS, String propName) throws XMPException;

	Double getPropertyDouble(String schemaNS, String propName) throws XMPException;

	XMPDateTime getPropertyDate(String schemaNS, String propName) throws XMPException;

	Calendar getPropertyCalendar(String schemaNS, String propName) throws XMPException;

	byte[] getPropertyBase64(String schemaNS, String propName) throws XMPException;

	String getPropertyString(String schemaNS, String propName) throws XMPException;

	void setPropertyBoolean(String schemaNS, String propName, boolean propValue, PropertyOptions options) throws XMPException;

	void setPropertyBoolean(String schemaNS, String propName, boolean propValue) throws XMPException;

	void setPropertyInteger(String schemaNS, String propName, int propValue, PropertyOptions options) throws XMPException;

	void setPropertyInteger(String schemaNS, String propName, int propValue) throws XMPException;

	void setPropertyLong(String schemaNS, String propName, long propValue, PropertyOptions options) throws XMPException;

	void setPropertyLong(String schemaNS, String propName, long propValue) throws XMPException;

	void setPropertyDouble(String schemaNS, String propName, double propValue, PropertyOptions options) throws XMPException;

	void setPropertyDouble(String schemaNS, String propName, double propValue) throws XMPException;

	void setPropertyDate(String schemaNS, String propName, XMPDateTime propValue, PropertyOptions options) throws XMPException;

	void setPropertyDate(String schemaNS, String propName, XMPDateTime propValue) throws XMPException;

	void setPropertyCalendar(String schemaNS, String propName, Calendar propValue, PropertyOptions options) throws XMPException;

	void setPropertyCalendar(String schemaNS, String propName, Calendar propValue) throws XMPException;

	void setPropertyBase64(String schemaNS, String propName, byte[] propValue, PropertyOptions options) throws XMPException;

	void setPropertyBase64(String schemaNS, String propName, byte[] propValue) throws XMPException;

	XMPIterator iterator() throws XMPException;

	XMPIterator iterator(IteratorOptions options) throws XMPException;

	XMPIterator iterator(String schemaNS, String propName, IteratorOptions options) throws XMPException;

	String getObjectName();

	void setObjectName(String name);

	String getPacketHeader();

	Object clone();

	void sort();

	void normalize(ParseOptions options) throws XMPException;

	String dumpObject();

}