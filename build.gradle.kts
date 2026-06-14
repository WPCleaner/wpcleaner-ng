plugins {
    id("com.diffplug.spotless") version "8.6.0" apply false
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
}

tasks {
    /*build {
        dependsOn(gradle.includedBuild("build-logic").task(":build"))
    }*/
    register("publishToMavenLocal") {
        description = "Publishes to local Maven repository"
        dependsOn(gradle.includedBuild("build-logic").task(":publishToMavenLocal"))
    }
    register("publish") {
        description = "Publishes to Maven repository"
        dependsOn(gradle.includedBuild("build-logic").task(":publish"))
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
