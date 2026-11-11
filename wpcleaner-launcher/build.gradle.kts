plugins { id("wpcleaner.module.java-library") }

version = "1.0.0"

dependencies {
  api(enforcedPlatform(project(":wpcleaner-dependencies")))
  implementation("com.fasterxml.jackson.core:jackson-annotations")
  implementation("com.fasterxml.jackson.core:jackson-databind")
  compileOnly("com.google.errorprone:error_prone_annotations")
  testImplementation("org.assertj:assertj-core")
  testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.processResources {
  from(
    project(":wpcleaner-lib-image")
      .file("src/main/resources/images/256px/commons/nuvola-web-broom.png"),
  ) {
    into("org/wpcleaner/launcher")
    rename { "logo.png" }
  }
}

val launcherName = "WPCleanerNG"
val hostName = System.getProperty("os.name").lowercase()
val hostArchitecture = System.getProperty("os.arch").lowercase()
val launcherOperatingSystem =
  when {
    hostName.contains("mac") -> "macos"
    hostName.contains("win") -> "windows"
    else -> "linux"
  }
val launcherArchitecture =
  if (hostArchitecture.contains("aarch64") || hostArchitecture.contains("arm64")) "aarch64"
  else "x64"
val launcherArchiveName = "wpcleaner-ng-launcher-$launcherOperatingSystem-$launcherArchitecture"
val launcherIcon =
  when (launcherOperatingSystem) {
    "macos" -> file("src/main/packaging/$launcherName.icns")
    "windows" -> file("src/main/packaging/$launcherName.ico")
    else ->
      project(":wpcleaner-lib-image")
        .file("src/main/resources/images/256px/commons/nuvola-web-broom.png")
  }
val launcherRuntimeModules =
  listOf(
    "java.se",
    "jdk.charsets",
    "jdk.crypto.cryptoki",
    "jdk.localedata",
    "jdk.management",
    "jdk.unsupported",
    "jdk.zipfs",
  )

val jpackageInputDirectory = layout.buildDirectory.dir("jpackage/input")
val jpackageImageDirectory = layout.buildDirectory.dir("jpackage/image")
val launcherDistributionDirectory = layout.buildDirectory.dir("distributions")

val prepareJpackageInput =
  tasks.register<Sync>("prepareJpackageInput") {
    description = "Gathers the jars of the launcher for jpackage"
    from(tasks.jar)
    from(configurations.runtimeClasspath)
    into(jpackageInputDirectory)
  }

val cleanJpackageImage =
  tasks.register<Delete>("cleanJpackageImage") {
    description = "Deletes the previous application image of the launcher"
    delete(jpackageImageDirectory)
  }

val jpackageImage =
  tasks.register<Exec>("jpackageImage") {
    group = "distribution"
    description = "Builds the application image of the launcher with its Java runtime"
    dependsOn(prepareJpackageInput, cleanJpackageImage)
    inputs.dir(jpackageInputDirectory)
    inputs.file(launcherIcon)
    outputs.dir(jpackageImageDirectory)
    executable =
      javaToolchains
        .launcherFor(java.toolchain)
        .get()
        .metadata
        .installationPath
        .file("bin/jpackage")
        .asFile
        .absolutePath
    args(
      "--type",
      "app-image",
      "--name",
      launcherName,
      "--app-version",
      version.toString(),
      "--vendor",
      "WPCleaner",
      "--input",
      jpackageInputDirectory.get().asFile.absolutePath,
      "--main-jar",
      tasks.jar.get().archiveFileName.get(),
      "--main-class",
      "org.wpcleaner.launcher.Launcher",
      "--dest",
      jpackageImageDirectory.get().asFile.absolutePath,
      "--icon",
      launcherIcon.absolutePath,
      "--add-modules",
      launcherRuntimeModules.joinToString(","),
      "--jlink-options",
      "--strip-debug --no-man-pages --no-header-files --compress zip-6",
      "--java-options",
      "-Xmx256m",
    )
  }

when (launcherOperatingSystem) {
  "windows" ->
    tasks.register<Zip>("packageLauncher") {
      group = "distribution"
      description = "Builds the archive of the launcher"
      from(jpackageImage)
      archiveFileName = "$launcherArchiveName.zip"
      destinationDirectory = launcherDistributionDirectory
    }
  "macos" ->
    tasks.register<Exec>("packageLauncher") {
      group = "distribution"
      description = "Builds the archive of the launcher"
      dependsOn(jpackageImage)
      val archive = launcherDistributionDirectory.map { it.file("$launcherArchiveName.zip") }
      inputs.dir(jpackageImageDirectory)
      outputs.file(archive)
      workingDir(jpackageImageDirectory)
      commandLine(
        "ditto",
        "-c",
        "-k",
        "--keepParent",
        "$launcherName.app",
        archive.get().asFile.absolutePath,
      )
    }
  else ->
    tasks.register<Exec>("packageLauncher") {
      group = "distribution"
      description = "Builds the archive of the launcher"
      dependsOn(jpackageImage)
      val archive = launcherDistributionDirectory.map { it.file("$launcherArchiveName.tar.gz") }
      inputs.dir(jpackageImageDirectory)
      outputs.file(archive)
      workingDir(jpackageImageDirectory)
      commandLine("tar", "-czf", archive.get().asFile.absolutePath, launcherName)
    }
}
