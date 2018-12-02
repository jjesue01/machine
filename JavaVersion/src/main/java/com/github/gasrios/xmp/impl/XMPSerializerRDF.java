// =================================================================================================
// ADOBE SYSTEMS INCORPORATED
// Copyright 2006 Adobe Systems Incorporated
// All Rights Reserved
//
// NOTICE:  Adobe permits you to use, modify, and distribute this file in accordance with the terms
// of the Adobe license agreement accompanying it.
// =================================================================================================

package com.github.gasrios.xmp.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.github.gasrios.xmp.XMPConst;
import com.github.gasrios.xmp.XMPError;
import com.github.gasrios.xmp.XMPException;
import com.github.gasrios.xmp.XMPMeta;
import com.github.gasrios.xmp.XMPMetaFactory;
import com.github.gasrios.xmp.options.SerializeOptions;

public class XMPSerializerRDF {

	private static final int DEFAULT_PAD = 2048;

	private static final String PACKET_HEADER = "<?xpacket begin=\"\uFEFF\" id=\"W5M0MpCehiHzreSzNTczkc9d\"?>";

	private static final String PACKET_TRAILER = "<?xpacket end=\"";

	private static final String PACKET_TRAILER2 = "\"?>";

	private static final String RDF_XMPMETA_START = "<x:xmpmeta xmlns:x=\"adobe:ns:meta/\" x:xmptk=\"";

	private static final String RDF_XMPMETA_END = "</x:xmpmeta>";

	private static final String RDF_RDF_START = "<rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\">";

	private static final String RDF_RDF_END = "</rdf:RDF>";

	private static final String RDF_SCHEMA_START = "<rdf:Description rdf:about=";

	private static final String RDF_SCHEMA_END = "</rdf:Description>";

	private static final String RDF_STRUCT_START = "<rdf:Description";

	private static final String RDF_STRUCT_END = "</rdf:Description>";

	private static final String RDF_EMPTY_STRUCT = "<rdf:Description/>";

	static final Set<String> RDF_ATTR_QUALIFIER =
		new HashSet<String>(Arrays.asList(new String[] { XMPConst.XML_LANG, "rdf:resource", "rdf:ID", "rdf:bagID", "rdf:nodeID" }));

	private XMPMetaImpl xmp;

	private CountOutputStream outputStream;

	private OutputStreamWriter writer;

	private SerializeOptions options;

	private int unicodeSize = 1;

	private int padding;

	public void serialize(XMPMeta xmp, OutputStream out, SerializeOptions options) throws XMPException {
		try {
			outputStream = new CountOutputStream(out);
			writer = new OutputStreamWriter(outputStream, options.getEncoding());
			this.xmp = (XMPMetaImpl) xmp;
			this.options = options;
			this.padding = options.getPadding();
			writer = new OutputStreamWriter(outputStream, options.getEncoding());
			checkOptionsConsistence();
			String tailStr = serializeAsRDF();
			writer.flush();
			addPadding(tailStr.length());
			write(tailStr);
			writer.flush();
			outputStream.close();
		} catch (IOException e) {
			throw new XMPException("Error writing to the OutputStream", XMPError.UNKNOWN);
		}
	}

	private void addPadding(int tailLength) throws XMPException, IOException {
		if (options.getExactPacketLength()) {
			int minSize = outputStream.getBytesWritten() + tailLength * unicodeSize;
			if (minSize > padding) throw new XMPException("Can't fit into specified packet size", XMPError.BADSERIALIZE);
			padding -= minSize; // Now the actual amount of padding to add.
		}
		padding /= unicodeSize;
		int newlineLen = options.getNewline().length();
		if (padding >= newlineLen) {
			padding -= newlineLen; // Write this newline last.
			while (padding >= (100 + newlineLen)) {
				writeChars(100, ' ');
				writeNewline();
				padding -= (100 + newlineLen);
			}
			writeChars(padding, ' ');
			writeNewline();
		} else writeChars(padding, ' ');
	}

	protected void checkOptionsConsistence() throws XMPException {
		if (options.getEncodeUTF16BE() | options.getEncodeUTF16LE()) unicodeSize = 2;
		if (options.getExactPacketLength()) {
			if (options.getOmitPacketWrapper() | options.getIncludeThumbnailPad())
				throw new XMPException("Inconsistent options for exact size serialize", XMPError.BADOPTIONS);
			if ((options.getPadding() & (unicodeSize - 1)) != 0)
				throw new XMPException("Exact size must be a multiple of the Unicode element", XMPError.BADOPTIONS);
		} else if (options.getReadOnlyPacket()) {
			if (options.getOmitPacketWrapper() | options.getIncludeThumbnailPad())
				throw new XMPException("Inconsistent options for read-only packet", XMPError.BADOPTIONS);
			padding = 0;
		} else if (options.getOmitPacketWrapper()) {
			if (options.getIncludeThumbnailPad())
				throw new XMPException("Inconsistent options for non-packet serialize", XMPError.BADOPTIONS);
			padding = 0;
		} else {
			if (padding == 0) padding = DEFAULT_PAD * unicodeSize;
			if (
				options.getIncludeThumbnailPad() &&
				!xmp.doesPropertyExist(XMPConst.NS_XMP, "Thumbnails")
			) padding += 10000 * unicodeSize;
		}
	}

	private String serializeAsRDF() throws IOException, XMPException {
		int level = 0;
		if (!options.getOmitPacketWrapper()) {
			writeIndent(level);
			write(PACKET_HEADER);
			writeNewline();
		}
		if (!options.getOmitXmpMetaElement()) {
			writeIndent(level);
			write(RDF_XMPMETA_START);
			if (!options.getOmitVersionAttribute()) write(XMPMetaFactory.getVersionInfo().getMessage());
			write("\">");
			writeNewline();
			level++;
		}
		writeIndent(level);
		write(RDF_RDF_START);
		writeNewline();
		if (options.getUseCanonicalFormat()) serializeCanonicalRDFSchemas(level);
		else serializeCompactRDFSchemas(level);
		writeIndent(level);
		write(RDF_RDF_END);
		writeNewline();
		if (!options.getOmitXmpMetaElement()) {
			writeIndent(--level);
			write(RDF_XMPMETA_END);
			writeNewline();
		}
		String tailStr = "";
		if (!options.getOmitPacketWrapper()) {
			for (level = options.getBaseIndent(); level > 0; level--) tailStr += options.getIndent();
			tailStr += PACKET_TRAILER;
			tailStr += options.getReadOnlyPacket() ? 'r' : 'w';
			tailStr += PACKET_TRAILER2;
		}
		return tailStr;
	}

	private void serializeCanonicalRDFSchemas(int level) throws IOException, XMPException {
		if (xmp.getRoot().getChildrenLength() > 0) {
			startOuterRDFDescription(xmp.getRoot(), level);
			for (Iterator<XMPNode> it = xmp.getRoot().iterateChildren(); it.hasNext();) {
				XMPNode currSchema = it.next();
				serializeCanonicalRDFSchema(currSchema, level);
			}
			endOuterRDFDescription(level);
		} else {
			writeIndent(level + 1);
			write(RDF_SCHEMA_START);
			writeTreeName();
			write("/>");
			writeNewline();
		}
	}

	private void writeTreeName() throws IOException {
		write('"');
		String name = xmp.getRoot().getName();
		if (name != null) appendNodeValue(name, true);
		write('"');
	}

	private void serializeCompactRDFSchemas(int level) throws IOException, XMPException {
		writeIndent(level + 1);
		write(RDF_SCHEMA_START);
		writeTreeName();
		Set<String> usedPrefixes = new HashSet<String>();
		usedPrefixes.add("xml");
		usedPrefixes.add("rdf");
		for (Iterator<XMPNode> it = xmp.getRoot().iterateChildren(); it.hasNext();) declareUsedNamespaces(it.next(), usedPrefixes, level + 3);
		boolean allAreAttrs = true;
		for (Iterator<XMPNode> it = xmp.getRoot().iterateChildren(); it.hasNext();) allAreAttrs &= serializeCompactRDFAttrProps(it.next(), level + 2);
		if (!allAreAttrs) {
			write('>');
			writeNewline();
		} else {
			write("/>");
			writeNewline();
			return;
		}
		for (Iterator<XMPNode> it = xmp.getRoot().iterateChildren(); it.hasNext();) serializeCompactRDFElementProps(it.next(), level + 2);
		writeIndent(level + 1);
		write(RDF_SCHEMA_END);
		writeNewline();
	}

	private boolean serializeCompactRDFAttrProps(XMPNode parentNode, int indent) throws IOException {
		boolean allAreAttrs = true;
		for (Iterator<XMPNode> it = parentNode.iterateChildren(); it.hasNext();) {
			XMPNode prop = it.next();
			if (canBeRDFAttrProp(prop)) {
				writeNewline();
				writeIndent(indent);
				write(prop.getName());
				write("=\"");
				appendNodeValue(prop.getValue(), true);
				write('"');
			} else allAreAttrs = false;
		}
		return allAreAttrs;
	}

	private void serializeCompactRDFElementProps(XMPNode parentNode, int indent) throws IOException, XMPException {
		for (Iterator<XMPNode> it = parentNode.iterateChildren(); it.hasNext();) {
			XMPNode node = it.next();
			if (canBeRDFAttrProp(node)) continue;
			boolean emitEndTag = true;
			boolean indentEndTag = true;
			String elemName = node.getName();
			if (XMPConst.ARRAY_ITEM_NAME.equals(elemName)) elemName = "rdf:li";
			writeIndent(indent);
			write('<');
			write(elemName);
			boolean hasGeneralQualifiers = false;
			boolean hasRDFResourceQual = false;
			for (Iterator<XMPNode> iq = node.iterateQualifier(); iq.hasNext();) {
				XMPNode qualifier = iq.next();
				if (!RDF_ATTR_QUALIFIER.contains(qualifier.getName())) hasGeneralQualifiers = true;
				else {
					hasRDFResourceQual = "rdf:resource".equals(qualifier.getName());
					write(' ');
					write(qualifier.getName());
					write("=\"");
					appendNodeValue(qualifier.getValue(), true);
					write('"');
				}
			}
			if (hasGeneralQualifiers) serializeCompactRDFGeneralQualifier(indent, node);
			else if (!node.getOptions().isCompositeProperty()) {
				Object[] result = serializeCompactRDFSimpleProp(node);
				emitEndTag = ((Boolean) result[0]).booleanValue();
				indentEndTag = ((Boolean) result[1]).booleanValue();
			}
			else if (node.getOptions().isArray()) serializeCompactRDFArrayProp(node, indent);
			else emitEndTag = serializeCompactRDFStructProp(node, indent, hasRDFResourceQual);
			if (emitEndTag) {
				if (indentEndTag) writeIndent(indent);
				write("</");
				write(elemName);
				write('>');
				writeNewline();
			}
		}
	}

	private Object[] serializeCompactRDFSimpleProp(XMPNode node) throws IOException {
		Boolean emitEndTag = Boolean.TRUE;
		Boolean indentEndTag = Boolean.TRUE;
		if (node.getOptions().isURI()) {
			write(" rdf:resource=\"");
			appendNodeValue(node.getValue(), true);
			write("\"/>");
			writeNewline();
			emitEndTag = Boolean.FALSE;
		} else if (node.getValue() == null || node.getValue().length() == 0) {
			write("/>");
			writeNewline();
			emitEndTag = Boolean.FALSE;
		} else {
			write('>');
			appendNodeValue(node.getValue(), false);
			indentEndTag = Boolean.FALSE;
		}
		return new Object[] { emitEndTag, indentEndTag };
	}

	private void serializeCompactRDFArrayProp(XMPNode node, int indent) throws IOException, XMPException {
		write('>');
		writeNewline();
		emitRDFArrayTag(node, true, indent + 1);
		if (node.getOptions().isArrayAltText()) XMPNodeUtils.normalizeLangArray(node);
		serializeCompactRDFElementProps(node, indent + 2);
		emitRDFArrayTag(node, false, indent + 1);
	}

	private boolean serializeCompactRDFStructProp(XMPNode node, int indent, boolean hasRDFResourceQual) throws XMPException, IOException {
		boolean hasAttrFields = false;
		boolean hasElemFields = false;
		boolean emitEndTag = true;
		for (Iterator<XMPNode> ic = node.iterateChildren(); ic.hasNext();) {
			if (canBeRDFAttrProp(ic.next())) hasAttrFields = true;
			else hasElemFields = true;
			if (hasAttrFields && hasElemFields) break;
		}
		if (hasRDFResourceQual && hasElemFields) throw new XMPException("Can't mix rdf:resource qualifier and element fields", XMPError.BADRDF);
		if (!node.hasChildren()) {
			write(" rdf:parseType=\"Resource\"/>");
			writeNewline();
			emitEndTag = false;
		} else if (!hasElemFields) {
			serializeCompactRDFAttrProps(node, indent + 1);
			write("/>");
			writeNewline();
			emitEndTag = false;
		} else if (!hasAttrFields) {
			write(" rdf:parseType=\"Resource\">");
			writeNewline();
			serializeCompactRDFElementProps(node, indent + 1);
		} else {
			write('>');
			writeNewline();
			writeIndent(indent + 1);
			write(RDF_STRUCT_START);
			serializeCompactRDFAttrProps(node, indent + 2);
			write(">");
			writeNewline();
			serializeCompactRDFElementProps(node, indent + 1);
			writeIndent(indent + 1);
			write(RDF_STRUCT_END);
			writeNewline();
		}
		return emitEndTag;
	}

	private void serializeCompactRDFGeneralQualifier(int indent, XMPNode node) throws IOException, XMPException {
		write(" rdf:parseType=\"Resource\">");
		writeNewline();
		serializeCanonicalRDFProperty(node, false, true, indent + 1);
		for (Iterator<XMPNode> iq = node.iterateQualifier(); iq.hasNext();) serializeCanonicalRDFProperty(iq.next(), false, false, indent + 1);
	}

	private void serializeCanonicalRDFSchema(XMPNode schemaNode, int level) throws IOException, XMPException {
		for (Iterator<XMPNode> it = schemaNode.iterateChildren(); it.hasNext();)
			serializeCanonicalRDFProperty(it.next(), options.getUseCanonicalFormat(), false, level + 2);
	}

	private void declareUsedNamespaces(XMPNode node, Set<String> usedPrefixes, int indent) throws IOException {
		if (node.getOptions().isSchemaNode())
			declareNamespace(node.getValue().substring(0, node.getValue().length() - 1), node.getName(), usedPrefixes, indent);
		else if (node.getOptions().isStruct()) for (Iterator<XMPNode> it = node.iterateChildren(); it.hasNext();)
			declareNamespace(it.next().getName(), null, usedPrefixes, indent);
		for (Iterator<XMPNode> it = node.iterateChildren(); it.hasNext();) declareUsedNamespaces(it.next(), usedPrefixes, indent);
		for (Iterator<XMPNode> it = node.iterateQualifier(); it.hasNext();) {
			XMPNode qualifier = it.next();
			declareNamespace(qualifier.getName(), null, usedPrefixes, indent);
			declareUsedNamespaces(qualifier, usedPrefixes, indent);
		}
	}

	private void declareNamespace(String prefix, String namespace, Set<String> usedPrefixes, int indent) throws IOException {
		if (namespace == null) {
			QName qname = new QName(prefix);
			if (qname.hasPrefix()) {
				prefix = qname.getPrefix();
				namespace = XMPMetaFactory.getSchemaRegistry().getNamespaceURI(prefix + ":");
				declareNamespace(prefix, namespace, usedPrefixes, indent);
			} else return;
		}
		if (!usedPrefixes.contains(prefix)) {
			writeNewline();
			writeIndent(indent);
			write("xmlns:");
			write(prefix);
			write("=\"");
			write(namespace);
			write('"');
			usedPrefixes.add(prefix);
		}
	}

	private void startOuterRDFDescription(XMPNode schemaNode, int level) throws IOException {
		writeIndent(level + 1);
		write(RDF_SCHEMA_START);
		writeTreeName();
		Set<String> usedPrefixes = new HashSet<String>();
		usedPrefixes.add("xml");
		usedPrefixes.add("rdf");
		declareUsedNamespaces(schemaNode, usedPrefixes, level + 3);
		write('>');
		writeNewline();
	}

	private void endOuterRDFDescription(int level) throws IOException {
		writeIndent(level + 1);
		write(RDF_SCHEMA_END);
		writeNewline();
	}

	private void serializeCanonicalRDFProperty(XMPNode node, boolean useCanonicalRDF, boolean emitAsRDFValue, int indent)
	throws IOException, XMPException {
		boolean emitEndTag = true;
		boolean indentEndTag = true;
		String elemName = node.getName();
		if (emitAsRDFValue) elemName = "rdf:value";
		else if (XMPConst.ARRAY_ITEM_NAME.equals(elemName)) elemName = "rdf:li";
		writeIndent(indent);
		write('<');
		write(elemName);
		boolean hasGeneralQualifiers = false;
		boolean hasRDFResourceQual = false;
		for (Iterator<XMPNode> it = node.iterateQualifier(); it.hasNext();) {
			XMPNode qualifier = it.next();
			if (!RDF_ATTR_QUALIFIER.contains(qualifier.getName())) hasGeneralQualifiers = true;
			else {
				hasRDFResourceQual = "rdf:resource".equals(qualifier.getName());
				if (!emitAsRDFValue) {
					write(' ');
					write(qualifier.getName());
					write("=\"");
					appendNodeValue(qualifier.getValue(), true);
					write('"');
				}
			}
		}
		if (hasGeneralQualifiers && !emitAsRDFValue) {
			if (hasRDFResourceQual) throw new XMPException("Can't mix rdf:resource and general qualifiers", XMPError.BADRDF);
			if (useCanonicalRDF) {
				write(">");
				writeNewline();
				writeIndent(++indent);
				write(RDF_STRUCT_START);
				write(">");
			} else write(" rdf:parseType=\"Resource\">");
			writeNewline();
			serializeCanonicalRDFProperty(node, useCanonicalRDF, true, indent + 1);
			for (Iterator<XMPNode> it = node.iterateQualifier(); it.hasNext();) {
				XMPNode qualifier = it.next();
				if (!RDF_ATTR_QUALIFIER.contains(qualifier.getName())) serializeCanonicalRDFProperty(qualifier, useCanonicalRDF, false, indent + 1);
			}
			if (useCanonicalRDF) {
				writeIndent(indent);
				write(RDF_STRUCT_END);
				writeNewline();
				indent--;
			}
		} else {
			if (!node.getOptions().isCompositeProperty()) {
				if (node.getOptions().isURI()) {
					write(" rdf:resource=\"");
					appendNodeValue(node.getValue(), true);
					write("\"/>");
					writeNewline();
					emitEndTag = false;
				} else if (node.getValue() == null || "".equals(node.getValue())) {
					write("/>");
					writeNewline();
					emitEndTag = false;
				} else {
					write('>');
					appendNodeValue(node.getValue(), false);
					indentEndTag = false;
				}
			} else if (node.getOptions().isArray()) {
				write('>');
				writeNewline();
				emitRDFArrayTag(node, true, indent + 1);
				if (node.getOptions().isArrayAltText()) XMPNodeUtils.normalizeLangArray(node);
				for (Iterator<XMPNode> it = node.iterateChildren(); it.hasNext();)
					serializeCanonicalRDFProperty(it.next(), useCanonicalRDF, false, indent + 2);
				emitRDFArrayTag(node, false, indent + 1);
			} else if (!hasRDFResourceQual) {
				if (!node.hasChildren()) {
					if (useCanonicalRDF) {
						write(">");
						writeNewline();
						writeIndent(indent + 1);
						write(RDF_EMPTY_STRUCT);
					} else {
						write(" rdf:parseType=\"Resource\"/>");
						emitEndTag = false;
					}
					writeNewline();
				} else {
					if (useCanonicalRDF) {
						write(">");
						writeNewline();
						indent++;
						writeIndent(indent);
						write(RDF_STRUCT_START);
						write(">");
					} else write(" rdf:parseType=\"Resource\">");
					writeNewline();
					for (Iterator<XMPNode> it = node.iterateChildren(); it.hasNext();) {
						XMPNode child = it.next();
						serializeCanonicalRDFProperty(child, useCanonicalRDF, false, indent + 1);
					}
					if (useCanonicalRDF) {
						writeIndent(indent);
						write(RDF_STRUCT_END);
						writeNewline();
						indent--;
					}
				}
			} else {
				for (Iterator<XMPNode> it = node.iterateChildren(); it.hasNext();) {
					XMPNode child = it.next();
					if (!canBeRDFAttrProp(child)) throw new XMPException("Can't mix rdf:resource and complex fields", XMPError.BADRDF);
					writeNewline();
					writeIndent(indent + 1);
					write(' ');
					write(child.getName());
					write("=\"");
					appendNodeValue(child.getValue(), true);
					write('"');
				}
				write("/>");
				writeNewline();
				emitEndTag = false;
			}
		}
		if (emitEndTag) {
			if (indentEndTag) writeIndent(indent);
			write("</");
			write(elemName);
			write('>');
			writeNewline();
		}
	}

	private void emitRDFArrayTag(XMPNode arrayNode, boolean isStartTag, int indent) throws IOException {
		if (isStartTag || arrayNode.hasChildren()) {
			writeIndent(indent);
			write(isStartTag ? "<rdf:" : "</rdf:");
			if (arrayNode.getOptions().isArrayAlternate()) write("Alt");
			else if (arrayNode.getOptions().isArrayOrdered()) write("Seq");
			else write("Bag");
			if (isStartTag && !arrayNode.hasChildren()) write("/>");
			else write(">");
			writeNewline();
		}
	}

	private void appendNodeValue(String value, boolean forAttribute) throws IOException {
		if (value == null) value = "";
		write(Utils.escapeXML(value, forAttribute, true));
	}

	private boolean canBeRDFAttrProp(XMPNode node) {
		return
			!node.hasQualifier() &&
			!node.getOptions().isURI() &&
			!node.getOptions().isCompositeProperty() &&
			!XMPConst.ARRAY_ITEM_NAME.equals(node.getName());
	}

	private void writeIndent(int times) throws IOException {
		for (int i = options.getBaseIndent() + times; i > 0; i--) writer.write(options.getIndent());
	}

	private void write(int c) throws IOException {
		writer.write(c);
	}

	private void write(String str) throws IOException {
		writer.write(str);
	}

	private void writeChars(int number, char c) throws IOException {
		for (; number > 0; number--) writer.write(c);
	}

	private void writeNewline() throws IOException {
		writer.write(options.getNewline());
	}

}