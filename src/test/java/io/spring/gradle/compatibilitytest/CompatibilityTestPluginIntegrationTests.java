/*
 * Copyright 2014-2021 the original author or authors.
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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import io.spring.gradle.compatibilitytest.testkit.GradleBuild;
import io.spring.gradle.compatibilitytest.testkit.GradleBuildExtension;

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
		assertThat(this.gradleBuild.build("resolve", "-PconfigurationName=testRuntimeClasspath").getOutput())
				.contains("spring-core-5.2.10.RELEASE.jar", "spring-jcl-5.2.10.RELEASE.jar");
		assertThat(this.gradleBuild.build("resolve", "-PconfigurationName=testRuntimeClasspath_spring_framework_5.3.0")
				.getOutput()).contains("spring-core-5.3.0.jar", "spring-jcl-5.3.0.jar");
		assertThat(this.gradleBuild.build("resolve", "-PconfigurationName=testRuntimeClasspath_spring_framework_5.3.1")
				.getOutput()).contains("spring-core-5.3.1.jar", "spring-jcl-5.3.1.jar");
	}

	@Test
	void whenGroupIdAndArtifactIdAreSetThenOnlyTheVersionOfThatModuleIsChanged() {
		assertThat(this.gradleBuild.build("resolve", "-PconfigurationName=testRuntimeClasspath").getOutput())
				.contains("spring-core-5.2.10.RELEASE.jar", "spring-jcl-5.2.10.RELEASE.jar");
		assertThat(this.gradleBuild.build("resolve", "-PconfigurationName=testRuntimeClasspath_spring-jcl_5.3.0")
				.getOutput()).contains("spring-core-5.2.10.RELEASE.jar", "spring-jcl-5.3.0.jar");
		assertThat(this.gradleBuild.build("resolve", "-PconfigurationName=testRuntimeClasspath_spring-jcl_5.3.1")
				.getOutput()).contains("spring-core-5.2.10.RELEASE.jar", "spring-jcl-5.3.1.jar");
	}

	@Test
	void whenMultipleDependenciesAreConfiguredThenAllPermutationsAreTested() {
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

}
