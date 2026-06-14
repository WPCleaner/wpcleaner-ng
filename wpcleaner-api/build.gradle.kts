plugins { id("wpcleaner.module.java-library") }

dependencies {
  api(enforcedPlatform(project(":wpcleaner-dependencies")))
  api("com.fasterxml.jackson.core:jackson-annotations")
  implementation(project(":wpcleaner-lib-image"))
  implementation("com.fasterxml.jackson.core:jackson-databind")
  implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
  implementation("org.springframework.boot:spring-boot-starter")
  implementation("org.springframework.boot:spring-boot-starter-web")
  runtimeOnly("org.apache.httpcomponents.client5:httpclient5")
  testFixturesApi("org.springframework.boot:spring-boot-starter-test")
}

tasks.test { useJUnitPlatform { excludeTags = setOf("call_api") } }

tasks.register<Test>("apiTest") {
  description = "Runs tests that are actually calling MediaWiki API on a real server"
  group = JavaBasePlugin.VERIFICATION_GROUP
  useJUnitPlatform { includeTags("call_api") }
  testClassesDirs = sourceSets.test.get().output.classesDirs
  classpath = sourceSets.test.get().runtimeClasspath
}
