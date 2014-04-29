package com.github.davidmoten.maven;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ClassFile;
import javassist.bytecode.FieldInfo;

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
		List<Mojo> mojos = getMojos(pluginXml);
		for (Mojo mojo : mojos)
			System.out.println(mojo);

	}

	private List<Mojo> getMojos(InputStream pluginXml) {
		List<Mojo> mojos;

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
			mojos = getMojos(doc, xpath);

		} catch (XPathExpressionException e) {
			throw new RuntimeException(e);
		} catch (SecurityException e) {
			throw new RuntimeException(e);
		}
		return mojos;
	}

	private List<Mojo> getMojos(Document doc, XPath xpath)
			throws XPathExpressionException {
		NodeList mojos = (NodeList) xpath.compile("/plugin/mojos/mojo")
				.evaluate(doc, XPathConstants.NODESET);
		List<Mojo> mojoList = new ArrayList<Mojo>();
		for (int i = 0; i < mojos.getLength(); i++) {
			Node mojo = mojos.item(i);
			NodeList children = mojo.getChildNodes();
			String goal = getValue(children, "goal");
			String mojoClassName = getValue(children, "implementation");

			List<Parameter> parameters = new ArrayList<Parameter>();
			Class<?> c = getCls(mojoClassName);
			while (c != null) {
				parameters.addAll(getParameters(c));
				c = c.getSuperclass();
			}
			Mojo mj = new Mojo(goal, mojoClassName, parameters);
			mojoList.add(mj);
		}
		return mojoList;
	}

	private static List<Parameter> getParameters(Class<?> c) {
		try {
			List<Parameter> parameters = new ArrayList<Parameter>();
			URL location = c.getResource('/' + c.getName().replace('.', '/')
					+ ".class");
			DataInputStream dstream = new DataInputStream(
					new BufferedInputStream(location.openStream()));

			ClassFile cf = new ClassFile(dstream);
			for (FieldInfo fi : (List<FieldInfo>) cf.getFields()) {
				AnnotationsAttribute invisible = (AnnotationsAttribute) fi
						.getAttribute(AnnotationsAttribute.invisibleTag);
				boolean isParameter = false;
				Map<String, String> memberNames = new HashMap<String, String>();
				if (invisible != null)
					for (javassist.bytecode.annotation.Annotation ann : invisible
							.getAnnotations()) {
						if (ann.getTypeName()
								.equals(org.apache.maven.plugins.annotations.Parameter.class
										.getName()))
							isParameter = true;
						if (ann.getMemberNames() != null)
							for (String memberName : (Set<String>) ann
									.getMemberNames())
								memberNames.put(memberName,
										ann.getMemberValue(memberName)
												.toString());

					}
				if (isParameter) {
					String baseType = c.getDeclaredField(fi.getName())
							.getType().getName();
					String genericType;
					if (c.getTypeParameters().length > 0)
						genericType = c.getTypeParameters()[0].getName();
					else
						genericType = null;
					parameters.add(new Parameter(fi.getName(), memberNames
							.get("alias"), memberNames.get("property"),
							memberNames.get("defaultValue"),
							toBoolean(memberNames.get("required")),
							toBoolean(memberNames.get("readonly"))));

				}
			}
			return parameters;
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (NoSuchFieldException e) {
			throw new RuntimeException(e);
		} catch (SecurityException e) {
			throw new RuntimeException(e);
		}
	}

	private static Boolean toBoolean(String s) {
		if (s == null)
			return null;
		else
			return s.equals("true");
	}

	private Class<?> getCls(String cls) {
		try {
			return Class.forName(cls);
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
