// =================================================================================================
// ADOBE SYSTEMS INCORPORATED
// Copyright 2006 Adobe Systems Incorporated
// All Rights Reserved
//
// NOTICE:  Adobe permits you to use, modify, and distribute this file in accordance with the terms
// of the Adobe license agreement accompanying it.
// =================================================================================================

package com.github.gasrios.xmp.impl;

public class QName {

	private String prefix;

	private String localName;

	public QName(String qname) {
		int colon = qname.indexOf(':');
		if (colon >= 0) {
			prefix = qname.substring(0, colon);
			localName = qname.substring(colon + 1);
		} else {
			prefix = "";
			localName = qname;
		}
	}

	public QName(String prefix, String localName) {
		this.prefix = prefix;
		this.localName = localName;
	}

	public boolean hasPrefix() {
		return prefix != null && prefix.length() > 0;
	}

	public String getLocalName() {
		return localName;
	}

	public String getPrefix() {
		return prefix;
	}

}