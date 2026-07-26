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

fun recursiveSubFolderInclusion(parentFolder: File?, folder: File, path: String) {
  val commonCheck =
    parentFolder != null &&
      parentFolder.exists() &&
      folder.name.startsWith("wpcleaner") &&
      File(folder, "build.gradle.kts").exists()
  if (commonCheck) {
    if (!parentFolder.name.equals(rootDir.name)) {
      val folderNameCalculated = path.removePrefix("/").removeSuffix("/${folder.name}")
      includeInSubFolder(folderNameCalculated, folder.name)
    } else {
      include(folder.name)
    }
  } else {
    folder
      .listFiles()
      ?.filter { it.isDirectory && it.name.startsWith("wpcleaner") }
      ?.forEach { recursiveSubFolderInclusion(folder, it, "$path/${it.name}") }
  }
}

recursiveSubFolderInclusion(null, rootDir, "")
