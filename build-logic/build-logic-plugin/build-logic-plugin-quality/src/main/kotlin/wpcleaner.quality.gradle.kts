plugins {
  id("base")
  id("wpcleaner.internal.spotless-kotlin-gradle")
}

project.plugins.withId("java") {
  plugins {
    id("wpcleaner.internal.error-prone")
    id("wpcleaner.internal.java-warning")
    id("wpcleaner.internal.pmd")
    id("wpcleaner.internal.spotless-java")
    id("wpcleaner.internal.spotless-json")
    id("wpcleaner.internal.spotless-kotlin")
    id("wpcleaner.internal.spotless-yaml")
  }
}

project.plugins.withId("java-library") { }

project.plugins.withId("com.diffplug.spotless") {
  tasks.withType<com.diffplug.gradle.spotless.SpotlessTask>().configureEach {
    notCompatibleWithConfigurationCache("Spotless has classloader issues under configuration cache when run with clean")
  }
}

