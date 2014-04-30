package com.github.davidmoten.maven;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Properties;

import com.google.common.base.Optional;

public class Parameter {
	private final String name;
	private final String alias;
	private final String property;
	private final String defaultValue;
	private final boolean required;
	private final boolean readonly;
	private final String baseType;
	private final Optional<String> genericType;
	private final boolean isArray;

	public Parameter(String name, String alias, String property,
			String defaultValue, Boolean required, Boolean readonly,
			String baseType, Optional<String> genericType, boolean isArray) {
		this.name = name;
		this.alias = alias;
		this.property = property;
		this.defaultValue = defaultValue;
		this.baseType = baseType;
		this.genericType = genericType;
		this.isArray = isArray;
		this.required = (required == null ? false : required);
		this.readonly = (readonly == null ? false : readonly);
	}

	public String getMethodName() {
		String s;
		if (property != null)
			s = property;
		else
			s = name;
		return s.replaceAll("\\.", "_");
	}

	public String getName() {
		return name;
	}

	public String getAlias() {
		return alias;
	}

	public String getProperty() {
		return property;
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	public boolean isRequired() {
		return required;
	}

	public boolean isReadonly() {
		return readonly;
	}

	public String getBaseType() {
		return baseType;
	}

	public boolean isArray() {
		return isArray;
	}

	public Optional<String> getGenericType() {
		return genericType;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Parameter [name=");
		builder.append(name);
		builder.append(", alias=");
		builder.append(alias);
		builder.append(", property=");
		builder.append(property);
		builder.append(", defaultValue=");
		builder.append(defaultValue);
		builder.append(", required=");
		builder.append(required);
		builder.append(", readonly=");
		builder.append(readonly);
		builder.append(", type=");
		builder.append(baseType);
		builder.append("]");
		return builder.toString();
	}

	public boolean isCollection() {
		return baseTypeIs(Collection.class);
	}

	private boolean baseTypeIs(Class<?> c) {
		if (isPrimitive(baseType))
			return false;
		try {
			Class<?> cls = Class.forName(baseType);
			return (c.isAssignableFrom(cls));
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	public boolean isMap() {
		return baseTypeIs(Map.class);
	}

	public boolean isProperties() {
		return baseTypeIs(Properties.class);
	}

	private static boolean isPrimitive(String className) {
		String[] prims = { "boolean", "short", "int", "double", "float",
				"long", "char" };
		return Arrays.asList(prims).contains(className);
	}
}
