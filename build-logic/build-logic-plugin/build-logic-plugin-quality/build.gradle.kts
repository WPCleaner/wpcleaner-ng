plugins {
  id("wpcleaner.java-conventions")
  `kotlin-dsl`
}

dependencies {
  api("com.diffplug.spotless:spotless-plugin-gradle:6.25.0")
  api("net.ltgt.errorprone:net.ltgt.errorprone.gradle.plugin:3.1.0")
}
