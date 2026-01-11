plugins {
  id("wpcleaner.java-conventions")
  `kotlin-dsl`
}

dependencies {
  api("com.diffplug.spotless:spotless-plugin-gradle:8.1.0")
  api("net.ltgt.errorprone:net.ltgt.errorprone.gradle.plugin:4.3.0")
}
