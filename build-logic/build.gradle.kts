import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  embeddedKotlin("jvm") apply false
}

allprojects {
  version = "0.0.1-SNAPSHOT"
  group = "org.wpcleaner.buildlogic"
  repositories {
    mavenLocal()
    mavenCentral()
    gradlePluginPortal()
  }

  tasks.withType<KotlinCompile>().configureEach {
    compilerOptions.jvmTarget.set(JvmTarget.JVM_17)
  }
}

fun aggregateSubModuleTask(taskName: String) {
  val task = tasks.findByPath(taskName)
  if (task != null) {
    task.dependsOn(subprojects.map { "${it.name}:$taskName" })
  } else {
    tasks.register(taskName) {
      dependsOn(subprojects.map { "${it.name}:$taskName" })
    }
  }
}

aggregateSubModuleTask("build")
aggregateSubModuleTask("check")
aggregateSubModuleTask("clean")
aggregateSubModuleTask("publish")
aggregateSubModuleTask("publishToMavenLocal")
