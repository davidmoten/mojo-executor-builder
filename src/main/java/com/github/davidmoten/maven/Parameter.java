package com.github.davidmoten.maven;

public class Parameter {
	private final String name;
	private final String alias;
	private final String property;
	private final String defaultValue;
	private final boolean required;
	private final boolean readonly;

	public Parameter(String name, String alias, String property,
			String defaultValue, Boolean required, Boolean readonly) {
		this.name = name;
		this.alias = alias;
		this.property = property;
		this.defaultValue = defaultValue;

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
		builder.append("]");
		return builder.toString();
	}

}
