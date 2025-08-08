plugins { id("wpcleaner.module.java-library") }

dependencies {
  api(enforcedPlatform(project(":wpcleaner-dependencies")))

  implementation(project(":wpcleaner-lib-image"))
  implementation(project(":wpcleaner-settings-local"))
  implementation("org.springframework.boot:spring-boot-starter")

  testFixturesApi("org.springframework.boot:spring-boot-starter-test")
}
