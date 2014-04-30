package com.github.davidmoten.maven;

import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Sets;

public class ImportManager {

	private final BiMap<String, String> map = HashBiMap.create();
	private final Set<String> staticNames = Sets.newHashSet();

	public ImportManager() {
	}

	public String getImportBlock() {
		StringBuilder s = new StringBuilder();
		for (Entry<String, String> entry : new TreeMap<String, String>(map)
				.entrySet()) {
			if (!entry.getKey().equals(entry.getValue())) {
				String st;
				if (staticNames.contains(entry.getKey()))
					st = " static ";
				else
					st = "";
				s.append("import " + st + entry.getKey() + ";\n");
			}
		}
		return s.toString();
	}

	public String addStatic(Class<?> cls, String method) {
		String name = cls.getName() + "." + method;
		return add(name, true);
	}

	public String add(String className) {
		return add(className, false);
	}

	public String add(String className, boolean isStatic) {
		if (map.get(className) == null) {
			String abbr = className.substring(className.lastIndexOf(".") + 1);
			if (map.containsValue(abbr)) {
				map.put(className, className);
			} else
				map.put(className, abbr);
		}
		if (isStatic)
			staticNames.add(className);
		return map.get(className);
	}

	public String add(String pkg, String classSimpleName) {
		return add(pkg + "." + classSimpleName);
	}

	public String add(String pkg, String classSimpleName1,
			String classSimpleName2) {
		return add(pkg + "." + classSimpleName1 + "." + classSimpleName2);
	}

	public String add(Class<?> cls) {
		return add(cls.getName());
	}
}
