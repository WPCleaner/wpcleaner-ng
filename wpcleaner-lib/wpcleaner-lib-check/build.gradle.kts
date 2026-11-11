plugins { id("wpcleaner.module.java-library") }

dependencies {
  api(enforcedPlatform(project(":wpcleaner-dependencies")))
  api(project(":wpcleaner-api"))
  implementation("org.springframework.boot:spring-boot-starter")
  testFixturesApi(testFixtures(project(":wpcleaner-api")))
  testImplementation(testFixtures(project(":wpcleaner-api")))
  testImplementation("org.springframework.boot:spring-boot-starter-test")
}

testing {
  suites {
    register<JvmTestSuite>("apiTest") {
      dependencies {
        implementation(project())
        implementation("org.springframework.boot:spring-boot-starter-test")
      }
      targets.configureEach {
        testTask.configure {
          description =
            "Runs tests that are actually calling MediaWiki API and Check Wiki on real servers"
        }
      }
    }
  }
}
