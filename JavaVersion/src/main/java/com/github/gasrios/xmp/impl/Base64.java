// =================================================================================================
// ADOBE SYSTEMS INCORPORATED
// Copyright 2001 Adobe Systems Incorporated
// All Rights Reserved
//
// NOTICE:  Adobe permits you to use, modify, and distribute this file in accordance with the terms
// of the Adobe license agreement accompanying it.
// =================================================================================================

package com.github.gasrios.xmp.impl;

public class Base64 {

	private static final byte INVALID = -1;

	private static final byte WHITESPACE = -2;

	private static final byte EQUAL = -3;

	private static byte[] base64 = {
		(byte) 'A', (byte) 'B', (byte) 'C', (byte) 'D',
		(byte) 'E', (byte) 'F', (byte) 'G', (byte) 'H',
		(byte) 'I', (byte) 'J', (byte) 'K', (byte) 'L',
		(byte) 'M', (byte) 'N', (byte) 'O', (byte) 'P',
		(byte) 'Q', (byte) 'R', (byte) 'S', (byte) 'T',
		(byte) 'U', (byte) 'V', (byte) 'W', (byte) 'X',
		(byte) 'Y', (byte) 'Z', (byte) 'a', (byte) 'b',
		(byte) 'c', (byte) 'd', (byte) 'e', (byte) 'f',
		(byte) 'g', (byte) 'h', (byte) 'i', (byte) 'j',
		(byte) 'k', (byte) 'l', (byte) 'm', (byte) 'n',
		(byte) 'o', (byte) 'p', (byte) 'q', (byte) 'r',
		(byte) 's', (byte) 't', (byte) 'u', (byte) 'v',
		(byte) 'w', (byte) 'x', (byte) 'y', (byte) 'z',
		(byte) '0', (byte) '1', (byte) '2', (byte) '3',
		(byte) '4', (byte) '5', (byte) '6', (byte) '7',
		(byte) '8', (byte) '9', (byte) '+', (byte) '/'
	};

	private static byte[] ascii = new byte[255];

	static {
		for (int idx = 0; idx < 255; idx++) ascii[idx] = INVALID;
		for (int idx = 0; idx < base64.length; idx++) ascii[base64[idx]] = (byte) idx;
		ascii[0x09] = WHITESPACE;
		ascii[0x0A] = WHITESPACE;
		ascii[0x0D] = WHITESPACE;
		ascii[0x20] = WHITESPACE;
		ascii[0x3d] = EQUAL;
	}

	public static final byte[] encode(byte[] src) {
		return encode(src, 0);
	}

	public static final byte[] encode(byte[] src, int lineFeed) {
		lineFeed = lineFeed / 4 * 4;
		if (lineFeed < 0) lineFeed = 0;
		int codeLength = ((src.length + 2) / 3) * 4;
		if (lineFeed > 0) codeLength += (codeLength - 1) / lineFeed;
		byte[] dst = new byte[codeLength];
		int bits24;
		int bits6;
		int didx = 0;
		int sidx = 0;
		int lf = 0;
		while (sidx + 3 <= src.length) {
			bits24 = (src[sidx++] & 0xFF) << 16;
			bits24 |= (src[sidx++] & 0xFF) << 8;
			bits24 |= (src[sidx++] & 0xFF) << 0;
			bits6 = (bits24 & 0x00FC0000) >> 18;
			dst[didx++] = base64[bits6];
			bits6 = (bits24 & 0x0003F000) >> 12;
			dst[didx++] = base64[bits6];
			bits6 = (bits24 & 0x00000FC0) >> 6;
			dst[didx++] = base64[bits6];
			bits6 = (bits24 & 0x0000003F);
			dst[didx++] = base64[bits6];
			lf += 4;
			if (didx < codeLength && lineFeed > 0 && lf % lineFeed == 0) dst[didx++] = 0x0A;
		}
		if (src.length - sidx == 2) {
			bits24 = (src[sidx] & 0xFF) << 16;
			bits24 |= (src[sidx + 1] & 0xFF) << 8;
			bits6 = (bits24 & 0x00FC0000) >> 18;
			dst[didx++] = base64[bits6];
			bits6 = (bits24 & 0x0003F000) >> 12;
			dst[didx++] = base64[bits6];
			bits6 = (bits24 & 0x00000FC0) >> 6;
			dst[didx++] = base64[bits6];
			dst[didx++] = (byte) '=';
		} else if (src.length - sidx == 1) {
			bits24 = (src[sidx] & 0xFF) << 16;
			bits6 = (bits24 & 0x00FC0000) >> 18;
			dst[didx++] = base64[bits6];
			bits6 = (bits24 & 0x0003F000) >> 12;
			dst[didx++] = base64[bits6];
			dst[didx++] = (byte) '=';
			dst[didx++] = (byte) '=';
		}
		return dst;
	}

	public static final String encode(String src) {
		return new String(encode(src.getBytes()));
	}

	public static final byte[] decode(byte[] src) throws IllegalArgumentException {
		int sidx;
		int srcLen = 0;
		for (sidx = 0; sidx < src.length; sidx++) {
			byte val = ascii[src[sidx]];
			if (val >= 0) src[srcLen++] = val;
			else if (val == INVALID) throw new IllegalArgumentException("Invalid base 64 string");
		}
		while (srcLen > 0 && src[srcLen - 1] == EQUAL) srcLen--;
		byte[] dst = new byte[srcLen * 3 / 4];
		int didx;
		for (sidx = 0, didx = 0; didx < dst.length - 2; sidx += 4, didx += 3) {
			dst[didx] = (byte) (((src[sidx] << 2) & 0xFF) | ((src[sidx + 1] >>> 4) & 0x03));
			dst[didx + 1] = (byte) (((src[sidx + 1] << 4) & 0xFF) | ((src[sidx + 2] >>> 2) & 0x0F));
			dst[didx + 2] = (byte) (((src[sidx + 2] << 6) & 0xFF) | ((src[sidx + 3]) & 0x3F));
		}
		if (didx < dst.length) dst[didx] = (byte) (((src[sidx] << 2) & 0xFF) | ((src[sidx + 1] >>> 4) & 0x03));
		if (++didx < dst.length) dst[didx] = (byte) (((src[sidx + 1] << 4) & 0xFF) | ((src[sidx + 2] >>> 2) & 0x0F));
		return dst;
	}

	public static final String decode(String src) {
		return new String(decode(src.getBytes()));
	}

}