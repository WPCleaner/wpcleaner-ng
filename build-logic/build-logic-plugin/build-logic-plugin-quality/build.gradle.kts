plugins {
  id("wpcleaner.java-conventions")
  `kotlin-dsl`
}

dependencies {
  api("com.diffplug.spotless:spotless-plugin-gradle:8.10.4")
  api("net.ltgt.errorprone:net.ltgt.errorprone.gradle.plugin:5.2.0")
}
