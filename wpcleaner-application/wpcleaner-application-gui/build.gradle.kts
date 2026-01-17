plugins { id("wpcleaner.module.java-application") }

dependencies {
  api(enforcedPlatform(project(":wpcleaner-dependencies")))
  implementation(project(":wpcleaner-api"))
  implementation(project(":wpcleaner-lib-image"))
  implementation(project(":wpcleaner-settings-local"))
  implementation("org.springframework.boot:spring-boot-starter")
  compileOnly("org.jetbrains:annotations:13.0")
}
