plugins { id("wpcleaner.module.java-library") }

tasks.processResources { from(rootProject.file("gradle.properties")) }

dependencies {
  api(enforcedPlatform(project(":wpcleaner-dependencies")))
  implementation(project(":wpcleaner-api"))
  implementation("org.springframework.boot:spring-boot-starter")
  testFixturesApi(project(":wpcleaner-api"))
  testFixturesApi(project(":wpcleaner-lib-check"))
  testFixturesApi(testFixtures(project(":wpcleaner-api")))
  testFixturesApi(testFixtures(project(":wpcleaner-lib-check")))
  testImplementation(testFixtures(project(":wpcleaner-api")))
}
