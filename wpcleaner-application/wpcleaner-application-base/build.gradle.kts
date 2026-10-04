plugins { id("wpcleaner.module.java-library") }

tasks.processResources { from(rootProject.file("gradle.properties")) }

dependencies {
  api(enforcedPlatform(project(":wpcleaner-dependencies")))
  implementation(project(":wpcleaner-api"))
  implementation(project(":wpcleaner-lib-image"))
  implementation("com.fasterxml.jackson.core:jackson-databind")
  implementation("org.springframework.boot:spring-boot-starter")
  implementation("org.springframework.boot:spring-boot-starter-web")
  testImplementation(testFixtures(project(":wpcleaner-api")))
}
