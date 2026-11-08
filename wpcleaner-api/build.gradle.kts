plugins { id("wpcleaner.module.java-library") }

dependencies {
  api(enforcedPlatform(project(":wpcleaner-dependencies")))
  api(project(":wpcleaner-lib-image"))
  api("com.fasterxml.jackson.core:jackson-annotations")
  api("com.fasterxml.jackson.core:jackson-databind")
  api("org.springframework.boot:spring-boot-starter-web")
  implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
  implementation("org.springframework.boot:spring-boot-starter")
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

val versionResourcesDirectory = layout.buildDirectory.dir("generated/resources/version")
val generateVersionResource =
  tasks.register<WriteProperties>("generateVersionResource") {
    description = "Generates the resource holding the version of the project"
    destinationFile = versionResourcesDirectory.map {
      it.file("org/wpcleaner/api/api/version.properties")
    }
    property("version", project.version.toString())
  }

sourceSets.main {
  resources.srcDir(files(versionResourcesDirectory).builtBy(generateVersionResource))
}
