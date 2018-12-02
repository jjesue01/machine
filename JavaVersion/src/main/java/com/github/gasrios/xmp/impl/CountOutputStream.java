// =================================================================================================
// ADOBE SYSTEMS INCORPORATED
// Copyright 2006 Adobe Systems Incorporated
// All Rights Reserved
//
// NOTICE:  Adobe permits you to use, modify, and distribute this file in accordance with the terms
// of the Adobe license agreement accompanying it.
// =================================================================================================

package com.github.gasrios.xmp.impl;

import java.io.IOException;
import java.io.OutputStream;

public final class CountOutputStream extends OutputStream {

	private final OutputStream out;

	private int bytesWritten = 0;

	CountOutputStream(OutputStream out) {
		this.out = out;
	}

	public void write(byte[] buf, int off, int len) throws IOException {
		out.write(buf, off, len);
		bytesWritten += len;
	}

	public void write(byte[] buf) throws IOException {
		out.write(buf);
		bytesWritten += buf.length;
	}

	public void write(int b) throws IOException {
		out.write(b);
		bytesWritten++;
	}

	public int getBytesWritten() {
		return bytesWritten;
	}

}