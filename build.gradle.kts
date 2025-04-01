plugins {
    id("org.sonarqube") version "6.1.0.5360"
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