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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ProcessingInstruction;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.github.gasrios.xmp.XMPConst;
import com.github.gasrios.xmp.XMPError;
import com.github.gasrios.xmp.XMPException;
import com.github.gasrios.xmp.XMPMeta;
import com.github.gasrios.xmp.options.ParseOptions;

public class XMPMetaParser {

	private static final Object XMP_RDF = new Object();

	private static DocumentBuilderFactory factory = createDocumentBuilderFactory();

	private XMPMetaParser() {}

	public static XMPMeta parse(Object input, ParseOptions options) throws XMPException {
		ParameterAsserts.assertNotNull(input);
		options = options != null ? options : new ParseOptions();
		Document document = parseXml(input, options);
		boolean xmpmetaRequired = options.getRequireXMPMeta();
		Object[] result = new Object[3];
		result = findRootNode(document, xmpmetaRequired, result);
		if (result != null && result[1] == XMP_RDF) {
			XMPMetaImpl xmp = ParseRDF.parse((Node) result[0]);
			xmp.setPacketHeader((String) result[2]);
			if (!options.getOmitNormalization()) return XMPNormalizer.process(xmp, options);
			else return xmp;
		} else return new XMPMetaImpl();
	}

	private static Document parseXml(Object input, ParseOptions options) throws XMPException {
		if (input instanceof InputStream) return parseXmlFromInputStream((InputStream) input, options);
		else if (input instanceof byte[]) return parseXmlFromBytebuffer(new ByteBuffer((byte[]) input), options);
		else return parseXmlFromString((String) input, options);
	}

	private static Document parseXmlFromInputStream(InputStream stream, ParseOptions options) throws XMPException {
		if (!options.getAcceptLatin1() && !options.getFixControlChars()) return parseInputSource(new InputSource(stream));
		else {
			try {
				ByteBuffer buffer = new ByteBuffer(stream);
				return parseXmlFromBytebuffer(buffer, options);
			} catch (IOException e) {
				throw new XMPException("Error reading the XML-file", XMPError.BADSTREAM, e);
			}
		}
	}

	private static Document parseXmlFromBytebuffer(ByteBuffer buffer, ParseOptions options) throws XMPException {
		InputSource source = new InputSource(buffer.getByteStream());
		try {
			if (options.getDisallowDoctype()) try {
				factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
			} catch (Throwable e) {}
			return parseInputSource(source);
		} catch (XMPException e) {
			if (e.getErrorCode() == XMPError.BADXML || e.getErrorCode() == XMPError.BADSTREAM) {
				if (options.getAcceptLatin1()) buffer = Latin1Converter.convert(buffer);
				if (options.getFixControlChars()) try {
					String encoding = buffer.getEncoding();
					Reader fixReader = new FixASCIIControlsReader(new InputStreamReader(buffer.getByteStream(), encoding));
					return parseInputSource(new InputSource(fixReader));
				} catch (UnsupportedEncodingException e1) {
					throw new XMPException("Unsupported Encoding", XMPError.INTERNALFAILURE, e);
				}
				source = new InputSource(buffer.getByteStream());
				return parseInputSource(source);
			} else throw e;
		}
	}

	private static Document parseXmlFromString(String input, ParseOptions options) throws XMPException {
		InputSource source = new InputSource(new StringReader(input));
		try {
			if (options.getDisallowDoctype()) try {
				factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
			} catch (Throwable e) {}
			source = new InputSource(new StringReader(input));
			return parseInputSource(source);
		} catch (XMPException e) {
			if (e.getErrorCode() == XMPError.BADXML && options.getFixControlChars()) {
				source = new InputSource(new FixASCIIControlsReader(new StringReader(input)));
				return parseInputSource(source);
			} else throw e;
		}
	}

	private static Document parseInputSource(InputSource source) throws XMPException {
		try {
			DocumentBuilder builder = factory.newDocumentBuilder();
			builder.setErrorHandler(null);
			return builder.parse(source);
		} catch (SAXException e) {
			throw new XMPException("XML parsing failure", XMPError.BADXML, e);
		} catch (ParserConfigurationException e) {
			throw new XMPException("XML Parser not correctly configured", XMPError.UNKNOWN, e);
		} catch (IOException e) {
			throw new XMPException("Error reading the XML-file", XMPError.BADSTREAM, e);
		}
	}

	private static Object[] findRootNode(Node root, boolean xmpmetaRequired, Object[] result) {
		NodeList children = root.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			root = children.item(i);
			if (Node.PROCESSING_INSTRUCTION_NODE == root.getNodeType() && XMPConst.XMP_PI.equals(((ProcessingInstruction) root).getTarget())) {
				if (result != null) result[2] = ((ProcessingInstruction) root).getData();
			} else if (Node.TEXT_NODE != root.getNodeType() && Node.PROCESSING_INSTRUCTION_NODE != root.getNodeType()) {
				String rootNS = root.getNamespaceURI();
				String rootLocal = root.getLocalName();
				if ((XMPConst.TAG_XMPMETA.equals(rootLocal) || XMPConst.TAG_XAPMETA.equals(rootLocal)) && XMPConst.NS_X.equals(rootNS)) {
					return findRootNode(root, false, result);
				} else if (!xmpmetaRequired && "RDF".equals(rootLocal) && XMPConst.NS_RDF.equals(rootNS)) {
					if (result != null) {
						result[0] = root;
						result[1] = XMP_RDF;
					}
					return result;
				} else {
					Object[] newResult = findRootNode(root, xmpmetaRequired, result);
					if (newResult != null) return newResult;
					else continue;
				}
			}
		}
		return null;
	}

	private static DocumentBuilderFactory createDocumentBuilderFactory() {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		factory.setIgnoringComments(true);
		try {
			factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
			factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
			factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
			factory.setFeature("http://xerces.apache.org/xerces2-j/features.html#disallow-doctype-decl", false);
			factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
			factory.setFeature("http://xerces.apache.org/xerces2-j/features.html#external-parameter-entities", false);
			factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
			factory.setXIncludeAware(false);
			factory.setExpandEntityReferences(false);
		} catch (Exception e) {}
		return factory;
	}

}