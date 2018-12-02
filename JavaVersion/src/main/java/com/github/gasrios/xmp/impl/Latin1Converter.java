// =================================================================================================
// ADOBE SYSTEMS INCORPORATED
// Copyright 2006 Adobe Systems Incorporated
// All Rights Reserved
//
// NOTICE:  Adobe permits you to use, modify, and distribute this file in accordance with the terms
// of the Adobe license agreement accompanying it.
// =================================================================================================

package com.github.gasrios.xmp.impl;

import java.io.UnsupportedEncodingException;

public class Latin1Converter {

	private static final int STATE_START = 0;

	private static final int STATE_UTF8CHAR = 11;

	private Latin1Converter() {}

	public static ByteBuffer convert(ByteBuffer buffer) {
		if ("UTF-8".equals(buffer.getEncoding())) {
			byte[] readAheadBuffer = new byte[8];
			int readAhead = 0;
			int expectedBytes = 0;
			ByteBuffer out = new ByteBuffer(buffer.length() * 4 / 3);
			int state = STATE_START;
			for (int i = 0; i < buffer.length(); i++) {
				int b = buffer.charAt(i);
				switch (state) {
				default:
				case STATE_START:
					if (b < 0x7F) out.append((byte) b);
					else if (b >= 0xC0) {
						expectedBytes = -1;
						int test = b;
						for (; expectedBytes < 8 && (test & 0x80) == 0x80; test = test << 1) expectedBytes++;
						readAheadBuffer[readAhead++] = (byte) b;
						state = STATE_UTF8CHAR;
					} else out.append(convertToUTF8((byte) b));
					break;
				case STATE_UTF8CHAR:
					if (expectedBytes > 0 && (b & 0xC0) == 0x80) {
						readAheadBuffer[readAhead++] = (byte) b;
						expectedBytes--;
						if (expectedBytes == 0) {
							out.append(readAheadBuffer, 0, readAhead);
							readAhead = 0;
							state = STATE_START;
						}
					} else {
						out.append(convertToUTF8(readAheadBuffer[0]));
						i = i - readAhead;
						readAhead = 0;
						state = STATE_START;
					}
					break;
				}
			}
			if (state == STATE_UTF8CHAR) for (int j = 0; j < readAhead; j++) out.append(convertToUTF8(readAheadBuffer[j]));
			return out;
		} else return buffer;
	}

	private static byte[] convertToUTF8(byte ch) {
		int c = ch & 0xFF;
		try {
			if (c >= 0x80) {
				if (c == 0x81 || c == 0x8D || c == 0x8F || c == 0x90 || c == 0x9D) return new byte[] { 0x20 };
				return new String(new byte[] { ch }, "cp1252").getBytes("UTF-8");
			}
		} catch (UnsupportedEncodingException e) {}
		return new byte[] { ch };
	}

}