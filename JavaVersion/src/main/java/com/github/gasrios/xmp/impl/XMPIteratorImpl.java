// =================================================================================================
// ADOBE SYSTEMS INCORPORATED
// Copyright 2006 Adobe Systems Incorporated
// All Rights Reserved
//
// NOTICE:  Adobe permits you to use, modify, and distribute this file in accordance with the terms
// of the Adobe license agreement accompanying it.
// =================================================================================================

package com.github.gasrios.xmp.impl;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Vector;

import com.github.gasrios.xmp.XMPError;
import com.github.gasrios.xmp.XMPException;
import com.github.gasrios.xmp.XMPIterator;
import com.github.gasrios.xmp.XMPMetaFactory;
import com.github.gasrios.xmp.impl.xpath.XMPPath;
import com.github.gasrios.xmp.impl.xpath.XMPPathParser;
import com.github.gasrios.xmp.options.IteratorOptions;
import com.github.gasrios.xmp.options.PropertyOptions;
import com.github.gasrios.xmp.properties.XMPPropertyInfo;

public class XMPIteratorImpl implements XMPIterator {

	private static final Iterator<XMPPropertyInfo> ITERATOR = new Vector<XMPPropertyInfo>().iterator();

	private IteratorOptions options;

	private String baseNS = null;

	protected boolean skipSiblings = false;

	protected boolean skipSubtree = false;

	private Iterator<XMPPropertyInfo> nodeIterator = null;

	public XMPIteratorImpl(XMPMetaImpl xmp, String schemaNS, String propPath, IteratorOptions options) throws XMPException {
		this.options = options != null ? options : new IteratorOptions();
		XMPNode startNode = null;
		String initialPath = null;
		boolean baseSchema = schemaNS != null && schemaNS.length() > 0;
		boolean baseProperty = propPath != null && propPath.length() > 0;
		if (!baseSchema && !baseProperty) startNode = xmp.getRoot();
		else if (baseSchema && baseProperty) {
			XMPPath path = XMPPathParser.expandXPath(schemaNS, propPath);
			XMPPath basePath = new XMPPath();
			for (int i = 0; i < path.size() - 1; i++) basePath.add(path.getSegment(i));
			startNode = XMPNodeUtils.findNode(xmp.getRoot(), path, false, null);
			baseNS = schemaNS;
			initialPath = basePath.toString();
		} else if (baseSchema && !baseProperty) startNode = XMPNodeUtils.findSchemaNode(xmp.getRoot(), schemaNS, false);
		else throw new XMPException("Schema namespace URI is required", XMPError.BADSCHEMA);
		if (startNode != null)
			if (!this.options.isJustChildren()) nodeIterator = new NodeIterator(startNode, initialPath, 1);
			else nodeIterator = new NodeIteratorChildren(startNode, initialPath);
		else nodeIterator = ITERATOR;
	}

	public void skipSubtree() {
		this.skipSubtree = true;
	}

	public void skipSiblings() {
		skipSubtree();
		this.skipSiblings = true;
	}

	public boolean hasNext() {
		return nodeIterator.hasNext();
	}

	public XMPPropertyInfo next() {
		return nodeIterator.next();
	}

	public void remove() {
		throw new UnsupportedOperationException("The XMPIterator does not support remove().");
	}

	protected IteratorOptions getOptions() {
		return options;
	}

	protected String getBaseNS() {
		return baseNS;
	}

	protected void setBaseNS(String baseNS) {
		this.baseNS = baseNS;
	}

	private class NodeIterator implements Iterator<XMPPropertyInfo> {

		protected static final int ITERATE_NODE = 0;

		protected static final int ITERATE_CHILDREN = 1;

		protected static final int ITERATE_QUALIFIER = 2;

		private int state = ITERATE_NODE;

		private XMPNode visitedNode;

		private String path;

		private Iterator<XMPNode> childrenIterator = null;

		private int index = 0;

		private Iterator<XMPPropertyInfo> subIterator = new Vector<XMPPropertyInfo>().iterator();

		private XMPPropertyInfo returnProperty = null;

		public NodeIterator() {}

		public NodeIterator(XMPNode visitedNode, String parentPath, int index) {
			this.visitedNode = visitedNode;
			this.state = NodeIterator.ITERATE_NODE;
			if (visitedNode.getOptions().isSchemaNode()) setBaseNS(visitedNode.getName());
			path = accumulatePath(visitedNode, parentPath, index);
		}

		public boolean hasNext() {
			if (returnProperty != null) return true;
			if (state == ITERATE_NODE) return reportNode();
			else if (state == ITERATE_CHILDREN) {
				if (childrenIterator == null) childrenIterator = visitedNode.iterateChildren();
				boolean hasNext = iterateChildren(childrenIterator);
				if (!hasNext && visitedNode.hasQualifier() && !getOptions().isOmitQualifiers()) {
					state = ITERATE_QUALIFIER;
					childrenIterator = null;
					hasNext = hasNext();
				}
				return hasNext;
			} else {
				if (childrenIterator == null) childrenIterator = visitedNode.iterateQualifier();
				return iterateChildren(childrenIterator);
			}
		}

		protected boolean reportNode() {
			state = ITERATE_CHILDREN;
			if (visitedNode.getParent() != null && (!getOptions().isJustLeafnodes() || !visitedNode.hasChildren())) {
				returnProperty = createPropertyInfo(visitedNode, getBaseNS(), path);
				return true;
			} else return hasNext();
		}

		private boolean iterateChildren(Iterator<XMPNode> iterator) {
			if (skipSiblings) {
				skipSiblings = false;
				subIterator = ITERATOR;
			}
			if ((!subIterator.hasNext()) && iterator.hasNext()) subIterator = new NodeIterator(iterator.next(), path, ++index);
			if (subIterator.hasNext()) {
				returnProperty = (XMPPropertyInfo) subIterator.next();
				return true;
			} else return false;
		}

		public XMPPropertyInfo next() {
			if (hasNext()) {
				XMPPropertyInfo result = returnProperty;
				returnProperty = null;
				return result;
			} else throw new NoSuchElementException("There are no more nodes to return");
		}

		public void remove() {
			throw new UnsupportedOperationException();
		}

		protected String accumulatePath(XMPNode currNode, String parentPath, int currentIndex) {
			String separator;
			String segmentName;
			if (currNode.getParent() == null || currNode.getOptions().isSchemaNode()) return null;
			else if (currNode.getParent().getOptions().isArray()) {
				separator = "";
				segmentName = "[" + String.valueOf(currentIndex) + "]";
			} else {
				separator = "/";
				segmentName = currNode.getName();
			}
			if (parentPath == null || parentPath.length() == 0) return segmentName;
			else if (getOptions().isJustLeafname()) return !segmentName.startsWith("?") ? segmentName : segmentName.substring(1); // qualifier
			else return parentPath + separator + segmentName;
		}

		protected XMPPropertyInfo createPropertyInfo(final XMPNode node, final String baseNS, final String path) {
			final String value = node.getOptions().isSchemaNode() ? null : node.getValue();
			return new XMPPropertyInfo() {
				public String getNamespace() {
					return
						node.getOptions().isSchemaNode()?
							baseNS:
							XMPMetaFactory.getSchemaRegistry().getNamespaceURI(new QName(node.getName()).getPrefix());
				}
				public String getPath() {
					return path;
				}
				public String getValue() {
					return value;
				}
				public PropertyOptions getOptions() {
					return node.getOptions();
				}
				public String getLanguage() {
					return null;
				}
			};
		}

		protected XMPPropertyInfo getReturnProperty() {
			return returnProperty;
		}

		protected void setReturnProperty(XMPPropertyInfo returnProperty) {
			this.returnProperty = returnProperty;
		}

	}

	private class NodeIteratorChildren extends NodeIterator {

		private String parentPath;

		private Iterator<XMPNode> childrenIterator;

		private int index = 0;

		public NodeIteratorChildren(XMPNode parentNode, String parentPath) {
			if (parentNode.getOptions().isSchemaNode()) setBaseNS(parentNode.getName());
			this.parentPath = accumulatePath(parentNode, parentPath, 1);
			childrenIterator = parentNode.iterateChildren();
		}

		public boolean hasNext() {
			if (getReturnProperty() != null) return true;
			else if (skipSiblings) return false;
			else if (childrenIterator.hasNext()) {
				XMPNode child = (XMPNode) childrenIterator.next();
				index++;
				String path = null;
				if (child.getOptions().isSchemaNode()) setBaseNS(child.getName());
				else if (child.getParent() != null) path = accumulatePath(child, parentPath, index);
				if (!getOptions().isJustLeafnodes() || !child.hasChildren()) {
					setReturnProperty(createPropertyInfo(child, getBaseNS(), path));
					return true;
				} else return hasNext();
			} else return false;
		}

	}

}