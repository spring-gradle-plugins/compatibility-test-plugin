# Compatibility Test Plugin

A Gradle plugin for testing a project's compatibility with different versions of its dependencies.


## Applying the Plugin

The plugin is published to https://repo.spring.io.
Depending on the version you wish to use, it will be availble from the `plugins-snapshot-local` or `plugins-release` repository.

The first step in using the plugin is to make the necessary repository available for plugin resolution.
This is done by configuring a plugin management repository in `settings.gradle`, as shown in the following example:

```
pluginManagement {
	repositories {
		gradlePluginPortal()
		maven { url 'https://repo.spring.io/plugins-release' }
	}
}
```

In the example above, `gradlePluginPortal()` is declared to allow other plugins to continue to be resolved from the portal.
The plugin can then be applied to a project in the usual manner, as shown in the following example:

```
plugins {
	id "io.spring.compatibility-test" version "<<version>>"
}
```


## Configuring the Plugin

With the plugin applied, its DSL can be used to configure compatibility testing, as shown in the following example:

```
compatibilityTest {
	dependency('Spring Framework') { springFramework ->
		springFramework.groupId = 'org.springframework'
		springFramework.versions = [
			'5.3.0',
			'5.3.1'
		]
	}
}
```

The configuration above will create two new `Test` tasks.
One task will use version 5.3.0 of any dependencies in the `org.springframework` group and that other will use version 5.3.1.
The `check` task is configured to depend on each of the new tasks.

When configuring compatibility testing, multiple dependencies can be configured, as shown in the following example:

```
compatibilityTest {
	dependency('Spring Framework') { springFramework ->
		springFramework.groupId = 'org.springframework'
		springFramework.versions = [
			'5.3.0',
			'5.3.1'
		]
	}
	dependency('Reactor') { reactor ->
		reactor.groupId = 'io.projectreactor'
		reactor.versions = [
			'3.4.0',
			'3.4.1'
		]
	}
}
```

The configuration above will create four new `Test` tasks, one for each combination of the configured dependency versions:

| Spring Framework | Reactor |
| ---------------- | ------- |
| 5.3.0            | 3.4.0   |
| 5.3.0            | 3.4.1   |
| 5.3.1            | 3.4.0   |
| 5.3.1            | 3.4.1   |
