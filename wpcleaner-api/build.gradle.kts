plugins { id("wpcleaner.module.java-library") }

dependencies {
  api(enforcedPlatform(project(":wpcleaner-dependencies")))

  implementation(project(":wpcleaner-lib-image"))
  implementation(project(":wpcleaner-settings-local"))
  implementation("com.fasterxml.jackson.core:jackson-annotations")
  implementation("com.fasterxml.jackson.core:jackson-databind")
  implementation("org.springframework.boot:spring-boot-starter")
  implementation("org.springframework.boot:spring-boot-starter-web")
  runtimeOnly("org.apache.httpcomponents.client5:httpclient5")

  testFixturesApi("org.springframework.boot:spring-boot-starter-test")
}

tasks.test { useJUnitPlatform { excludeTags = setOf("call_api") } }

tasks.register<Test>("apiTest") {
  useJUnitPlatform { includeTags("call_api") }
  testClassesDirs = sourceSets.test.get().output.classesDirs
  classpath = sourceSets.test.get().runtimeClasspath
}
