plugins { id("wpcleaner.module.java-application") }

dependencies {
  api(enforcedPlatform(project(":wpcleaner-dependencies")))
  implementation(project(":wpcleaner-api"))
  implementation(project(":wpcleaner-application-base"))
  implementation(project(":wpcleaner-lib-check"))
  implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml")
  implementation("org.apache.commons:commons-compress")
  implementation("org.springframework.boot:spring-boot-starter")
  testImplementation(testFixtures(project(":wpcleaner-api")))
  testImplementation(testFixtures(project(":wpcleaner-application-base")))
  testImplementation("org.springframework.boot:spring-boot-starter-test")
}
