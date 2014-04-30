package com.github.davidmoten.maven;

import java.util.List;

public class Mojo {
	private final String goal;
	private final String mojoClassName;
	private final List<Parameter> parameters;

	public Mojo(String goal, String mojoClassName, List<Parameter> parameters) {
		this.goal = goal;
		this.mojoClassName = mojoClassName;
		this.parameters = parameters;
	}

	public String getGoal() {
		return goal;
	}

	public String getMojoClassName() {
		return mojoClassName;
	}

	public List<Parameter> getParameters() {
		return parameters;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Mojo [goal=");
		builder.append(goal);
		builder.append(", mojoClassName=");
		builder.append(mojoClassName);
		builder.append(", parameters=");
		builder.append(parameters);
		builder.append("]");
		return builder.toString();
	}

}
