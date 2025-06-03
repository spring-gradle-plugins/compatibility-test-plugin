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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import io.spring.gradle.compatibilitytest.testkit.GradleBuild;
import io.spring.gradle.compatibilitytest.testkit.GradleBuildExtension;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.TaskOutcome;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link CompatibilityTestPlugin}.
 *
 * @author Andy Wilkinson
 */
@ExtendWith(GradleBuildExtension.class)
class CompatibilityTestPluginIntegrationTests {

	private final GradleBuild gradleBuild = new GradleBuild();

	@Test
	void whenOnlyGroupIdIsSetThenVersionsOfAllModulesInTheGroupAreChanged() {
		assertThat(this.gradleBuild.build("resolve", "-PconfigurationName=testCompileClasspath").getOutput())
			.contains("spring-core-5.2.10.RELEASE.jar", "spring-jcl-5.2.10.RELEASE.jar");
		assertThat(this.gradleBuild.build("resolve", "-PconfigurationName=testCompileClasspath_spring_framework_5.3.0")
			.getOutput()).contains("spring-core-5.3.0.jar", "spring-jcl-5.3.0.jar");
		assertThat(this.gradleBuild.build("resolve", "-PconfigurationName=testCompileClasspath_spring_framework_5.3.1")
			.getOutput()).contains("spring-core-5.3.1.jar", "spring-jcl-5.3.1.jar");
		assertThat(this.gradleBuild.build("resolve", "-PconfigurationName=testRuntimeClasspath").getOutput())
			.contains("spring-core-5.2.10.RELEASE.jar", "spring-jcl-5.2.10.RELEASE.jar");
		assertThat(this.gradleBuild.build("resolve", "-PconfigurationName=testRuntimeClasspath_spring_framework_5.3.0")
			.getOutput()).contains("spring-core-5.3.0.jar", "spring-jcl-5.3.0.jar");
		assertThat(this.gradleBuild.build("resolve", "-PconfigurationName=testRuntimeClasspath_spring_framework_5.3.1")
			.getOutput()).contains("spring-core-5.3.1.jar", "spring-jcl-5.3.1.jar");
	}

	@Test
	void whenGroupIdAndArtifactIdAreSetThenOnlyTheVersionOfThatModuleIsChanged() {
		assertThat(this.gradleBuild.build("resolve", "-PconfigurationName=testCompileClasspath").getOutput())
			.contains("spring-core-5.2.10.RELEASE.jar", "spring-jcl-5.2.10.RELEASE.jar");
		assertThat(this.gradleBuild.build("resolve", "-PconfigurationName=testCompileClasspath_spring-jcl_5.3.0")
			.getOutput()).contains("spring-core-5.2.10.RELEASE.jar", "spring-jcl-5.3.0.jar");
		assertThat(this.gradleBuild.build("resolve", "-PconfigurationName=testCompileClasspath_spring-jcl_5.3.1")
			.getOutput()).contains("spring-core-5.2.10.RELEASE.jar", "spring-jcl-5.3.1.jar");
		assertThat(this.gradleBuild.build("resolve", "-PconfigurationName=testRuntimeClasspath").getOutput())
			.contains("spring-core-5.2.10.RELEASE.jar", "spring-jcl-5.2.10.RELEASE.jar");
		assertThat(this.gradleBuild.build("resolve", "-PconfigurationName=testRuntimeClasspath_spring-jcl_5.3.0")
			.getOutput()).contains("spring-core-5.2.10.RELEASE.jar", "spring-jcl-5.3.0.jar");
		assertThat(this.gradleBuild.build("resolve", "-PconfigurationName=testRuntimeClasspath_spring-jcl_5.3.1")
			.getOutput()).contains("spring-core-5.2.10.RELEASE.jar", "spring-jcl-5.3.1.jar");
	}

	@Test
	void whenMultipleDependenciesAreConfiguredThenAllPermutationsAreTested() {
		assertThat(this.gradleBuild.build("resolve", "-PconfigurationName=testCompileClasspath").getOutput()).contains(
				"spring-core-5.2.10.RELEASE.jar", "spring-jcl-5.2.10.RELEASE.jar", "reactor-core-3.3.17.RELEASE.jar",
				"reactive-streams-1.0.3.jar");
		assertThat(this.gradleBuild
			.build("resolve", "-PconfigurationName=testCompileClasspath_spring_framework_5.3.0_reactor_3.4.0")
			.getOutput()).contains("spring-core-5.3.0.jar", "spring-jcl-5.3.0.jar", "reactor-core-3.4.0.jar",
					"reactive-streams-1.0.3.jar");
		assertThat(this.gradleBuild
			.build("resolve", "-PconfigurationName=testCompileClasspath_spring_framework_5.3.0_reactor_3.4.1")
			.getOutput()).contains("spring-core-5.3.0.jar", "spring-jcl-5.3.0.jar", "reactor-core-3.4.1.jar",
					"reactive-streams-1.0.3.jar");
		assertThat(this.gradleBuild
			.build("resolve", "-PconfigurationName=testCompileClasspath_spring_framework_5.3.1_reactor_3.4.0")
			.getOutput()).contains("spring-core-5.3.1.jar", "spring-jcl-5.3.1.jar", "reactor-core-3.4.0.jar",
					"reactive-streams-1.0.3.jar");
		assertThat(this.gradleBuild
			.build("resolve", "-PconfigurationName=testCompileClasspath_spring_framework_5.3.1_reactor_3.4.1")
			.getOutput()).contains("spring-core-5.3.1.jar", "spring-jcl-5.3.1.jar", "reactor-core-3.4.1.jar",
					"reactive-streams-1.0.3.jar");
		assertThat(this.gradleBuild.build("resolve", "-PconfigurationName=testRuntimeClasspath").getOutput()).contains(
				"spring-core-5.2.10.RELEASE.jar", "spring-jcl-5.2.10.RELEASE.jar", "reactor-core-3.3.17.RELEASE.jar",
				"reactive-streams-1.0.3.jar");
		assertThat(this.gradleBuild
			.build("resolve", "-PconfigurationName=testRuntimeClasspath_spring_framework_5.3.0_reactor_3.4.0")
			.getOutput()).contains("spring-core-5.3.0.jar", "spring-jcl-5.3.0.jar", "reactor-core-3.4.0.jar",
					"reactive-streams-1.0.3.jar");
		assertThat(this.gradleBuild
			.build("resolve", "-PconfigurationName=testRuntimeClasspath_spring_framework_5.3.0_reactor_3.4.1")
			.getOutput()).contains("spring-core-5.3.0.jar", "spring-jcl-5.3.0.jar", "reactor-core-3.4.1.jar",
					"reactive-streams-1.0.3.jar");
		assertThat(this.gradleBuild
			.build("resolve", "-PconfigurationName=testRuntimeClasspath_spring_framework_5.3.1_reactor_3.4.0")
			.getOutput()).contains("spring-core-5.3.1.jar", "spring-jcl-5.3.1.jar", "reactor-core-3.4.0.jar",
					"reactive-streams-1.0.3.jar");
		assertThat(this.gradleBuild
			.build("resolve", "-PconfigurationName=testRuntimeClasspath_spring_framework_5.3.1_reactor_3.4.1")
			.getOutput()).contains("spring-core-5.3.1.jar", "spring-jcl-5.3.1.jar", "reactor-core-3.4.1.jar",
					"reactive-streams-1.0.3.jar");
	}

	@Test
	void javaCompileTaskIsCreatedForEachPermutation() {
		BuildResult build = this.gradleBuild.build("dumpJavaCompileTasks");
		String output = build.getOutput();
		assertThat(output).contains("compileCompatibilityTestJava_spring_framework_5.3.0",
				"compileCompatibilityTestJava_spring_framework_5.3.1");
	}

	@Test
	void testsAreExecutedWithoutDeprecationWarnings() throws IOException {
		File projectDir = this.gradleBuild.getProjectDir();
		File exampleTests = new File(projectDir, "src/test/java/example/ExampleTests.java");
		exampleTests.getParentFile().mkdirs();
		try (PrintWriter writer = new PrintWriter(new FileWriter(exampleTests))) {
			writer.println("package example;");
			writer.println("");
			writer.println("import org.junit.jupiter.api.Test;");
			writer.println("");
			writer.println("class ExampleTests {");
			writer.println("");
			writer.println("    @Test");
			writer.println("    void test() {");
			writer.println("    }");
			writer.println("");
			writer.println("}");
		}
		BuildResult result = this.gradleBuild.gradleVersion("8.1").build("build");
		assertThat(result.task(":compatibilityTest_spring_framework_5.3.0").getOutcome())
			.isEqualTo(TaskOutcome.SUCCESS);
		assertThat(result.task(":compatibilityTest_spring_framework_5.3.1").getOutcome())
			.isEqualTo(TaskOutcome.SUCCESS);
		assertThat(result.getOutput()).doesNotContain("deprecated");
	}

}
