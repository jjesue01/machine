// =================================================================================================
// ADOBE SYSTEMS INCORPORATED
// Copyright 2006 Adobe Systems Incorporated
// All Rights Reserved
//
// NOTICE:  Adobe permits you to use, modify, and distribute this file in accordance with the terms
// of the Adobe license agreement accompanying it.
// =================================================================================================

package com.github.gasrios.xmp.properties;

import com.github.gasrios.xmp.options.AliasOptions;

public interface XMPAliasInfo {

	String getNamespace();

	String getPrefix();

	String getPropName();

	AliasOptions getAliasForm();

}