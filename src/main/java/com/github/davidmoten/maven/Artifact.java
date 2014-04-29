package com.github.davidmoten.maven;

public class Artifact {
	private final String artifactId;
	private final String groupId;
	private final String version;
	private final String type;

	public Artifact(String groupId, String artifactId, String version,
			String type) {
		this.artifactId = artifactId;
		this.groupId = groupId;
		this.version = version;
		this.type = type;
	}

	public String getArtifactId() {
		return artifactId;
	}

	public String getGroupId() {
		return groupId;
	}

	public String getVersion() {
		return version;
	}

	public String getType() {
		return type;
	}

}
