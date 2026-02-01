pluginManagement {
    includeBuild("build-logic")
    repositories {
        mavenLocal()
        mavenCentral()
        gradlePluginPortal()
    }
}

rootProject.name = "wpcleaner-ng"

fun includeInSubFolder(folder: String, moduleName: String) {
    include(":$moduleName")
    project(":$moduleName").projectDir = file("$folder/$moduleName")
}

include("wpcleaner-dependencies")
include("wpcleaner-api")
includeInSubFolder("wpcleaner-application", "wpcleaner-application-base")
includeInSubFolder("wpcleaner-application", "wpcleaner-application-gui")
includeInSubFolder("wpcleaner-lib", "wpcleaner-lib-image")
