package com.github.davidmoten.maven;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

public class MojoExecutorBuilderTest {

	private static File generatedSources = new File("target/generated-sources");

	@Test
	public void test() {
		MojoExecutorBuilder b = new MojoExecutorBuilder();
		generate(b);
		assertTrue(generatedSources.exists());
	}

	private static void generate(MojoExecutorBuilder b) {
		try {
			FileUtils.deleteDirectory(generatedSources);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		b.generateSource(generatedSources, new Artifact(
				"org.apache.maven.plugins", "maven-dependency-plugin", "2.8",
				"maven-plugin"), "maven.plugin.dependency",
				MojoExecutorBuilder.class.getResourceAsStream("/plugin.xml"));
	}
}
