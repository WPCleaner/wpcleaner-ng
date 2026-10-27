plugins { id("wpcleaner.module.java-library") }

dependencies {
  api(enforcedPlatform(project(":wpcleaner-dependencies")))
  api("com.fasterxml.jackson.core:jackson-annotations")
  api("com.fasterxml.jackson.core:jackson-databind")
  implementation(project(":wpcleaner-lib-image"))
  implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
  implementation("org.springframework.boot:spring-boot-starter")
  implementation("org.springframework.boot:spring-boot-starter-web")
  runtimeOnly("org.apache.httpcomponents.client5:httpclient5")
  testFixturesApi("org.springframework.boot:spring-boot-starter-test")
}

testing {
  suites {
    register<JvmTestSuite>("apiTest") {
      dependencies {
        implementation(project())
        implementation(testFixtures(project()))
      }
      targets.configureEach {
        testTask.configure {
          description = "Runs tests that are actually calling MediaWiki API on a real server"
        }
      }
    }
  }
}
