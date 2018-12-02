// =================================================================================================
// ADOBE SYSTEMS INCORPORATED
// Copyright 2006 Adobe Systems Incorporated
// All Rights Reserved
//
// NOTICE:  Adobe permits you to use, modify, and distribute this file in accordance with the terms
// of the Adobe license agreement accompanying it.
// =================================================================================================

package com.github.gasrios.xmp;

import java.util.Map;

import com.github.gasrios.xmp.properties.XMPAliasInfo;

public interface XMPSchemaRegistry {

	String registerNamespace(String namespaceURI, String suggestedPrefix) throws XMPException;

	String getNamespacePrefix(String namespaceURI);

	String getNamespaceURI(String namespacePrefix);

	Map<String, String> getNamespaces();

	Map<String, String> getPrefixes();

	void deleteNamespace(String namespaceURI);

	XMPAliasInfo resolveAlias(String aliasNS, String aliasProp);

	XMPAliasInfo[] findAliases(String aliasNS);

	XMPAliasInfo findAlias(String qname);

	Map<String, XMPAliasInfo> getAliases();

}