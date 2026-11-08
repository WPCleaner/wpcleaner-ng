plugins { id("wpcleaner.module.java-library") }

dependencies {
  api(enforcedPlatform(project(":wpcleaner-dependencies")))
  api(project(":wpcleaner-api"))
  implementation("org.springframework.boot:spring-boot-starter")
  testFixturesApi(testFixtures(project(":wpcleaner-api")))
  testImplementation(testFixtures(project(":wpcleaner-api")))
  testImplementation("org.springframework.boot:spring-boot-starter-test")
}
