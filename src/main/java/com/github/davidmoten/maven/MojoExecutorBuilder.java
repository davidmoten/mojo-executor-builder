package com.github.davidmoten.maven;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class MojoExecutorBuilder {

	public void generateSource(File sourceDir, Artifact artifact, String pkg,
			InputStream pluginXml) {
		sourceDir.mkdirs();

		Document doc;
		XPath xpath;
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			doc = builder.parse(pluginXml);
			XPathFactory xPathfactory = XPathFactory.newInstance();
			xpath = xPathfactory.newXPath();
		} catch (SAXException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (ParserConfigurationException e) {
			throw new RuntimeException(e);
		}
		try {
			NodeList mojos = (NodeList) xpath.compile("/plugin/mojos/mojo")
					.evaluate(doc, XPathConstants.NODESET);
			System.out.println("mojos=" + mojos.getLength());
			for (int i = 0; i < mojos.getLength(); i++) {
				Node mojo = mojos.item(i);
				NodeList children = mojo.getChildNodes();
				String goal = getValue(children, "goal");
				String mojoClassName = getValue(children, "implementation");
				System.out.println("  mojo: " + goal + "[" + mojoClassName
						+ "]");
				for (Method m : getMethods(mojoClassName)) {
					if (m.getName().startsWith("get")
							&& !m.getName().equals("getClass")) {
						System.out.println("    method: " + m.getName());
						for (Annotation a : m.getAnnotations())
							System.out.println("      annotation: "
									+ a.toString());
					}
				}
			}
		} catch (XPathExpressionException e) {
			throw new RuntimeException(e);
		} catch (SecurityException e) {
			throw new RuntimeException(e);
		}
	}

	private Method[] getMethods(String mojoClassName) {
		try {
			return Class.forName(mojoClassName).getMethods();
		} catch (SecurityException e) {
			throw new RuntimeException(e);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	private String getValue(NodeList nodeList, String name) {
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node node = nodeList.item(i);
			if (node.getNodeName().equals(name))
				return node.getTextContent();
		}
		return null;
	}
}
