package com.github.davidmoten.maven;

import static com.google.common.base.Optional.absent;
import static com.google.common.base.Optional.fromNullable;
import static com.google.common.base.Optional.of;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
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

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.WordUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.google.common.base.Optional;

public class MojoExecutorBuilder {

	public void generateSource(File sourceDir, Artifact artifact, String pkg,
			InputStream pluginXml) {
		sourceDir.mkdirs();
		List<Mojo> mojos = getMojos(pluginXml);
		for (Mojo mojo : mojos) {
			System.out.println(mojo);
		}
		File file = createBuilder(sourceDir, artifact, pkg, mojos);
		System.out.println();
		System.out.println(file.getPath());
		System.out.println();
		System.out.println("-----------------------------------");
		logFileContent(file);
	}

	private void logFileContent(File file) {
		try {
			System.out.println(IOUtils.toString(new FileInputStream(file)));
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private File createBuilder(File sourceDir, Artifact artifact, String pkg1,
			List<Mojo> mojos) {
		String path = getPath(artifact);
		File file = new File(sourceDir, path);
		String pkg = getPackage(path);
		createBuilder(file, pkg, mojos);
		return file;
	}

	private String getPackage(String path) {
		String pkg = path.replace("/", ".");
		pkg = pkg.substring(0, pkg.lastIndexOf("."));
		pkg = pkg.substring(0, pkg.lastIndexOf("."));
		return pkg;
	}

	private String getPath(Artifact artifact) {
		String path = artifact.getGroupId().replace(".", " ").replace("-", " ")
				+ "/"
				+ WordUtils.capitalize(
						artifact.getArtifactId().replace("-", " ")
								.replace("_", " ")).replace(" ", "") + "_"
				+ artifact.getVersion().replace(".", "_") + ".java";
		path = path.replace(" ", "/");
		return path;
	}

	private void createBuilder(File file, String pkg, List<Mojo> mojos) {
		PrintStream out = null;
		try {
			file.getParentFile().mkdirs();
			out = new PrintStream(file);
			String className = file.getName().substring(0,
					file.getName().lastIndexOf("."));
			createBuilder(out, pkg, className, mojos);
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		} finally {
			if (out != null)
				out.close();
		}
	}

	private void createBuilder(PrintStream out, String pkg, String className,
			List<Mojo> mojos) {

		ImportManager im = new ImportManager();
		out.format("package %s;\n\n", pkg);
		out.format("// imports here\n\n");
		out.format("public class %s {\n\n", className);
		out.format("  private String evaluateBoolean(String value) {\n    return value;\n  }\n\n");

		for (Mojo mojo : mojos) {
			String builderClassName = im.add(
					pkg,
					className,
					WordUtils.capitalize(
							mojo.getGoal().replace("-", " ").replace(".", " "))
							.replace(" ", "")
							+ "Builder");
			String goalMethod = WordUtils.capitalize(
					mojo.getGoal().replace("-", " ").replace(".", " "))
					.replace(" ", "");
			goalMethod = Character.toLowerCase(goalMethod.charAt(0))
					+ goalMethod.substring(1);
			out.format("  public static %s %s() {\n", builderClassName,
					goalMethod);
			out.format("    return new %s();\n", builderClassName);
			out.format("  }\n\n");

			out.format("  public static class %s {\n\n", builderClassName);
			for (Parameter parameter : mojo.getParameters()) {
				// TODO handle
				// boolean,Boolean,byte,Byte,int,Integer,long,Long,short,Short,double,Double,float,Float,Date,URL,String,Arrays,Collection,Map,Properties
				String defaultValue = parameter.getDefaultValue();
				String baseType = im.add(parameter.getBaseType());
				Optional<String> genericType;
				if (parameter.getGenericType().isPresent())
					genericType = of(im.add(parameter.getGenericType().get()));
				else
					genericType = absent();
				out.format("      private String %s = %s;\n",
						parameter.getMethodName(),
						quoteIfNotNull(parameter.getDefaultValue()));
			}
			out.format("  }\n\n");
		}
		out.format("}");
		out.println(im.getImportBlock());
	}

	private String quoteIfNotNull(String s) {
		if (s == null)
			return s;
		else
			return "\"" + s + "\"";
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

	@SuppressWarnings("unchecked")
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
					Class<?> type = c.getDeclaredField(fi.getName()).getType();
					final String baseType;
					if (type.isArray())
						baseType = type.getComponentType().getName();
					else
						baseType = type.getName();
					String genericType;
					if (c.getTypeParameters().length > 0)
						genericType = c.getTypeParameters()[0].getName();
					else
						genericType = null;

					parameters
							.add(new Parameter(
									fi.getName(),
									stripQuotes(memberNames.get("alias")),
									stripQuotes(memberNames.get("property")),
									stripQuotes(memberNames.get("defaultValue")),
									toBoolean(stripQuotes(memberNames
											.get("required"))),
									toBoolean(stripQuotes(memberNames
											.get("readonly"))), baseType,
									fromNullable(genericType), type.isArray()));
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

	private static String stripQuotes(String s) {
		if (s == null)
			return null;
		else
			return s.replace("\"", "");
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
