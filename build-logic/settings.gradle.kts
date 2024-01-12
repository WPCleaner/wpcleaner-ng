pluginManagement {
    repositories {
        mavenLocal()
        mavenCentral()
        gradlePluginPortal()
    }
}

fun includeInSubFolder(folder: String, moduleName: String) {
    include(":$moduleName")
    project(":$moduleName").projectDir = file("$folder/$moduleName")
}

includeBuild("build-logic-conventions")
includeInSubFolder("build-logic-module", "build-logic-module-library")
