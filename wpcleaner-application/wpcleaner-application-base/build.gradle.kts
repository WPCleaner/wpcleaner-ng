plugins { id("wpcleaner.module.java-library") }

tasks.processResources { from(rootProject.file("gradle.properties")) }

dependencies {
  api(enforcedPlatform(project(":wpcleaner-dependencies")))
  implementation(project(":wpcleaner-api"))
  implementation("org.springframework.boot:spring-boot-starter")
  testImplementation(testFixtures(project(":wpcleaner-api")))
}
