plugins {
  id("wpcleaner.java-conventions")
  `kotlin-dsl`
}

dependencies {
  api(project(":build-logic-module-library"))
  api(project(":build-logic-plugin-quality"))

  implementation("org.springframework.boot:spring-boot-gradle-plugin:3.2.1")
}