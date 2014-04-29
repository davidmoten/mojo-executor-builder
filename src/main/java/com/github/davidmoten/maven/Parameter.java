package com.github.davidmoten.maven;

import java.util.HashMap;
import java.util.Map;

public class Parameter {
	private final Map<String, String> map;
	private final String name;

	public Parameter(Map<String, String> map, String name) {
		this.map = new HashMap<String, String>(map);
		this.name = name;
	}

	public String methodName() {
		String s;
		if (map.get("property") != null)
			s = map.get("property");
		else
			s = name;
		return s.replaceAll("\\.", "_");
	}
}
