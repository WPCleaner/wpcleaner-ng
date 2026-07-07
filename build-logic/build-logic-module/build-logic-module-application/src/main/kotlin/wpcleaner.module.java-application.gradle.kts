plugins {
  id("wpcleaner.module.java-library")
  id("org.springframework.boot")
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
