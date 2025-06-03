/*
 * Copyright 2014-2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.spring.gradle.compatibilitytest;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.DependencyResolveDetails;
import org.gradle.api.artifacts.ModuleVersionSelector;
import org.gradle.api.plugins.JavaBasePlugin;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.compile.JavaCompile;
import org.gradle.api.tasks.testing.Test;

import io.spring.gradle.compatibilitytest.CompatibilityMatrix.DependencyVersion;

/**
 * Gradle plugin for compatibility testing.
 *
 * @author Andy Wilkinson
 */
public class CompatibilityTestPlugin implements Plugin<Project> {

	@Override
	public void apply(Project project) {
		CompatibilityMatrix testMatrix = new CompatibilityMatrix();
		CompatibilityTestExtension extension = new CompatibilityTestExtension(testMatrix);
		project.getExtensions().add("compatibilityTest", extension);
		project.afterEvaluate((evaluated) -> configure(project, testMatrix, extension));
	}

	private void configure(Project project, CompatibilityMatrix testMatrix, CompatibilityTestExtension extension) {
		List<Set<DependencyVersion>> matrixEntries = testMatrix.getEntries();
		if (matrixEntries.isEmpty()) {
			return;
		}
		CartesianProduct.of(matrixEntries)
			.forEach((dependencyVersions) -> configureTasks(project, dependencyVersions, extension));
	}

	private void configureTasks(Project project, List<DependencyVersion> dependencyVersions,
			CompatibilityTestExtension extension) {
		String identifier = dependencyVersions.stream()
			.map(DependencyVersion::getIdentifier)
			.collect(Collectors.joining("_"));
		JavaCompile javaCompile = project.getTasks()
			.create("compileCompatibilityTestJava_" + identifier, JavaCompile.class,
					(task) -> configureJavaCompileTask(project, task, identifier, dependencyVersions));
		Test compatibilityTest = project.getTasks()
			.create("compatibilityTest_" + identifier, Test.class,
					(task) -> configureMatrixTestTask(project, task, javaCompile, identifier, dependencyVersions));
		if (extension.isUseJUnitPlatform()) {
			compatibilityTest.useJUnitPlatform();
		}
		project.getTasks().getByName(JavaBasePlugin.CHECK_TASK_NAME).dependsOn(compatibilityTest);
	}

	private void configureJavaCompileTask(Project project, JavaCompile javaCompile, String identifier,
			List<DependencyVersion> dependencyVersions) {
		javaCompile.setDescription("Compiles test Java source with "
				+ dependencyVersions.stream().map(DependencyVersion::getDescription).collect(Collectors.joining(", ")));
		SourceSetContainer sourceSets = project.getExtensions().getByType(JavaPluginExtension.class).getSourceSets();
		SourceSet testSourceSet = sourceSets.getByName(SourceSet.TEST_SOURCE_SET_NAME);
		javaCompile.setSource(testSourceSet.getAllJava());
		javaCompile.getDestinationDirectory()
			.set(project.getLayout().getBuildDirectory().dir("classes/java/compatibilityTest_" + identifier));
		String compileClasspathConfigurationName = testSourceSet.getCompileClasspathConfigurationName();
		Configuration configuration = project.getConfigurations()
			.create(compileClasspathConfigurationName + "_" + identifier);
		configuration.extendsFrom(project.getConfigurations().getByName(compileClasspathConfigurationName));
		configuration.getResolutionStrategy()
			.eachDependency((details) -> dependencyVersions.stream()
				.filter((dependencyVersion) -> matches(dependencyVersion, details))
				.forEach((dependencyVersion) -> details.useVersion(dependencyVersion.getVersion())));
		javaCompile.setClasspath(
				project.files(sourceSets.getByName(SourceSet.MAIN_SOURCE_SET_NAME).getOutput(), configuration));
	}

	private void configureMatrixTestTask(Project project, Test compatibilityTest, JavaCompile javaCompile,
			String identifier, List<DependencyVersion> dependencyVersions) {
		compatibilityTest.setDescription("Runs the unit tests with "
				+ dependencyVersions.stream().map(DependencyVersion::getDescription).collect(Collectors.joining(", ")));
		compatibilityTest.setGroup(JavaBasePlugin.VERIFICATION_GROUP);
		SourceSetContainer sourceSets = project.getExtensions().getByType(JavaPluginExtension.class).getSourceSets();
		SourceSet testSourceSet = sourceSets.getByName(SourceSet.TEST_SOURCE_SET_NAME);
		compatibilityTest.dependsOn(testSourceSet.getProcessResourcesTaskName());
		String runtimeClasspathConfigurationName = testSourceSet.getRuntimeClasspathConfigurationName();
		Configuration configuration = project.getConfigurations()
			.create(runtimeClasspathConfigurationName + "_" + identifier);
		configuration.extendsFrom(project.getConfigurations().getByName(runtimeClasspathConfigurationName));
		configuration.getResolutionStrategy()
			.eachDependency((details) -> dependencyVersions.stream()
				.filter((dependencyVersion) -> matches(dependencyVersion, details))
				.forEach((dependencyVersion) -> details.useVersion(dependencyVersion.getVersion())));
		compatibilityTest.setTestClassesDirs(testSourceSet.getOutput().getClassesDirs());
		compatibilityTest.setClasspath(
				project.files(javaCompile.getDestinationDirectory(), testSourceSet.getOutput().getResourcesDir(),
						sourceSets.getByName(SourceSet.MAIN_SOURCE_SET_NAME).getOutput(), configuration));
	}

	private boolean matches(DependencyVersion dependencyVersion, DependencyResolveDetails details) {
		ModuleVersionSelector selector = details.getRequested();
		return dependencyVersion.getGroupId().equals(selector.getGroup()) && (dependencyVersion.getArtifactId() == null
				|| dependencyVersion.getArtifactId().equals(selector.getName()));
	}

}
