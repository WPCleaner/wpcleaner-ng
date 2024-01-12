plugins {
  id("base")
  id("wpcleaner.internal.spotless-kotlin-gradle")
}

project.plugins.withId("java") {
  apply(plugin = "wpcleaner.internal.error-prone")
  apply(plugin = "wpcleaner.internal.java-warning")
  apply(plugin = "wpcleaner.internal.pmd")
  apply(plugin = "wpcleaner.internal.spotless-java")
  apply(plugin = "wpcleaner.internal.spotless-json")
  apply(plugin = "wpcleaner.internal.spotless-kotlin")
  apply(plugin = "wpcleaner.internal.spotless-yaml")
}

project.plugins.withId("java-library") { }
