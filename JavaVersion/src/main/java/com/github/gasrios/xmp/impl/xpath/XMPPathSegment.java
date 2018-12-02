// =================================================================================================
// ADOBE SYSTEMS INCORPORATED
// Copyright 2006 Adobe Systems Incorporated
// All Rights Reserved
//
// NOTICE:  Adobe permits you to use, modify, and distribute this file in accordance with the terms
// of the Adobe license agreement accompanying it.
// =================================================================================================

package com.github.gasrios.xmp.impl.xpath;

public class XMPPathSegment
{

	private String name;

	private int kind;

	private boolean alias;

	private int aliasForm;

	public XMPPathSegment(String name)
	{
		this.name = name;
	}

	public XMPPathSegment(String name, int kind)
	{
		this.name = name;
		this.kind = kind;
	}

	public int getKind()
	{
		return kind;
	}

	public void setKind(int kind)
	{
		this.kind = kind;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public void setAlias(boolean alias)
	{
		this.alias = alias;
	}

	public boolean isAlias()
	{
		return alias;
	}

	public int getAliasForm()
	{
		return aliasForm;
	}

	public void setAliasForm(int aliasForm)
	{
		this.aliasForm = aliasForm;
	}

	public String toString()
	{
		switch (kind)
		{
			case XMPPath.STRUCT_FIELD_STEP:
			case XMPPath.ARRAY_INDEX_STEP: 
			case XMPPath.QUALIFIER_STEP: 
			case XMPPath.ARRAY_LAST_STEP: 
				return name;
			case XMPPath.QUAL_SELECTOR_STEP: 
			case XMPPath.FIELD_SELECTOR_STEP: 
			return name;

		default:
			return name;
		}
	}

}