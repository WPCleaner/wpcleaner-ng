plugins { id("wpcleaner.module.java-application") }

fun getJavaFxPlatform(): String {
  val os = System.getProperty("os.name").lowercase()
  val arch = System.getProperty("os.arch").lowercase()
  return when {
    os.contains("win") -> "win"
    os.contains("mac") ->
      if (arch.contains("aarch64") || arch.contains("arm64")) "mac-aarch64" else "mac"
    os.contains("nix") || os.contains("nux") ->
      if (arch.contains("aarch64") || arch.contains("arm64")) "linux-aarch64" else "linux"
    else -> "linux"
  }
}

val javafxVersion = "23.0.1"
val javafxPlatform = getJavaFxPlatform()

dependencies {
  api(enforcedPlatform(project(":wpcleaner-dependencies")))
  implementation(project(":wpcleaner-api"))
  implementation(project(":wpcleaner-application-base"))
  implementation(project(":wpcleaner-lib-image"))
  implementation("com.fasterxml.jackson.core:jackson-databind")
  implementation("org.openjfx:javafx-base:${javafxVersion}:${javafxPlatform}")
  implementation("org.openjfx:javafx-controls:${javafxVersion}:${javafxPlatform}")
  implementation("org.openjfx:javafx-graphics:${javafxVersion}:${javafxPlatform}")
  implementation("org.controlsfx:controlsfx")
  implementation("org.springframework.boot:spring-boot-starter")
  implementation("org.springframework.boot:spring-boot-starter-web")
  compileOnly("org.jetbrains:annotations:26.1.0")
  testImplementation(testFixtures(project(":wpcleaner-api")))
}
