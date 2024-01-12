import net.ltgt.gradle.errorprone.CheckSeverity
import net.ltgt.gradle.errorprone.errorprone
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import java.nio.charset.StandardCharsets

plugins {
  id("java-library")
  id("wpcleaner.quality")
  id("java-test-fixtures")
  id("jacoco")
}

val javaVersion: String =
    System.getProperty("build.java-version", JavaVersion.VERSION_17.toString())

java { toolchain { languageVersion.set(JavaLanguageVersion.of(javaVersion)) } }

dependencies {
  testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<JavaCompile>().configureEach {
  sourceCompatibility = javaVersion
  targetCompatibility = javaVersion
  options.compilerArgs.add("-parameters")
  options.encoding = StandardCharsets.UTF_8.name()
  if (!name.contains("Test")) {
    options.errorprone {
      check("NullAway", CheckSeverity.ERROR)
      option("NullAway:AnnotatedPackages", "org.wpcleaner")
    }
  }
}

tasks.compileJava { options.javaModuleVersion.set(provider { version as String }) }

tasks.test { useJUnitPlatform() }

tasks.withType<Test>().configureEach {
  jvmArgs("-XX:+ShowCodeDetailsInExceptionMessages", "-Duser.language=US")
  testLogging {
    events(TestLogEvent.FAILED)
    exceptionFormat = TestExceptionFormat.FULL
  }
}

tasks.javadoc {
  options {
    this as StandardJavadocDocletOptions
    links =
        listOf(
            "https://docs.oracle.com/en/java/javase/14/docs/api/",
            "https://docs.spring.io/spring-boot/docs/current/api/"
        )
  }
}