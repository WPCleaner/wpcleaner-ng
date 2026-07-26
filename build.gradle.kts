plugins {
  id("wpcleaner.quality")
  id("org.sonarqube") version "7.3.1.8318"
}

allprojects {
  group = "org.wpcleaner"
  version = "0.0.1-SNAPSHOT"

  repositories {
    mavenLocal()
    mavenCentral()
  }
}

subprojects {
  tasks
    .matching { it.name == "compileJava" }
    .configureEach {
      mustRunAfter(rootProject.tasks.named("generatePotFile"))
    }
}

tasks {
  register("publishToMavenLocal") {
    description = "Publishes to local Maven repository"
    dependsOn(gradle.includedBuild("build-logic").task(":publishToMavenLocal"))
  }
  register("publish") {
    description = "Publishes to Maven repository"
    dependsOn(gradle.includedBuild("build-logic").task(":publish"))
  }
  register("generatePotFile") {
    group = "translation"
    description = "Generates the WPCleaner.pot file using xgettext"

    val javaFiles =
      files(
        provider {
          (subprojects + rootProject).map { proj ->
            proj.fileTree(proj.projectDir) {
              include("**/*.java")
              exclude("**/build/**")
              exclude("**/.gradle/**")
            }
          }
        }
      )
    inputs.files(javaFiles)
    val relativeDir = rootDir

    val outputFile = file("wpcleaner-translations/src/main/resources/WPCleaner.pot")
    outputs.file(outputFile)

    mustRunAfter(
      provider {
        subprojects.map { proj ->
          proj.tasks.matching { it.name.startsWith("spotless") }
        }
      }
    )

    doLast {
      outputFile.parentFile.mkdirs()
      val tempFile = temporaryDir.resolve("java-files.txt")
      val filePaths =
        javaFiles.files.filter { it.isFile }.map { it.relativeTo(relativeDir) }.distinct()
      tempFile.writeText(filePaths.joinToString("\n"))

      val process =
        ProcessBuilder(
            "xgettext",
            "--copyright-holder=Nicolas Vervelle",
            "--files-from=${tempFile.absolutePath}",
            "--from-code=utf-8",
            "--keyword=GT._T",
            "--language=Java",
            "--msgid-bugs-address=https://phabricator.wikimedia.org/project/board/4842/",
            "--no-wrap",
            "--output=${outputFile.absolutePath}",
            "--package-name=WPCleaner NG",
          )
          .redirectErrorStream(true)
          .start()

      val output = process.inputStream.bufferedReader().readText()
      val exitCode = process.waitFor()
      if (exitCode != 0) {
        throw GradleException("xgettext failed with exit code $exitCode: $output")
      }
    }
  }
}

sonarqube {
  properties {
    property("sonar.issue.ignore.multicriteria", "java-S4036,java-S7466")
    // Ignore rule Searching OS commands in PATH is security-sensitive
    property("sonar.issue.ignore.multicriteria.java-S4036.ruleKey", "java:S4036")
    property("sonar.issue.ignore.multicriteria.java-S4036.resourceKey", "**/*.java")
    // Ignore rule Use `var` instead of a type with unnamed variable _
    property("sonar.issue.ignore.multicriteria.java-S7466.ruleKey", "java:S7466")
    property("sonar.issue.ignore.multicriteria.java-S7466.resourceKey", "**/*.java")
  }
}
