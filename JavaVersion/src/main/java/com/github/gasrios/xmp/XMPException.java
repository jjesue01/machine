// =================================================================================================
// ADOBE SYSTEMS INCORPORATED
// Copyright 2006 Adobe Systems Incorporated
// All Rights Reserved
//
// NOTICE:  Adobe permits you to use, modify, and distribute this file in accordance with the terms
// of the Adobe license agreement accompanying it.
// =================================================================================================

package com.github.gasrios.xmp;

public class XMPException extends RuntimeException {

	private static final long serialVersionUID = -2923540786785662373L;

	private int errorCode;

	public XMPException(String message, int errorCode) {
		super(message);
		this.errorCode = errorCode;
	}

	public XMPException(String message, int errorCode, Throwable t) {
		super(message, t);
		this.errorCode = errorCode;
	}

	public int getErrorCode() {
		return errorCode;
	}

}