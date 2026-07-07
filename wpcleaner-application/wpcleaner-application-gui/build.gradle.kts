plugins { id("wpcleaner.module.java-application") }

dependencies {
  api(enforcedPlatform(project(":wpcleaner-dependencies")))
  implementation(project(":wpcleaner-api"))
  implementation(project(":wpcleaner-application-base"))
  implementation(project(":wpcleaner-lib-image"))
  implementation("com.fasterxml.jackson.core:jackson-databind")
  implementation("org.springframework.boot:spring-boot-starter")
  implementation("org.springframework.boot:spring-boot-starter-web")
  compileOnly("org.jetbrains:annotations:26.1.0")
  testImplementation(testFixtures(project(":wpcleaner-api")))
}
