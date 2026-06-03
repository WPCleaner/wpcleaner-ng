plugins {
    id("com.diffplug.spotless") version "8.6.0" apply false
    id("org.sonarqube") version "7.3.0.8198"
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
}

tasks {
    /*build {
        dependsOn(gradle.includedBuild("build-logic").task(":build"))
    }*/
    register("publishToMavenLocal") {
        dependsOn(gradle.includedBuild("build-logic").task(":publishToMavenLocal"))
    }
    register("publish") {
        dependsOn(gradle.includedBuild("build-logic").task(":publish"))
    }
}