//=================================================================================================
// ADOBE SYSTEMS INCORPORATED
// Copyright 2006 Adobe Systems Incorporated
// All Rights Reserved
//
// NOTICE:  Adobe permits you to use, modify, and distribute this file in accordance with the terms
// of the Adobe license agreement accompanying it.
// =================================================================================================

package com.github.gasrios.xmp.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Vector;

import com.github.gasrios.xmp.XMPConst;
import com.github.gasrios.xmp.XMPError;
import com.github.gasrios.xmp.XMPException;
import com.github.gasrios.xmp.options.PropertyOptions;

class XMPNode implements Comparable<XMPNode> {

	private static final ListIterator<XMPNode> LIST_ITERATOR = new Vector<XMPNode>().listIterator();

	private String name;

	private String value;

	private XMPNode parent;

	private List<XMPNode> children = null;

	private List<XMPNode> qualifier = null;

	private PropertyOptions options = null;

	private boolean implicit;

	private boolean hasAliases;

	private boolean alias;

	private boolean hasValueChild;

	public XMPNode(String name, String value, PropertyOptions options) {
		this.name = name;
		this.value = value;
		this.options = options;
	}

	public XMPNode(String name, PropertyOptions options) { this(name, null, options); }

	public void clear() {
		options = null;
		name = null;
		value = null;
		children = null;
		qualifier = null;
	}

	public XMPNode getParent() { return parent; }

	public XMPNode getChild(int index) { return (XMPNode) getChildren().get(index - 1); }

	public void addChild(XMPNode node) throws XMPException {
		assertChildNotExisting(node.getName());
		node.setParent(this);
		getChildren().add(node);
	}

	public void addChild(int index, XMPNode node) throws XMPException {
		assertChildNotExisting(node.getName());
		node.setParent(this);
		getChildren().add(index - 1, node);
	}

	public void replaceChild(int index, XMPNode node) {
		node.setParent(this);
		getChildren().set(index - 1, node);
	}

	public void removeChild(int itemIndex) {
		getChildren().remove(itemIndex - 1);
		cleanupChildren();
	}

	public void removeChild(XMPNode node) {
		getChildren().remove(node);
		cleanupChildren();
	}

	protected void cleanupChildren() { if (children.isEmpty()) children = null; }

	public void removeChildren() { children = null; }

	public int getChildrenLength() { return children != null ? children.size() : 0; }

	public XMPNode findChildByName(String expr) { return find(getChildren(), expr); }

	public XMPNode getQualifier(int index) { return (XMPNode) getQualifier().get(index - 1); }

	public int getQualifierLength() { return qualifier != null ? qualifier.size() : 0; }

	public void addQualifier(XMPNode qualNode) throws XMPException {
		assertQualifierNotExisting(qualNode.getName());
		qualNode.setParent(this);
		qualNode.getOptions().setQualifier(true);
		getOptions().setHasQualifiers(true);
		if (qualNode.isLanguageNode()) {
			options.setHasLanguage(true);
			getQualifier().add(0, qualNode);
		} else if (qualNode.isTypeNode()) {
			options.setHasType(true);
			getQualifier().add(!options.getHasLanguage() ? 0 : 1, qualNode);
		} else getQualifier().add(qualNode);
	}

	public void removeQualifier(XMPNode qualNode) {
		PropertyOptions opts = getOptions();
		if (qualNode.isLanguageNode()) opts.setHasLanguage(false);
		else if (qualNode.isTypeNode()) opts.setHasType(false);
		getQualifier().remove(qualNode);
		if (qualifier.isEmpty()) {
			opts.setHasQualifiers(false);
			qualifier = null;
		}
	}

	public void removeQualifiers() {
		PropertyOptions opts = getOptions();
		opts.setHasQualifiers(false);
		opts.setHasLanguage(false);
		opts.setHasType(false);
		qualifier = null;
	}

	public XMPNode findQualifierByName(String expr) { return find(qualifier, expr); }

	public boolean hasChildren() { return children != null && children.size() > 0; }

	public Iterator<XMPNode> iterateChildren() {
		if (children != null) return getChildren().iterator();
		else return LIST_ITERATOR;
	}

	public boolean hasQualifier() { return qualifier != null && qualifier.size() > 0; }

	public Iterator<XMPNode> iterateQualifier() {
		if (qualifier == null) return LIST_ITERATOR;
		else {
			final Iterator<XMPNode> it = getQualifier().iterator();
			return new Iterator<XMPNode>() {
				public boolean hasNext() { return it.hasNext(); }
				public XMPNode next() { return it.next(); }
				public void remove() {
					throw new UnsupportedOperationException("remove() is not allowed due to the internal contraints");
				}
			};
		}
	}

	public XMPNode clone() {
		PropertyOptions newOptions;
		try { newOptions = new PropertyOptions(getOptions().getOptions()); }
		catch (XMPException e) { newOptions = new PropertyOptions(); }
		XMPNode newNode = new XMPNode(name, value, newOptions);
		cloneSubtree(newNode);
		return newNode;
	}

	public void cloneSubtree(XMPNode destination) {
		try {
			for (Iterator<XMPNode> it = iterateChildren(); it.hasNext();) destination.addChild(it.next().clone());
			for (Iterator<XMPNode> it = iterateQualifier(); it.hasNext();) destination.addQualifier((XMPNode) it.next().clone());
		} catch (XMPException e) { assert false; }

	}

	public String dumpNode(boolean recursive) {
		StringBuffer result = new StringBuffer(512);
		this.dumpNode(result, recursive, 0, 0);
		return result.toString();
	}

	public int compareTo(XMPNode xmpNode) {
		if (getOptions().isSchemaNode()) return this.value.compareTo(((XMPNode) xmpNode).getValue());
		else return this.name.compareTo(((XMPNode) xmpNode).getName());
	}

	public String getName() { return name; }

	public void setName(String name) { this.name = name; }

	public String getValue() { return value; }

	public void setValue(String value) { this.value = value; }

	public PropertyOptions getOptions() {
		if (options == null) options = new PropertyOptions();
		return options;
	}

	public void setOptions(PropertyOptions options) { this.options = options; }

	public boolean isImplicit() { return implicit; }

	public void setImplicit(boolean implicit) { this.implicit = implicit; }

	public boolean getHasAliases() { return hasAliases; }

	public void setHasAliases(boolean hasAliases) { this.hasAliases = hasAliases; }

	public boolean isAlias() { return alias; }

	public void setAlias(boolean alias) { this.alias = alias; }

	public boolean getHasValueChild() { return hasValueChild; }

	public void setHasValueChild(boolean hasValueChild) { this.hasValueChild = hasValueChild; }

	public void sort() {
		if (hasQualifier()) {
			XMPNode[] quals = (XMPNode[]) getQualifier().toArray(new XMPNode[getQualifierLength()]);
			int sortFrom = 0;
			while (
				quals.length > sortFrom && (
					XMPConst.XML_LANG.equals(quals[sortFrom].getName()) ||
					"rdf:type".equals(quals[sortFrom].getName())
				)
			) quals[sortFrom++].sort();
			Arrays.sort(quals, sortFrom, quals.length);
			ListIterator<XMPNode> it = qualifier.listIterator();
			for (int j = 0; j < quals.length; j++) {
				it.next();
				it.set(quals[j]);
				quals[j].sort();
			}
		}
		if (hasChildren()) {
			if (!getOptions().isArray()) Collections.sort(children);
			for (Iterator<XMPNode> it = iterateChildren(); it.hasNext();) it.next().sort();
		}
	}

	private void dumpNode(StringBuffer result, boolean recursive, int indent, int index) {
		for (int i = 0; i < indent; i++) result.append('\t');
		if (parent != null)
			if (getOptions().isQualifier()) {
				result.append('?');
				result.append(name);
			} else if (getParent().getOptions().isArray()) {
				result.append('[');
				result.append(index);
				result.append(']');
			} else result.append(name);
		else {
			result.append("ROOT NODE");
			if (name != null && name.length() > 0) {
				result.append(" (");
				result.append(name);
				result.append(')');
			}
		}
		if (value != null && value.length() > 0) {
			result.append(" = \"");
			result.append(value);
			result.append('"');
		}
		if (getOptions().containsOneOf(0xffffffff)) {
			result.append("\t(");
			result.append(getOptions().toString());
			result.append(" : ");
			result.append(getOptions().getOptionsString());
			result.append(')');
		}
		result.append('\n');
		if (recursive && hasQualifier()) {
			XMPNode[] quals = (XMPNode[]) getQualifier().toArray(new XMPNode[getQualifierLength()]);
			int i = 0;
			while (quals.length > i && (XMPConst.XML_LANG.equals(quals[i].getName()) || "rdf:type".equals(quals[i].getName()))) i++;
			Arrays.sort(quals, i, quals.length);
			for (i = 0; i < quals.length; i++) quals[i].dumpNode(result, recursive, indent + 2, i + 1);
		}
		if (recursive && hasChildren()) {
			XMPNode[] children = (XMPNode[]) getChildren().toArray(new XMPNode[getChildrenLength()]);
			if (!getOptions().isArray()) Arrays.sort(children);
			for (int i = 0; i < children.length; i++) children[i].dumpNode(result, recursive, indent + 1, i + 1);
		}
	}

	private boolean isLanguageNode() { return XMPConst.XML_LANG.equals(name); }

	private boolean isTypeNode() { return "rdf:type".equals(name); }

	private List<XMPNode> getChildren() { return children == null? children = new ArrayList<XMPNode>(0) : children; }

	public List<XMPNode> getUnmodifiableChildren() { return Collections.unmodifiableList(new ArrayList<XMPNode>(getChildren())); }

	private List<XMPNode> getQualifier() { return qualifier == null? qualifier = new ArrayList<XMPNode>(0) : qualifier; }

	protected void setParent(XMPNode parent) { this.parent = parent; }

	private XMPNode find(List<XMPNode> list, String expr) {
		if (list != null) for (Iterator<XMPNode> it = list.iterator(); it.hasNext();) {
			XMPNode child = it.next();
			if (child.getName().equals(expr)) return child;
		}
		return null;
	}

	private void assertChildNotExisting(String childName) throws XMPException {
		if (!XMPConst.ARRAY_ITEM_NAME.equals(childName) && findChildByName(childName) != null)
			throw new XMPException("Duplicate property or field node '" + childName + "'", XMPError.BADXMP);
	}

	private void assertQualifierNotExisting(String qualifierName) throws XMPException {
		if (!XMPConst.ARRAY_ITEM_NAME.equals(qualifierName) && findQualifierByName(qualifierName) != null)
			throw new XMPException("Duplicate '" + qualifierName + "' qualifier", XMPError.BADXMP);
	}

}