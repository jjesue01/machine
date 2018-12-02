// =================================================================================================
// ADOBE SYSTEMS INCORPORATED
// Copyright 2006 Adobe Systems Incorporated
// All Rights Reserved
//
// NOTICE:  Adobe permits you to use, modify, and distribute this file in accordance with the terms
// of the Adobe license agreement accompanying it.
// =================================================================================================

package com.github.gasrios.xmp.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;

import com.github.gasrios.xmp.XMPConst;
import com.github.gasrios.xmp.XMPError;
import com.github.gasrios.xmp.XMPException;
import com.github.gasrios.xmp.XMPSchemaRegistry;
import com.github.gasrios.xmp.options.AliasOptions;
import com.github.gasrios.xmp.properties.XMPAliasInfo;

public final class XMPSchemaRegistryImpl implements XMPSchemaRegistry, XMPConst {

	private Map<String, String> namespaceToPrefixMap = new HashMap<String, String>();

	private Map<String, String> prefixToNamespaceMap = new HashMap<String, String>();

	private Map<String, XMPAliasInfo> aliasMap = new HashMap<String, XMPAliasInfo>();

	private Pattern p = Pattern.compile("[/*?\\[\\]]");

	public XMPSchemaRegistryImpl() {
		try {
			registerStandardNamespaces();
			registerStandardAliases();
		} catch (XMPException e) {
			throw new RuntimeException("The XMPSchemaRegistry cannot be initialized!");
		}
	}

	public synchronized String registerNamespace(String namespaceURI, String suggestedPrefix) throws XMPException {
		ParameterAsserts.assertSchemaNS(namespaceURI);
		ParameterAsserts.assertPrefix(suggestedPrefix);
		if (suggestedPrefix.charAt(suggestedPrefix.length() - 1) != ':') suggestedPrefix += ':';
		if (!Utils.isXMLNameNS(suggestedPrefix.substring(0, suggestedPrefix.length() - 1)))
			throw new XMPException("The prefix is a bad XML name", XMPError.BADXML);
		String registeredPrefix = (String) namespaceToPrefixMap.get(namespaceURI);
		String registeredNS = (String) prefixToNamespaceMap.get(suggestedPrefix);
		if (registeredPrefix != null) return registeredPrefix;
		else {
			if (registeredNS != null) {
				String generatedPrefix = suggestedPrefix;
				for (int i = 1; prefixToNamespaceMap.containsKey(generatedPrefix); i++)
					generatedPrefix = suggestedPrefix.substring(0, suggestedPrefix.length() - 1) + "_" + i + "_:";
				suggestedPrefix = generatedPrefix;
			}
			prefixToNamespaceMap.put(suggestedPrefix, namespaceURI);
			namespaceToPrefixMap.put(namespaceURI, suggestedPrefix);
			return suggestedPrefix;
		}
	}

	public synchronized void deleteNamespace(String namespaceURI) {
		String prefixToDelete = getNamespacePrefix(namespaceURI);
		if (prefixToDelete != null) {
			namespaceToPrefixMap.remove(namespaceURI);
			prefixToNamespaceMap.remove(prefixToDelete);
		}
	}

	public synchronized String getNamespacePrefix(String namespaceURI) {
		return (String) namespaceToPrefixMap.get(namespaceURI);
	}

	public synchronized String getNamespaceURI(String namespacePrefix) {
		if (namespacePrefix != null && !namespacePrefix.endsWith(":")) namespacePrefix += ":";
		return prefixToNamespaceMap.get(namespacePrefix);
	}

	public synchronized Map<String, String> getNamespaces() {
		return Collections.unmodifiableMap(new TreeMap<String, String>(namespaceToPrefixMap));
	}

	public synchronized Map<String, String> getPrefixes() {
		return Collections.unmodifiableMap(new TreeMap<String, String>(prefixToNamespaceMap));
	}

	private void registerStandardNamespaces() throws XMPException {
		registerNamespace(NS_XML, "xml");
		registerNamespace(NS_RDF, "rdf");
		registerNamespace(NS_DC, "dc");
		registerNamespace(NS_IPTCCORE, "Iptc4xmpCore");
		registerNamespace(NS_IPTCEXT, "Iptc4xmpExt");
		registerNamespace(NS_DICOM, "DICOM");
		registerNamespace(NS_PLUS, "plus");
		registerNamespace(NS_X, "x");
		registerNamespace(NS_IX, "iX");
		registerNamespace(NS_XMP, "xmp");
		registerNamespace(NS_XMP_RIGHTS, "xmpRights");
		registerNamespace(NS_XMP_MM, "xmpMM");
		registerNamespace(NS_XMP_BJ, "xmpBJ");
		registerNamespace(NS_XMP_NOTE, "xmpNote");
		registerNamespace(NS_PDF, "pdf");
		registerNamespace(NS_PDFX, "pdfx");
		registerNamespace(NS_PDFX_ID, "pdfxid");
		registerNamespace(NS_PDFA_SCHEMA, "pdfaSchema");
		registerNamespace(NS_PDFA_PROPERTY, "pdfaProperty");
		registerNamespace(NS_PDFA_TYPE, "pdfaType");
		registerNamespace(NS_PDFA_FIELD, "pdfaField");
		registerNamespace(NS_PDFA_ID, "pdfaid");
		registerNamespace(NS_PDFA_EXTENSION, "pdfaExtension");
		registerNamespace(NS_PHOTOSHOP, "photoshop");
		registerNamespace(NS_PSALBUM, "album");
		registerNamespace(NS_EXIF, "exif");
		registerNamespace(NS_EXIFX, "exifEX");
		registerNamespace(NS_EXIF_AUX, "aux");
		registerNamespace(NS_TIFF, "tiff");
		registerNamespace(NS_PNG, "png");
		registerNamespace(NS_JPEG, "jpeg");
		registerNamespace(NS_JP2K, "jp2k");
		registerNamespace(NS_CAMERARAW, "crs");
		registerNamespace(NS_ADOBESTOCKPHOTO, "bmsp");
		registerNamespace(NS_CREATOR_ATOM, "creatorAtom");
		registerNamespace(NS_ASF, "asf");
		registerNamespace(NS_WAV, "wav");
		registerNamespace(NS_BWF, "bext");
		registerNamespace(NS_RIFFINFO, "riffinfo");
		registerNamespace(NS_SCRIPT, "xmpScript");
		registerNamespace(NS_TXMP, "txmp");
		registerNamespace(NS_SWF, "swf");
		registerNamespace(NS_DM, "xmpDM");
		registerNamespace(NS_TRANSIENT, "xmpx");
		registerNamespace(TYPE_TEXT, "xmpT");
		registerNamespace(TYPE_PAGEDFILE, "xmpTPg");
		registerNamespace(TYPE_GRAPHICS, "xmpG");
		registerNamespace(TYPE_IMAGE, "xmpGImg");
		registerNamespace(TYPE_FONT, "stFnt");
		registerNamespace(TYPE_DIMENSIONS, "stDim");
		registerNamespace(TYPE_RESOURCEEVENT, "stEvt");
		registerNamespace(TYPE_RESOURCEREF, "stRef");
		registerNamespace(TYPE_ST_VERSION, "stVer");
		registerNamespace(TYPE_ST_JOB, "stJob");
		registerNamespace(TYPE_MANIFESTITEM, "stMfs");
		registerNamespace(TYPE_IDENTIFIERQUAL, "xmpidq");
	}

	public synchronized XMPAliasInfo resolveAlias(String aliasNS, String aliasProp) {
		String aliasPrefix = getNamespacePrefix(aliasNS);
		return aliasPrefix == null? null : (XMPAliasInfo) aliasMap.get(aliasPrefix + aliasProp);
	}

	public synchronized XMPAliasInfo findAlias(String qname) { return (XMPAliasInfo) aliasMap.get(qname); }

	public synchronized XMPAliasInfo[] findAliases(String aliasNS) {
		String prefix = getNamespacePrefix(aliasNS);
		List<XMPAliasInfo> result = new ArrayList<XMPAliasInfo>();
		if (prefix != null) for (Iterator<String> it = aliasMap.keySet().iterator(); it.hasNext();) {
			String qname = it.next();
			if (qname.startsWith(prefix)) result.add(findAlias(qname));
		}
		return (XMPAliasInfo[]) result.toArray(new XMPAliasInfo[result.size()]);
	}

	synchronized void registerAlias(
		String aliasNS,
		String aliasProp,
		final String actualNS,
		final String actualProp,
		final AliasOptions aliasForm)
	throws XMPException {
		ParameterAsserts.assertSchemaNS(aliasNS);
		ParameterAsserts.assertPropName(aliasProp);
		ParameterAsserts.assertSchemaNS(actualNS);
		ParameterAsserts.assertPropName(actualProp);
		final AliasOptions aliasOpts = aliasForm != null
			? new AliasOptions(XMPNodeUtils.verifySetOptions(aliasForm.toPropertyOptions(), null).getOptions())
			: new AliasOptions();
		if (p.matcher(aliasProp).find() || p.matcher(actualProp).find())
			throw new XMPException("Alias and actual property names must be simple", XMPError.BADXPATH);
		final String aliasPrefix = getNamespacePrefix(aliasNS);
		final String actualPrefix = getNamespacePrefix(actualNS);
		if (aliasPrefix == null) throw new XMPException("Alias namespace is not registered", XMPError.BADSCHEMA);
		else if (actualPrefix == null) throw new XMPException("Actual namespace is not registered", XMPError.BADSCHEMA);
		String key = aliasPrefix + aliasProp;
		if (aliasMap.containsKey(key)) throw new XMPException("Alias is already existing", XMPError.BADPARAM);
		else if (aliasMap.containsKey(actualPrefix + actualProp))
			throw new XMPException("Actual property is already an alias, use the base property", XMPError.BADPARAM);
		XMPAliasInfo aliasInfo = new XMPAliasInfo() {
			public String getNamespace() {
				return actualNS;
			}
			public String getPrefix() {
				return actualPrefix;
			}
			public String getPropName() {
				return actualProp;
			}
			public AliasOptions getAliasForm() {
				return aliasOpts;
			}
			public String toString() {
				return actualPrefix + actualProp + " NS(" + actualNS + "), FORM (" + getAliasForm() + ")";
			}
		};
		aliasMap.put(key, aliasInfo);
	}

	public synchronized Map<String, XMPAliasInfo> getAliases() {
		return Collections.unmodifiableMap(new TreeMap<String, XMPAliasInfo>(aliasMap));
	}

	private void registerStandardAliases() throws XMPException {
		AliasOptions aliasToArrayOrdered = new AliasOptions().setArrayOrdered(true);
		AliasOptions aliasToArrayAltText = new AliasOptions().setArrayAltText(true);
		registerAlias(NS_XMP, "Author", NS_DC, "creator", aliasToArrayOrdered);
		registerAlias(NS_XMP, "Authors", NS_DC, "creator", null);
		registerAlias(NS_XMP, "Description", NS_DC, "description", null);
		registerAlias(NS_XMP, "Format", NS_DC, "format", null);
		registerAlias(NS_XMP, "Keywords", NS_DC, "subject", null);
		registerAlias(NS_XMP, "Locale", NS_DC, "language", null);
		registerAlias(NS_XMP, "Title", NS_DC, "title", null);
		registerAlias(NS_XMP_RIGHTS, "Copyright", NS_DC, "rights", null);
		registerAlias(NS_PDF, "Author", NS_DC, "creator", aliasToArrayOrdered);
		registerAlias(NS_PDF, "BaseURL", NS_XMP, "BaseURL", null);
		registerAlias(NS_PDF, "CreationDate", NS_XMP, "CreateDate", null);
		registerAlias(NS_PDF, "Creator", NS_XMP, "CreatorTool", null);
		registerAlias(NS_PDF, "ModDate", NS_XMP, "ModifyDate", null);
		registerAlias(NS_PDF, "Subject", NS_DC, "description", aliasToArrayAltText);
		registerAlias(NS_PDF, "Title", NS_DC, "title", aliasToArrayAltText);
		registerAlias(NS_PHOTOSHOP, "Author", NS_DC, "creator", aliasToArrayOrdered);
		registerAlias(NS_PHOTOSHOP, "Caption", NS_DC, "description", aliasToArrayAltText);
		registerAlias(NS_PHOTOSHOP, "Copyright", NS_DC, "rights", aliasToArrayAltText);
		registerAlias(NS_PHOTOSHOP, "Keywords", NS_DC, "subject", null);
		registerAlias(NS_PHOTOSHOP, "Marked", NS_XMP_RIGHTS, "Marked", null);
		registerAlias(NS_PHOTOSHOP, "Title", NS_DC, "title", aliasToArrayAltText);
		registerAlias(NS_PHOTOSHOP, "WebStatement", NS_XMP_RIGHTS, "WebStatement", null);
		registerAlias(NS_TIFF, "Artist", NS_DC, "creator", aliasToArrayOrdered);
		registerAlias(NS_TIFF, "Copyright", NS_DC, "rights", null);
		registerAlias(NS_TIFF, "DateTime", NS_XMP, "ModifyDate", null);
		registerAlias(NS_TIFF, "ImageDescription", NS_DC, "description", null);
		registerAlias(NS_TIFF, "Software", NS_XMP, "CreatorTool", null);
		registerAlias(NS_PNG, "Author", NS_DC, "creator", aliasToArrayOrdered);
		registerAlias(NS_PNG, "Copyright", NS_DC, "rights", aliasToArrayAltText);
		registerAlias(NS_PNG, "CreationTime", NS_XMP, "CreateDate", null);
		registerAlias(NS_PNG, "Description", NS_DC, "description", aliasToArrayAltText);
		registerAlias(NS_PNG, "ModificationTime", NS_XMP, "ModifyDate", null);
		registerAlias(NS_PNG, "Software", NS_XMP, "CreatorTool", null);
		registerAlias(NS_PNG, "Title", NS_DC, "title", aliasToArrayAltText);
	}

}