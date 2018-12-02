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
import java.util.Iterator;
import java.util.List;

import org.w3c.dom.Attr;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.github.gasrios.xmp.XMPConst;
import com.github.gasrios.xmp.XMPError;
import com.github.gasrios.xmp.XMPException;
import com.github.gasrios.xmp.XMPMetaFactory;
import com.github.gasrios.xmp.XMPSchemaRegistry;
import com.github.gasrios.xmp.options.PropertyOptions;

public class ParseRDF implements XMPError, XMPConst {

	public static final int RDFTERM_OTHER = 0;

	public static final int RDFTERM_RDF = 1;

	public static final int RDFTERM_ID = 2;

	public static final int RDFTERM_ABOUT = 3;

	public static final int RDFTERM_PARSE_TYPE = 4;

	public static final int RDFTERM_RESOURCE = 5;

	public static final int RDFTERM_NODE_ID = 6;

	public static final int RDFTERM_DATATYPE = 7;

	public static final int RDFTERM_DESCRIPTION = 8;

	public static final int RDFTERM_LI = 9;

	public static final int RDFTERM_ABOUT_EACH = 10;

	public static final int RDFTERM_ABOUT_EACH_PREFIX = 11;

	public static final int RDFTERM_BAG_ID = 12;

	public static final int RDFTERM_FIRST_CORE = RDFTERM_RDF;

	public static final int RDFTERM_LAST_CORE = RDFTERM_DATATYPE;

	public static final int RDFTERM_FIRST_SYNTAX = RDFTERM_FIRST_CORE;

	public static final int RDFTERM_LAST_SYNTAX = RDFTERM_LI;

	public static final int RDFTERM_FIRST_OLD = RDFTERM_ABOUT_EACH;

	public static final int RDFTERM_LAST_OLD = RDFTERM_BAG_ID;

	public static final String DEFAULT_PREFIX = "_dflt";

	static XMPMetaImpl parse(Node xmlRoot) throws XMPException {
		XMPMetaImpl xmp = new XMPMetaImpl();
		rdf_RDF(xmp, xmlRoot);
		return xmp;
	}

	static void rdf_RDF(XMPMetaImpl xmp, Node rdfRdfNode) throws XMPException {
		if (rdfRdfNode.hasAttributes()) rdf_NodeElementList(xmp, xmp.getRoot(), rdfRdfNode);
		else throw new XMPException("Invalid attributes of rdf:RDF element", BADRDF);
	}

	private static void rdf_NodeElementList(XMPMetaImpl xmp, XMPNode xmpParent, Node rdfRdfNode) throws XMPException {
		for (int i = 0; i < rdfRdfNode.getChildNodes().getLength(); i++) {
			Node child = rdfRdfNode.getChildNodes().item(i);
			if (!isWhitespaceNode(child)) rdf_NodeElement(xmp, xmpParent, child, true);
		}
	}

	private static void rdf_NodeElement(XMPMetaImpl xmp, XMPNode xmpParent, Node xmlNode, boolean isTopLevel) throws XMPException {
		int nodeTerm = getRDFTermKind(xmlNode);
		if (nodeTerm != RDFTERM_DESCRIPTION && nodeTerm != RDFTERM_OTHER)
			throw new XMPException("Node element must be rdf:Description or typed node", BADRDF);
		else if (isTopLevel && nodeTerm == RDFTERM_OTHER) throw new XMPException("Top level typed node not allowed", BADXMP);
		else {
			rdf_NodeElementAttrs(xmp, xmpParent, xmlNode, isTopLevel);
			rdf_PropertyElementList(xmp, xmpParent, xmlNode, isTopLevel);
		}

	}

	private static void rdf_NodeElementAttrs(XMPMetaImpl xmp, XMPNode xmpParent, Node xmlNode, boolean isTopLevel) throws XMPException {
		int exclusiveAttrs = 0;
		for (int i = 0; i < xmlNode.getAttributes().getLength(); i++) {
			Node attribute = xmlNode.getAttributes().item(i);
			if ("xmlns".equals(attribute.getPrefix()) || (attribute.getPrefix() == null && "xmlns".equals(attribute.getNodeName()))) continue;
			int attrTerm = getRDFTermKind(attribute);
			switch (attrTerm) {
			case RDFTERM_ID:
			case RDFTERM_NODE_ID:
			case RDFTERM_ABOUT:
				if (exclusiveAttrs > 0) throw new XMPException("Mutally exclusive about, ID, nodeID attributes", BADRDF);
				exclusiveAttrs++;
				if (isTopLevel && (attrTerm == RDFTERM_ABOUT)) {
					if (xmpParent.getName() != null && xmpParent.getName().length() > 0) {
						if (!xmpParent.getName().equals(attribute.getNodeValue()))
							throw new XMPException("Mismatched top level rdf:about values", BADXMP);
					} else xmpParent.setName(attribute.getNodeValue());
				}
				break;
			case RDFTERM_OTHER:
				addChildNode(xmp, xmpParent, attribute, attribute.getNodeValue(), isTopLevel);
				break;
			default:
				throw new XMPException("Invalid nodeElement attribute", BADRDF);
			}
		}
	}

	private static void rdf_PropertyElementList(XMPMetaImpl xmp, XMPNode xmpParent, Node xmlParent, boolean isTopLevel) throws XMPException {
		for (int i = 0; i < xmlParent.getChildNodes().getLength(); i++) {
			Node currChild = xmlParent.getChildNodes().item(i);
			if (isWhitespaceNode(currChild)) continue;
			else if (currChild.getNodeType() != Node.ELEMENT_NODE) throw new XMPException("Expected property element node not found", BADRDF);
			else rdf_PropertyElement(xmp, xmpParent, currChild, isTopLevel);
		}
	}

	private static void rdf_PropertyElement(XMPMetaImpl xmp, XMPNode xmpParent, Node xmlNode, boolean isTopLevel) throws XMPException {
		if (!isPropertyElementName(getRDFTermKind(xmlNode))) throw new XMPException("Invalid property element name", BADRDF);
		NamedNodeMap attributes = xmlNode.getAttributes();
		List<String> nsAttrs = null;
		for (int i = 0; i < attributes.getLength(); i++) {
			Node attribute = attributes.item(i);
			if ("xmlns".equals(attribute.getPrefix()) || (attribute.getPrefix() == null && "xmlns".equals(attribute.getNodeName()))) {
				if (nsAttrs == null) nsAttrs = new ArrayList<String>();
				nsAttrs.add(attribute.getNodeName());
			}
		}
		if (nsAttrs != null) for (Iterator<String> it = nsAttrs.iterator(); it.hasNext();) attributes.removeNamedItem(it.next());
		if (attributes.getLength() > 3) rdf_EmptyPropertyElement(xmp, xmpParent, xmlNode, isTopLevel);
		else {
			for (int i = 0; i < attributes.getLength(); i++) {
				Node attribute = attributes.item(i);
				String attrLocal = attribute.getLocalName();
				String attrNS = attribute.getNamespaceURI();
				String attrValue = attribute.getNodeValue();
				if (!(XML_LANG.equals(attribute.getNodeName()) && !("ID".equals(attrLocal) && NS_RDF.equals(attrNS)))) {
					if ("datatype".equals(attrLocal) && NS_RDF.equals(attrNS)) rdf_LiteralPropertyElement(xmp, xmpParent, xmlNode, isTopLevel);
					else if (!("parseType".equals(attrLocal) && NS_RDF.equals(attrNS))) rdf_EmptyPropertyElement(xmp, xmpParent, xmlNode, isTopLevel);
					else if ("Literal".equals(attrValue)) rdf_ParseTypeLiteralPropertyElement();
					else if ("Resource".equals(attrValue)) rdf_ParseTypeResourcePropertyElement(xmp, xmpParent, xmlNode, isTopLevel);
					else if ("Collection".equals(attrValue)) rdf_ParseTypeCollectionPropertyElement();
					else rdf_ParseTypeOtherPropertyElement();
					return;
				}
			}
			if (xmlNode.hasChildNodes()) {
				for (int i = 0; i < xmlNode.getChildNodes().getLength(); i++) if (xmlNode.getChildNodes().item(i).getNodeType() != Node.TEXT_NODE) {
					rdf_ResourcePropertyElement(xmp, xmpParent, xmlNode, isTopLevel);
					return;
				}
				rdf_LiteralPropertyElement(xmp, xmpParent, xmlNode, isTopLevel);
			} else rdf_EmptyPropertyElement(xmp, xmpParent, xmlNode, isTopLevel);
		}
	}

	private static void rdf_ResourcePropertyElement(XMPMetaImpl xmp, XMPNode xmpParent, Node xmlNode, boolean isTopLevel) throws XMPException {
		if (isTopLevel && "iX:changes".equals(xmlNode.getNodeName())) return;
		XMPNode newCompound = addChildNode(xmp, xmpParent, xmlNode, "", isTopLevel);
		for (int i = 0; i < xmlNode.getAttributes().getLength(); i++) {
			Node attribute = xmlNode.getAttributes().item(i);
			if ("xmlns".equals(attribute.getPrefix()) || (attribute.getPrefix() == null && "xmlns".equals(attribute.getNodeName()))) continue;
			String attrLocal = attribute.getLocalName();
			String attrNS = attribute.getNamespaceURI();
			if (XML_LANG.equals(attribute.getNodeName())) addQualifierNode(newCompound, XML_LANG, attribute.getNodeValue());
			else if ("ID".equals(attrLocal) && NS_RDF.equals(attrNS)) continue;
			else throw new XMPException("Invalid attribute for resource property element", BADRDF);
		}
		Node currChild = null;
		boolean found = false;
		int i;
		for (i = 0; i < xmlNode.getChildNodes().getLength(); i++) {
			currChild = xmlNode.getChildNodes().item(i);
			if (!isWhitespaceNode(currChild)) {
				if (currChild.getNodeType() == Node.ELEMENT_NODE && !found) {
					boolean isRDF = NS_RDF.equals(currChild.getNamespaceURI());
					String childLocal = currChild.getLocalName();
					if (isRDF && "Bag".equals(childLocal)) newCompound.getOptions().setArray(true);
					else if (isRDF && "Seq".equals(childLocal)) newCompound.getOptions().setArray(true).setArrayOrdered(true);
					else if (isRDF && "Alt".equals(childLocal)) newCompound.getOptions().setArray(true).setArrayOrdered(true).setArrayAlternate(true);
					else {
						newCompound.getOptions().setStruct(true);
						if (!isRDF && !"Description".equals(childLocal)) {
							String typeName = currChild.getNamespaceURI();
							if (typeName == null) throw new XMPException("All XML elements must be in a namespace", BADXMP);
							typeName += ':' + childLocal;
							addQualifierNode(newCompound, "rdf:type", typeName);
						}
					}
					rdf_NodeElement(xmp, newCompound, currChild, false);
					if (newCompound.getHasValueChild()) fixupQualifiedNode(newCompound);
					else if (newCompound.getOptions().isArrayAlternate()) XMPNodeUtils.detectAltText(newCompound);
					found = true;
				}
				else if (found) throw new XMPException("Invalid child of resource property element", BADRDF);
				else throw new XMPException("Children of resource property element must be XML elements", BADRDF);
			}
		}
		if (!found) throw new XMPException("Missing child of resource property element", BADRDF);
	}

	private static void rdf_LiteralPropertyElement(XMPMetaImpl xmp, XMPNode xmpParent, Node xmlNode, boolean isTopLevel) throws XMPException {
		XMPNode newChild = addChildNode(xmp, xmpParent, xmlNode, null, isTopLevel);
		for (int i = 0; i < xmlNode.getAttributes().getLength(); i++) {
			Node attribute = xmlNode.getAttributes().item(i);
			if ("xmlns".equals(attribute.getPrefix()) || (attribute.getPrefix() == null && "xmlns".equals(attribute.getNodeName()))) continue;
			String attrLocal = attribute.getLocalName();
			if (XML_LANG.equals(attribute.getNodeName())) addQualifierNode(newChild, XML_LANG, attribute.getNodeValue());
			else if (NS_RDF.equals(attribute.getNamespaceURI()) && ("ID".equals(attrLocal) || "datatype".equals(attrLocal))) continue;
			else throw new XMPException("Invalid attribute for literal property element", BADRDF);
		}
		String textValue = "";
		for (int i = 0; i < xmlNode.getChildNodes().getLength(); i++) {
			Node child = xmlNode.getChildNodes().item(i);
			if (child.getNodeType() == Node.TEXT_NODE) textValue += child.getNodeValue();
			else throw new XMPException("Invalid child of literal property element", BADRDF);
		}
		newChild.setValue(textValue);
	}

	private static void rdf_ParseTypeLiteralPropertyElement() throws XMPException {
		throw new XMPException("ParseTypeLiteral property element not allowed", BADXMP);
	}

	private static void rdf_ParseTypeResourcePropertyElement(XMPMetaImpl xmp, XMPNode xmpParent, Node xmlNode, boolean isTopLevel) throws XMPException {
		XMPNode newStruct = addChildNode(xmp, xmpParent, xmlNode, "", isTopLevel);
		newStruct.getOptions().setStruct(true);
		for (int i = 0; i < xmlNode.getAttributes().getLength(); i++) {
			Node attribute = xmlNode.getAttributes().item(i);
			if ("xmlns".equals(attribute.getPrefix()) || (attribute.getPrefix() == null && "xmlns".equals(attribute.getNodeName()))) continue;
			String attrLocal = attribute.getLocalName();
			if (XML_LANG.equals(attribute.getNodeName())) addQualifierNode(newStruct, XML_LANG, attribute.getNodeValue());
			else if (NS_RDF.equals(attribute.getNamespaceURI()) && ("ID".equals(attrLocal) || "parseType".equals(attrLocal))) continue;
			else throw new XMPException("Invalid attribute for ParseTypeResource property element", BADRDF);
		}
		rdf_PropertyElementList(xmp, newStruct, xmlNode, false);
		if (newStruct.getHasValueChild()) fixupQualifiedNode(newStruct);
	}

	private static void rdf_ParseTypeCollectionPropertyElement() throws XMPException {
		throw new XMPException("ParseTypeCollection property element not allowed", BADXMP);
	}

	private static void rdf_ParseTypeOtherPropertyElement() throws XMPException {
		throw new XMPException("ParseTypeOther property element not allowed", BADXMP);
	}

	private static void rdf_EmptyPropertyElement(XMPMetaImpl xmp, XMPNode xmpParent, Node xmlNode, boolean isTopLevel) throws XMPException {
		boolean hasPropertyAttrs = false;
		boolean hasResourceAttr = false;
		boolean hasNodeIDAttr = false;
		boolean hasValueAttr = false;
		Node valueNode = null;
		if (xmlNode.hasChildNodes()) throw new XMPException("Nested content not allowed with rdf:resource or property attributes", BADRDF);
		for (int i = 0; i < xmlNode.getAttributes().getLength(); i++) {
			Node attribute = xmlNode.getAttributes().item(i);
			if ("xmlns".equals(attribute.getPrefix()) || (attribute.getPrefix() == null && "xmlns".equals(attribute.getNodeName()))) continue;
			switch (getRDFTermKind(attribute)) {
			case RDFTERM_ID: break;
			case RDFTERM_RESOURCE:
				if (hasNodeIDAttr) throw new XMPException("Empty property element can't have both rdf:resource and rdf:nodeID", BADRDF);
				else if (hasValueAttr) throw new XMPException("Empty property element can't have both rdf:value and rdf:resource", BADXMP);
				hasResourceAttr = true;
				if (!hasValueAttr) valueNode = attribute;
				break;
			case RDFTERM_NODE_ID:
				if (hasResourceAttr) throw new XMPException("Empty property element can't have both rdf:resource and rdf:nodeID", BADRDF);
				hasNodeIDAttr = true;
				break;
			case RDFTERM_OTHER:
				if ("value".equals(attribute.getLocalName()) && NS_RDF.equals(attribute.getNamespaceURI())) {
					if (hasResourceAttr) throw new XMPException("Empty property element can't have both rdf:value and rdf:resource", BADXMP);
					hasValueAttr = true;
					valueNode = attribute;
				} else if (!XML_LANG.equals(attribute.getNodeName())) hasPropertyAttrs = true;
				break;
			default: throw new XMPException("Unrecognized attribute of empty property element", BADRDF);
			}
		}
		XMPNode childNode = addChildNode(xmp, xmpParent, xmlNode, "", isTopLevel);
		boolean childIsStruct = false;
		if (hasValueAttr || hasResourceAttr) {
			childNode.setValue(valueNode != null ? valueNode.getNodeValue() : "");
			if (!hasValueAttr) childNode.getOptions().setURI(true);
		} else if (hasPropertyAttrs) {
			childNode.getOptions().setStruct(true);
			childIsStruct = true;
		}
		for (int i = 0; i < xmlNode.getAttributes().getLength(); i++) {
			Node attribute = xmlNode.getAttributes().item(i);
			if (
				attribute == valueNode ||
				"xmlns".equals(attribute.getPrefix()) ||
				(
					attribute.getPrefix() == null &&
					"xmlns".equals(attribute.getNodeName())
				)
			) continue;
			switch (getRDFTermKind(attribute)) {
			case RDFTERM_ID:
			case RDFTERM_NODE_ID: break;
			case RDFTERM_RESOURCE:
				addQualifierNode(childNode, "rdf:resource", attribute.getNodeValue());
				break;
			case RDFTERM_OTHER:
				if (!childIsStruct) addQualifierNode(childNode, attribute.getNodeName(), attribute.getNodeValue());
				else if (XML_LANG.equals(attribute.getNodeName())) addQualifierNode(childNode, XML_LANG, attribute.getNodeValue());
				else addChildNode(xmp, childNode, attribute, attribute.getNodeValue(), false);
				break;
			default: throw new XMPException("Unrecognized attribute of empty property element", BADRDF);
			}

		}
	}

	private static XMPNode addChildNode(XMPMetaImpl xmp, XMPNode xmpParent, Node xmlNode, String value, boolean isTopLevel) throws XMPException {
		XMPSchemaRegistry registry = XMPMetaFactory.getSchemaRegistry();
		String namespace = xmlNode.getNamespaceURI();
		String childName;
		if (namespace != null) {
			if (NS_DC_DEPRECATED.equals(namespace)) namespace = NS_DC;
			String prefix = registry.getNamespacePrefix(namespace);
			if (prefix == null) {
				prefix = xmlNode.getPrefix() != null ? xmlNode.getPrefix() : DEFAULT_PREFIX;
				prefix = registry.registerNamespace(namespace, prefix);
			}
			childName = prefix + xmlNode.getLocalName();
		} else throw new XMPException("XML namespace required for all elements and attributes", BADRDF);
		boolean isAlias = false;
		if (isTopLevel) {
			XMPNode schemaNode = XMPNodeUtils.findSchemaNode(xmp.getRoot(), namespace, DEFAULT_PREFIX, true);
			schemaNode.setImplicit(false);
			xmpParent = schemaNode;
			if (registry.findAlias(childName) != null) {
				isAlias = true;
				xmp.getRoot().setHasAliases(true);
				schemaNode.setHasAliases(true);
			}
		}
		boolean isValueNode = "rdf:value".equals(childName);
		XMPNode newChild = new XMPNode(childName, value, new PropertyOptions());
		newChild.setAlias(isAlias);
		if (!isValueNode) xmpParent.addChild(newChild);
		else xmpParent.addChild(1, newChild);
		if (isValueNode) {
			if (isTopLevel || !xmpParent.getOptions().isStruct()) throw new XMPException("Misplaced rdf:value element", BADRDF);
			xmpParent.setHasValueChild(true);
		}
		if ("rdf:li".equals(childName)) {
			if (!xmpParent.getOptions().isArray()) throw new XMPException("Misplaced rdf:li element", BADRDF);
			newChild.setName(ARRAY_ITEM_NAME);
		}
		return newChild;
	}

	private static XMPNode addQualifierNode(XMPNode xmpParent, String name, String value) throws XMPException {
		XMPNode newQual = new XMPNode(name, XML_LANG.equals(name)? Utils.normalizeLangValue(value) : value, null);
		xmpParent.addQualifier(newQual);
		return newQual;
	}

	private static void fixupQualifiedNode(XMPNode xmpParent) throws XMPException {
		assert xmpParent.getOptions().isStruct() && xmpParent.hasChildren();
		XMPNode valueNode = xmpParent.getChild(1);
		assert "rdf:value".equals(valueNode.getName());
		if (valueNode.getOptions().getHasLanguage()) {
			if (xmpParent.getOptions().getHasLanguage()) throw new XMPException("Redundant xml:lang for rdf:value element", BADXMP);
			XMPNode langQual = valueNode.getQualifier(1);
			valueNode.removeQualifier(langQual);
			xmpParent.addQualifier(langQual);
		}
		for (int i = 1; i <= valueNode.getQualifierLength(); i++) xmpParent.addQualifier(valueNode.getQualifier(i));
		for (int i = 2; i <= xmpParent.getChildrenLength(); i++) xmpParent.addQualifier(xmpParent.getChild(i));
		assert xmpParent.getOptions().isStruct() || xmpParent.getHasValueChild();
		xmpParent.setHasValueChild(false);
		xmpParent.getOptions().setStruct(false);
		xmpParent.getOptions().mergeWith(valueNode.getOptions());
		xmpParent.setValue(valueNode.getValue());
		xmpParent.removeChildren();
		for (Iterator<XMPNode> it = valueNode.iterateChildren(); it.hasNext();) xmpParent.addChild(it.next());
	}

	private static boolean isWhitespaceNode(Node node) {
		if (node.getNodeType() != Node.TEXT_NODE) return false;
		String value = node.getNodeValue();
		for (int i = 0; i < value.length(); i++) if (!Character.isWhitespace(value.charAt(i))) return false;
		return true;
	}

	private static boolean isPropertyElementName(int term) {
		if (term == RDFTERM_DESCRIPTION || isOldTerm(term)) return false;
		else return (!isCoreSyntaxTerm(term));
	}

	private static boolean isOldTerm(int term) {
		return RDFTERM_FIRST_OLD <= term && term <= RDFTERM_LAST_OLD;
	}

	private static boolean isCoreSyntaxTerm(int term) {
		return RDFTERM_FIRST_CORE <= term && term <= RDFTERM_LAST_CORE;
	}

	private static int getRDFTermKind(Node node) {
		String localName = node.getLocalName();
		String namespace = node.getNamespaceURI();
		if (
			namespace == null &&
			("about".equals(localName) || "ID".equals(localName)) &&
			(node instanceof Attr) &&
			NS_RDF.equals(((Attr) node).getOwnerElement().getNamespaceURI())
		) namespace = NS_RDF;
		if (NS_RDF.equals(namespace)) {
			if ("li".equals(localName)) return RDFTERM_LI;
			else if ("parseType".equals(localName)) return RDFTERM_PARSE_TYPE;
			else if ("Description".equals(localName)) return RDFTERM_DESCRIPTION;
			else if ("about".equals(localName)) return RDFTERM_ABOUT;
			else if ("resource".equals(localName)) return RDFTERM_RESOURCE;
			else if ("RDF".equals(localName)) return RDFTERM_RDF;
			else if ("ID".equals(localName)) return RDFTERM_ID;
			else if ("nodeID".equals(localName)) return RDFTERM_NODE_ID;
			else if ("datatype".equals(localName)) return RDFTERM_DATATYPE;
			else if ("aboutEach".equals(localName)) return RDFTERM_ABOUT_EACH;
			else if ("aboutEachPrefix".equals(localName)) return RDFTERM_ABOUT_EACH_PREFIX;
			else if ("bagID".equals(localName)) return RDFTERM_BAG_ID;
		}
		return RDFTERM_OTHER;
	}

}