package com.github.davidmoten.maven;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.List;

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

import org.apache.maven.plugins.annotations.Parameter;
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

				Parameter p;
				Class<?> cls = getCls(mojoClassName);
				while (cls != null) {
					System.out.println("    class: " + cls);
					for (Field f : cls.getDeclaredFields()) {
						// f.setAccessible(true);
						System.out.println("      field: " + f.getName());
						for (Annotation a : f.getDeclaredAnnotations())
							System.out.println("      annotation: "
									+ a.toString());
					}
					cls = cls.getSuperclass();
				}
				Class<?> c = getCls(mojoClassName);
				URL location = c.getResource('/'
						+ c.getName().replace('.', '/') + ".class");
				DataInputStream dstream = new DataInputStream(
						new BufferedInputStream(location.openStream()));

				ClassFile cf = new ClassFile(dstream);
				for (FieldInfo fi : (List<FieldInfo>) cf.getFields()) {
					System.out.println(fi);
					AnnotationsAttribute visible = (AnnotationsAttribute) fi
							.getAttribute(AnnotationsAttribute.visibleTag);
					AnnotationsAttribute invisible = (AnnotationsAttribute) fi
							.getAttribute(AnnotationsAttribute.invisibleTag);
					if (invisible != null)
						for (javassist.bytecode.annotation.Annotation ann : invisible
								.getAnnotations()) {
							System.out.println("@" + ann.getTypeName());
						}
				}

			}
		} catch (XPathExpressionException e) {
			throw new RuntimeException(e);
		} catch (SecurityException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

	}

	private Class<?> getCls(String cls) {
		try {
			return Class.forName(cls);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	private Field[] getFields(String mojoClassName) {
		try {
			return Class.forName(mojoClassName).getFields();
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
