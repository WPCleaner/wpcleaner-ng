plugins { id("wpcleaner.module.java-application") }

dependencies {
  api(enforcedPlatform(project(":wpcleaner-dependencies")))

  implementation(project(":wpcleaner-gui-core"))
  implementation("org.springframework.boot:spring-boot-starter")

  runtimeOnly(project(":wpcleaner-gui-swing-login"))
}
