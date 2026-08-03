plugins {
  id("wpcleaner.module.java-library")
  id("org.springframework.boot")
}

tasks.bootRun {
  doFirst {
    val javafxFiles = classpath.filter { it.name.contains("javafx-") || it.name.contains("jfx-") }
    if (!javafxFiles.isEmpty) {
      jvmArgs(
        "--module-path", javafxFiles.asPath,
        "--add-modules", "javafx.controls,jfx.incubator.richtext,jfx.incubator.input",
        "--add-exports", "javafx.graphics/com.sun.javafx.util=ALL-UNNAMED",
        "--add-exports", "javafx.graphics/com.sun.javafx.util=jfx.incubator.richtext"
      )
    }
  }
  jvmArgs(
    "--enable-native-access=ALL-UNNAMED,javafx.graphics",
    "--sun-misc-unsafe-memory-access=allow"
  )
}

val bootJarDirectory = layout.buildDirectory.dir("docker-jar")
val copyBootJar =
  tasks.register<Copy>("copyBootJar") {
    description = "Copy bootJar into $bootJarDirectory"
    dependsOn(tasks.bootJar)
    from(tasks.bootJar)
    into(bootJarDirectory)
    doFirst { delete(bootJarDirectory) }
  }
