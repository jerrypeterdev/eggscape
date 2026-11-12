# Contributing to EggScape

## Development

Build with Java 21 and Gradle 8.10 using `gradle build`. On the existing portable Windows setup, use `build.bat build --offline`.

`start-test.bat` launches the local development client with testing controls. `start-release-test.bat` previews the normal player interface. The three test controls are absent and their actions are blocked in ordinary RuneLite launches.

Test tools require the explicit JVM property `eggscape.developerTools=true`; it is enabled only by the local development task. The test launcher and tests are excluded from the runtime JAR.
