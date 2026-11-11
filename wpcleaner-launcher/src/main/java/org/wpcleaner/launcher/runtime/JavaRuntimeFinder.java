package org.wpcleaner.launcher.runtime;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.wpcleaner.launcher.platform.OperatingSystem;

public final class JavaRuntimeFinder {

  private static final Pattern PATH_SEPARATOR =
      Pattern.compile(File.pathSeparator, Pattern.LITERAL);

  private final OperatingSystem operatingSystem;
  private final List<Path> candidateHomes;

  public JavaRuntimeFinder(final OperatingSystem operatingSystem, final List<Path> candidateHomes) {
    this.operatingSystem = operatingSystem;
    this.candidateHomes = List.copyOf(candidateHomes);
  }

  public static JavaRuntimeFinder forSystem(final OperatingSystem operatingSystem) {
    final List<Path> homes = new ArrayList<>();
    homes.add(Paths.get(System.getProperty("java.home")));
    Optional.ofNullable(System.getenv("JAVA_HOME"))
        .filter(home -> !home.isBlank())
        .map(Paths::get)
        .ifPresent(homes::add);
    homes.addAll(homesFromPath(operatingSystem));
    for (final Path folder : installationFolders(operatingSystem)) {
      homes.addAll(homesFromInstallationFolder(folder));
    }
    return new JavaRuntimeFinder(operatingSystem, homes);
  }

  public Optional<JavaRuntime> find(final int minimumVersion) {
    return candidateHomes.stream()
        .map(this::inspect)
        .flatMap(Optional::stream)
        .filter(runtime -> runtime.version() >= minimumVersion)
        .findFirst();
  }

  private Optional<JavaRuntime> inspect(final Path home) {
    final Path executable = home.resolve("bin").resolve(executableName(operatingSystem));
    final Path release = home.resolve("release");
    if (!Files.isRegularFile(executable) || !Files.isRegularFile(release)) {
      return Optional.empty();
    }
    final Properties properties = new Properties();
    try (Reader reader = Files.newBufferedReader(release)) {
      properties.load(reader);
      final String version = properties.getProperty("JAVA_VERSION", "").replace("\"", "");
      return Optional.of(
          new JavaRuntime(home, Runtime.Version.parse(version).feature(), executable));
    } catch (final IOException | IllegalArgumentException e) {
      return Optional.empty();
    }
  }

  private static String executableName(final OperatingSystem operatingSystem) {
    return operatingSystem == OperatingSystem.WINDOWS ? "javaw.exe" : "java";
  }

  private static List<Path> homesFromPath(final OperatingSystem operatingSystem) {
    final String path = System.getenv("PATH");
    if (path == null) {
      return List.of();
    }
    final List<Path> homes = new ArrayList<>();
    for (final String folder : PATH_SEPARATOR.splitAsStream(path).toList()) {
      if (folder.isBlank()) {
        continue;
      }
      final Path executable = Paths.get(folder).resolve(executableName(operatingSystem));
      if (Files.isRegularFile(executable)) {
        homeOfExecutable(executable).ifPresent(homes::add);
      }
    }
    return homes;
  }

  private static Optional<Path> homeOfExecutable(final Path executable) {
    try {
      return Optional.ofNullable(executable.toRealPath().getParent()).map(Path::getParent);
    } catch (final IOException e) {
      return Optional.empty();
    }
  }

  private static List<Path> installationFolders(final OperatingSystem operatingSystem) {
    return switch (operatingSystem) {
      case LINUX -> List.of(Paths.get("/usr/lib/jvm"), Paths.get("/usr/java"));
      case MACOS ->
          List.of(
              Paths.get("/Library/Java/JavaVirtualMachines"),
              Paths.get(System.getProperty("user.home"), "Library/Java/JavaVirtualMachines"));
      case WINDOWS ->
          Stream.of("ProgramFiles", "ProgramW6432")
              .map(System::getenv)
              .flatMap(Stream::ofNullable)
              .distinct()
              .flatMap(
                  programs ->
                      Stream.of("Java", "Eclipse Adoptium", "Microsoft", "Zulu")
                          .map(vendor -> Paths.get(programs, vendor)))
              .toList();
    };
  }

  private static List<Path> homesFromInstallationFolder(final Path folder) {
    if (!Files.isDirectory(folder)) {
      return List.of();
    }
    try (Stream<Path> installations = Files.list(folder)) {
      return installations
          .sorted()
          .flatMap(
              installation ->
                  Stream.of(installation, installation.resolve("Contents").resolve("Home")))
          .toList()
          .reversed();
    } catch (final IOException e) {
      return List.of();
    }
  }
}
