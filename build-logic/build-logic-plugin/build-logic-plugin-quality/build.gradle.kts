plugins {
  id("wpcleaner.java-conventions")
  `kotlin-dsl`
}

dependencies {
  api("com.diffplug.spotless:spotless-plugin-gradle:8.5.1")
  api("net.ltgt.errorprone:net.ltgt.errorprone.gradle.plugin:5.1.0")
}
