// =================================================================================================
// ADOBE SYSTEMS INCORPORATED
// Copyright 2006 Adobe Systems Incorporated
// All Rights Reserved
//
// NOTICE:  Adobe permits you to use, modify, and distribute this file in accordance with the terms
// of the Adobe license agreement accompanying it.
// =================================================================================================

package com.github.gasrios.xmp.impl.xpath;

import com.github.gasrios.xmp.XMPError;
import com.github.gasrios.xmp.XMPException;
import com.github.gasrios.xmp.XMPMetaFactory;
import com.github.gasrios.xmp.impl.Utils;
import com.github.gasrios.xmp.properties.XMPAliasInfo;

public final class XMPPathParser {

	private XMPPathParser() {}

	public static XMPPath expandXPath(String schemaNS, String path) throws XMPException {
		if (schemaNS == null || path == null) {
			throw new XMPException("Parameter must not be null", XMPError.BADPARAM);
		}

		XMPPath expandedXPath = new XMPPath();
		PathPosition pos = new PathPosition();
		pos.path = path;

		parseRootNode(schemaNS, pos, expandedXPath);

		while (pos.stepEnd < path.length()) {
			pos.stepBegin = pos.stepEnd;

			skipPathDelimiter(path, pos);

			pos.stepEnd = pos.stepBegin;

			XMPPathSegment segment;
			if (path.charAt(pos.stepBegin) != '[') {
				segment = parseStructSegment(pos);
			} else {
				segment = parseIndexSegment(pos);
			}

			if (segment.getKind() == XMPPath.STRUCT_FIELD_STEP) {
				if (segment.getName().charAt(0) == '@') {
					segment.setName("?" + segment.getName().substring(1));
					if (!"?xml:lang".equals(segment.getName())) {
						throw new XMPException("Only xml:lang allowed with '@'", XMPError.BADXPATH);
					}
				}
				if (segment.getName().charAt(0) == '?') {
					pos.nameStart++;
					segment.setKind(XMPPath.QUALIFIER_STEP);
				}

				verifyQualName(pos.path.substring(pos.nameStart, pos.nameEnd));
			} else if (segment.getKind() == XMPPath.FIELD_SELECTOR_STEP) {
				if (segment.getName().charAt(1) == '@') {
					segment.setName("[?" + segment.getName().substring(2));
					if (!segment.getName().startsWith("[?xml:lang=")) {
						throw new XMPException("Only xml:lang allowed with '@'", XMPError.BADXPATH);
					}
				}

				if (segment.getName().charAt(1) == '?') {
					pos.nameStart++;
					segment.setKind(XMPPath.QUAL_SELECTOR_STEP);
					verifyQualName(pos.path.substring(pos.nameStart, pos.nameEnd));
				}
			}

			expandedXPath.add(segment);
		}
		return expandedXPath;
	}

	private static void skipPathDelimiter(String path, PathPosition pos) throws XMPException {
		if (path.charAt(pos.stepBegin) == '/') {

			pos.stepBegin++;

			if (pos.stepBegin >= path.length()) {
				throw new XMPException("Empty XMPPath segment", XMPError.BADXPATH);
			}
		}

		if (path.charAt(pos.stepBegin) == '*') {
			pos.stepBegin++;
			if (pos.stepBegin >= path.length() || path.charAt(pos.stepBegin) != '[') {
				throw new XMPException("Missing '[' after '*'", XMPError.BADXPATH);
			}
		}
	}

	private static XMPPathSegment parseStructSegment(PathPosition pos) throws XMPException {
		pos.nameStart = pos.stepBegin;
		while (pos.stepEnd < pos.path.length() && "/[*".indexOf(pos.path.charAt(pos.stepEnd)) < 0) {
			pos.stepEnd++;
		}
		pos.nameEnd = pos.stepEnd;

		if (pos.stepEnd == pos.stepBegin) {
			throw new XMPException("Empty XMPPath segment", XMPError.BADXPATH);
		}

		XMPPathSegment segment = new XMPPathSegment(pos.path.substring(pos.stepBegin, pos.stepEnd),
				XMPPath.STRUCT_FIELD_STEP);
		return segment;
	}

	private static XMPPathSegment parseIndexSegment(PathPosition pos) throws XMPException {
		XMPPathSegment segment;
		pos.stepEnd++; // Look at the character after the leading '['.

		if ('0' <= pos.path.charAt(pos.stepEnd) && pos.path.charAt(pos.stepEnd) <= '9') {
			// A numeric (decimal integer) array index.
			while (pos.stepEnd < pos.path.length() && '0' <= pos.path.charAt(pos.stepEnd)
					&& pos.path.charAt(pos.stepEnd) <= '9') {
				pos.stepEnd++;
			}

			segment = new XMPPathSegment(null, XMPPath.ARRAY_INDEX_STEP);
		} else {
			// Could be "[last()]" or one of the selector forms. Find the ']' or '='.
			while (pos.stepEnd < pos.path.length() && pos.path.charAt(pos.stepEnd) != ']'
					&& pos.path.charAt(pos.stepEnd) != '=') {
				pos.stepEnd++;
			}

			if (pos.stepEnd >= pos.path.length()) {
				throw new XMPException("Missing ']' or '=' for array index", XMPError.BADXPATH);
			}

			if (pos.path.charAt(pos.stepEnd) == ']') {
				if (!"[last()".equals(pos.path.substring(pos.stepBegin, pos.stepEnd))) {
					throw new XMPException("Invalid non-numeric array index", XMPError.BADXPATH);
				}
				segment = new XMPPathSegment(null, XMPPath.ARRAY_LAST_STEP);
			} else {
				pos.nameStart = pos.stepBegin + 1;
				pos.nameEnd = pos.stepEnd;
				pos.stepEnd++; // Absorb the '=', remember the quote.
				char quote = pos.path.charAt(pos.stepEnd);
				if (quote != '\'' && quote != '"') {
					throw new XMPException("Invalid quote in array selector", XMPError.BADXPATH);
				}

				pos.stepEnd++; // Absorb the leading quote.
				while (pos.stepEnd < pos.path.length()) {
					if (pos.path.charAt(pos.stepEnd) == quote) {
						// check for escaped quote
						if (pos.stepEnd + 1 >= pos.path.length() || pos.path.charAt(pos.stepEnd + 1) != quote) {
							break;
						}
						pos.stepEnd++;
					}
					pos.stepEnd++;
				}

				if (pos.stepEnd >= pos.path.length()) {
					throw new XMPException("No terminating quote for array selector", XMPError.BADXPATH);
				}
				pos.stepEnd++; // Absorb the trailing quote.

				// ! Touch up later, also changing '@' to '?'.
				segment = new XMPPathSegment(null, XMPPath.FIELD_SELECTOR_STEP);
			}
		}

		if (pos.stepEnd >= pos.path.length() || pos.path.charAt(pos.stepEnd) != ']') {
			throw new XMPException("Missing ']' for array index", XMPError.BADXPATH);
		}
		pos.stepEnd++;
		segment.setName(pos.path.substring(pos.stepBegin, pos.stepEnd));

		return segment;
	}

	private static void parseRootNode(String schemaNS, PathPosition pos, XMPPath expandedXPath) throws XMPException {
		while (pos.stepEnd < pos.path.length() && "/[*".indexOf(pos.path.charAt(pos.stepEnd)) < 0) {
			pos.stepEnd++;
		}

		if (pos.stepEnd == pos.stepBegin) {
			throw new XMPException("Empty initial XMPPath step", XMPError.BADXPATH);
		}

		String rootProp = verifyXPathRoot(schemaNS, pos.path.substring(pos.stepBegin, pos.stepEnd));
		XMPAliasInfo aliasInfo = XMPMetaFactory.getSchemaRegistry().findAlias(rootProp);
		if (aliasInfo == null) {
			// add schema xpath step
			expandedXPath.add(new XMPPathSegment(schemaNS, XMPPath.SCHEMA_NODE));
			XMPPathSegment rootStep = new XMPPathSegment(rootProp, XMPPath.STRUCT_FIELD_STEP);
			expandedXPath.add(rootStep);
		} else {
			// add schema xpath step and base step of alias
			expandedXPath.add(new XMPPathSegment(aliasInfo.getNamespace(), XMPPath.SCHEMA_NODE));
			XMPPathSegment rootStep = new XMPPathSegment(
					verifyXPathRoot(aliasInfo.getNamespace(), aliasInfo.getPropName()), XMPPath.STRUCT_FIELD_STEP);
			rootStep.setAlias(true);
			rootStep.setAliasForm(aliasInfo.getAliasForm().getOptions());
			expandedXPath.add(rootStep);

			if (aliasInfo.getAliasForm().isArrayAltText()) {
				XMPPathSegment qualSelectorStep = new XMPPathSegment("[?xml:lang='x-default']",
						XMPPath.QUAL_SELECTOR_STEP);
				qualSelectorStep.setAlias(true);
				qualSelectorStep.setAliasForm(aliasInfo.getAliasForm().getOptions());
				expandedXPath.add(qualSelectorStep);
			} else if (aliasInfo.getAliasForm().isArray()) {
				XMPPathSegment indexStep = new XMPPathSegment("[1]", XMPPath.ARRAY_INDEX_STEP);
				indexStep.setAlias(true);
				indexStep.setAliasForm(aliasInfo.getAliasForm().getOptions());
				expandedXPath.add(indexStep);
			}
		}
	}

	private static void verifyQualName(String qualName) throws XMPException {
		int colonPos = qualName.indexOf(':');
		if (colonPos > 0) {
			String prefix = qualName.substring(0, colonPos);
			if (Utils.isXMLNameNS(prefix)) {
				String regURI = XMPMetaFactory.getSchemaRegistry().getNamespaceURI(prefix);
				if (regURI != null) {
					return;
				}

				throw new XMPException("Unknown namespace prefix for qualified name", XMPError.BADXPATH);
			}
		}

		throw new XMPException("Ill-formed qualified name", XMPError.BADXPATH);
	}

	private static void verifySimpleXMLName(String name) throws XMPException {
		if (!Utils.isXMLName(name)) {
			throw new XMPException("Bad XML name", XMPError.BADXPATH);
		}
	}

	private static String verifyXPathRoot(String schemaNS, String rootProp) throws XMPException {
		// Do some basic checks on the URI and name. Try to lookup the URI. See if the
		// name is
		// qualified.

		if (schemaNS == null || schemaNS.length() == 0) {
			throw new XMPException("Schema namespace URI is required", XMPError.BADSCHEMA);
		}

		if ((rootProp.charAt(0) == '?') || (rootProp.charAt(0) == '@')) {
			throw new XMPException("Top level name must not be a qualifier", XMPError.BADXPATH);
		}

		if (rootProp.indexOf('/') >= 0 || rootProp.indexOf('[') >= 0) {
			throw new XMPException("Top level name must be simple", XMPError.BADXPATH);
		}

		String prefix = XMPMetaFactory.getSchemaRegistry().getNamespacePrefix(schemaNS);
		if (prefix == null) {
			throw new XMPException("Unregistered schema namespace URI", XMPError.BADSCHEMA);
		}

		// Verify the various URI and prefix combinations. Initialize the
		// expanded XMPPath.
		int colonPos = rootProp.indexOf(':');
		if (colonPos < 0) {
			// The propName is unqualified, use the schemaURI and associated
			// prefix.
			verifySimpleXMLName(rootProp); // Verify the part before any colon
			return prefix + rootProp;
		} else {
			// The propName is qualified. Make sure the prefix is legit. Use the associated
			// URI and
			// qualified name.

			// Verify the part before any colon
			verifySimpleXMLName(rootProp.substring(0, colonPos));
			verifySimpleXMLName(rootProp.substring(colonPos));

			prefix = rootProp.substring(0, colonPos + 1);

			String regPrefix = XMPMetaFactory.getSchemaRegistry().getNamespacePrefix(schemaNS);
			if (regPrefix == null) {
				throw new XMPException("Unknown schema namespace prefix", XMPError.BADSCHEMA);
			}
			if (!prefix.equals(regPrefix)) {
				throw new XMPException("Schema namespace URI and prefix mismatch", XMPError.BADSCHEMA);
			}

			return rootProp;
		}
	}
}

class PathPosition {

	public String path = null;

	int nameStart = 0;

	int nameEnd = 0;

	int stepBegin = 0;

	int stepEnd = 0;

}