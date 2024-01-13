plugins { id("wpcleaner.module.java-library") }

dependencies {
  api(enforcedPlatform(project(":wpcleaner-dependencies")))

  implementation(project(":wpcleaner-lib-image"))
  implementation("org.springframework.boot:spring-boot-starter")
}
