plugins {
  id("wpcleaner.java-conventions")
  `kotlin-dsl`
}

dependencies {
  api(project(":build-logic-plugin-quality"))
}