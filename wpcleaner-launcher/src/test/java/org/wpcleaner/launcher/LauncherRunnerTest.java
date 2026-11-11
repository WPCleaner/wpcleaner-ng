package org.wpcleaner.launcher;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.wpcleaner.launcher.install.ReleaseUpdater;
import org.wpcleaner.launcher.platform.Architecture;
import org.wpcleaner.launcher.platform.LauncherDirectories;
import org.wpcleaner.launcher.platform.OperatingSystem;
import org.wpcleaner.launcher.platform.Platform;
import org.wpcleaner.launcher.release.ReleaseRepository;
import org.wpcleaner.launcher.runtime.JavaRuntimeFinder;
import org.wpcleaner.launcher.start.ReleaseStarter;

class LauncherRunnerTest {

  private static final Platform PLATFORM = new Platform(OperatingSystem.LINUX, Architecture.X64);
  private static final String FIRST_VERSION = "0.0.5";
  private static final String SECOND_VERSION = "0.0.6";

  @TempDir private Path temporaryFolder;

  private ReleaseServer server;
  private LauncherDirectories directories;
  private Path javaExecutable;
  private RecordingDisplay display;
  private RecordingProcessStarter processStarter;
  private AtomicInteger createdShortcuts;

  @BeforeEach
  void prepare() throws IOException {
    server = new ReleaseServer();
    directories = new LauncherDirectories(temporaryFolder.resolve("launcher"));
    final Path javaHome = temporaryFolder.resolve("java");
    javaExecutable = javaHome.resolve("bin").resolve("java");
    Files.createDirectories(javaExecutable.getParent());
    Files.createFile(javaExecutable);
    Files.writeString(javaHome.resolve("release"), "JAVA_VERSION=\"25.0.4\"\n");
    display = new RecordingDisplay();
    processStarter = new RecordingProcessStarter();
    createdShortcuts = new AtomicInteger();
  }

  @AfterEach
  void stopServer() {
    server.close();
  }

  @DisplayName("Install and start the latest release on first start")
  @Test
  void installOnFirstStart() throws IOException, InterruptedException {
    // GIVEN
    server.publish(FIRST_VERSION, "first");

    // WHEN
    run("--wiki", "en");

    // THEN
    final Path jar = jar(FIRST_VERSION);
    Assertions.assertThat(jar).hasContent("first");
    Assertions.assertThat(processStarter.commands())
        .containsExactly(
            List.of(
                javaExecutable.toString(),
                "--enable-native-access=ALL-UNNAMED",
                "-jar",
                jar.toString(),
                "--wiki",
                "en"));
    Assertions.assertThat(display.errors()).isEmpty();
    Assertions.assertThat(display.isProgressOpen()).isFalse();
  }

  @DisplayName("Propose the desktop shortcut only once")
  @Test
  void proposeShortcutOnce() throws IOException, InterruptedException {
    // GIVEN
    server.publish(FIRST_VERSION, "first");

    // WHEN
    run();
    run();

    // THEN
    Assertions.assertThat(display.questions()).hasSize(1);
    Assertions.assertThat(createdShortcuts).hasValue(1);
  }

  @DisplayName("Do not create the desktop shortcut when the user declines")
  @Test
  void declineShortcut() throws IOException, InterruptedException {
    // GIVEN
    server.publish(FIRST_VERSION, "first");
    display.answerWith(false);

    // WHEN
    run();

    // THEN
    Assertions.assertThat(createdShortcuts).hasValue(0);
    Assertions.assertThat(processStarter.commands()).hasSize(1);
  }

  @DisplayName("Update to a newer release and keep the previous one")
  @Test
  void updateToNewerRelease() throws IOException, InterruptedException {
    // GIVEN
    server.publish(FIRST_VERSION, "first");
    run();
    server.publish(SECOND_VERSION, "second");

    // WHEN
    run();

    // THEN
    Assertions.assertThat(jar(SECOND_VERSION)).hasContent("second");
    Assertions.assertThat(jar(FIRST_VERSION)).exists();
    Assertions.assertThat(processStarter.commands().getLast())
        .contains(jar(SECOND_VERSION).toString());
  }

  @DisplayName("Remove releases older than the previous one")
  @Test
  void removeOldReleases() throws IOException, InterruptedException {
    // GIVEN
    server.publish(FIRST_VERSION, "first");
    run();
    server.publish(SECOND_VERSION, "second");
    run();
    server.publish("0.1.0", "third");

    // WHEN
    run();

    // THEN
    Assertions.assertThat(jar(FIRST_VERSION)).doesNotExist();
    Assertions.assertThat(jar(SECOND_VERSION)).exists();
    Assertions.assertThat(jar("0.1.0")).exists();
  }

  @DisplayName("Keep the installed release when the download does not match its checksum")
  @Test
  void keepInstalledReleaseWhenChecksumDiffers() throws IOException, InterruptedException {
    // GIVEN
    server.publish(FIRST_VERSION, "first");
    run();
    server.publish(SECOND_VERSION, "corrupted", 25, 1, ReleaseServer.checksum("second"));

    // WHEN
    run();

    // THEN
    Assertions.assertThat(jar(SECOND_VERSION)).doesNotExist();
    Assertions.assertThat(processStarter.commands().getLast())
        .contains(jar(FIRST_VERSION).toString());
    Assertions.assertThat(display.errors()).isEmpty();
  }

  @DisplayName("Start the installed release when the release server is unreachable")
  @Test
  void startInstalledReleaseWhenOffline() throws IOException, InterruptedException {
    // GIVEN
    server.publish(FIRST_VERSION, "first");
    run();
    server.close();

    // WHEN
    run();

    // THEN
    Assertions.assertThat(processStarter.commands()).hasSize(2);
    Assertions.assertThat(display.errors()).isEmpty();
    Assertions.assertThat(display.informations()).isEmpty();
  }

  @DisplayName("Ask to retry when nothing is installed and the release server is unreachable")
  @Test
  void askToRetryWhenNothingCanBeInstalled() throws IOException, InterruptedException {
    // GIVEN
    server.close();
    display.answerWith(false);

    // WHEN
    run();

    // THEN
    Assertions.assertThat(display.questions())
        .singleElement()
        .asString()
        .contains("could not be installed");
    Assertions.assertThat(processStarter.commands()).isEmpty();
  }

  @DisplayName("Start the previous release when the new one stops unexpectedly")
  @Test
  void startPreviousReleaseWhenNewOneStops() throws IOException, InterruptedException {
    // GIVEN
    server.publish(FIRST_VERSION, "first");
    run();
    server.publish(SECOND_VERSION, "second");
    processStarter.exitNextWith(1);

    // WHEN
    run();
    run();

    // THEN
    Assertions.assertThat(processStarter.commands())
        .extracting(command -> command.get(3))
        .containsExactly(
            jar(FIRST_VERSION).toString(),
            jar(SECOND_VERSION).toString(),
            jar(FIRST_VERSION).toString(),
            jar(FIRST_VERSION).toString());
  }

  @DisplayName("Report the failure when the only release stops unexpectedly")
  @Test
  void reportFailureWithoutPreviousRelease() throws IOException, InterruptedException {
    // GIVEN
    server.publish(FIRST_VERSION, "first");
    processStarter.exitNextWith(3);

    // WHEN
    run();

    // THEN
    Assertions.assertThat(display.errors()).singleElement().asString().contains("exit code 3");
  }

  @DisplayName("Inform once when the latest release requires a more recent launcher")
  @Test
  void informWhenLauncherIsTooOld() throws IOException, InterruptedException {
    // GIVEN
    server.publish(FIRST_VERSION, "first");
    run();
    server.publish(SECOND_VERSION, "second", 25, 2, ReleaseServer.checksum("second"));

    // WHEN
    run();
    run();

    // THEN
    Assertions.assertThat(display.informations())
        .singleElement()
        .asString()
        .contains("more recent launcher");
    Assertions.assertThat(jar(SECOND_VERSION)).doesNotExist();
    Assertions.assertThat(processStarter.commands()).hasSize(3);
  }

  @DisplayName("Inform when the latest release requires a more recent Java")
  @Test
  void informWhenJavaIsTooOld() throws IOException, InterruptedException {
    // GIVEN
    server.publish(FIRST_VERSION, "first");
    run();
    server.publish(SECOND_VERSION, "second", 26, 1, ReleaseServer.checksum("second"));

    // WHEN
    run();

    // THEN
    Assertions.assertThat(display.informations()).singleElement().asString().contains("Java 26");
    Assertions.assertThat(processStarter.commands().getLast())
        .contains(jar(FIRST_VERSION).toString());
  }

  @DisplayName("Pass the JVM options chosen by the user")
  @Test
  void passUserJvmOptions() throws IOException, InterruptedException {
    // GIVEN
    server.publish(FIRST_VERSION, "first");
    Files.createDirectories(directories.root());
    Files.writeString(directories.jvmOptionsFile(), "# Memory\n-Xmx4g\n\n");

    // WHEN
    run();

    // THEN
    Assertions.assertThat(processStarter.commands().getFirst())
        .containsSubsequence("--enable-native-access=ALL-UNNAMED", "-Xmx4g", "-jar");
  }

  private void run(final String... arguments) throws IOException, InterruptedException {
    final JavaRuntimeFinder javaRuntimeFinder =
        new JavaRuntimeFinder(
            OperatingSystem.LINUX, List.of(javaExecutable.getParent().getParent()));
    new LauncherRunner(
            directories,
            new ReleaseUpdater(
                directories,
                PLATFORM,
                new ReleaseRepository(server.releasesUrl()),
                javaRuntimeFinder,
                display),
            new ReleaseStarter(directories, javaRuntimeFinder, processStarter, display),
            display,
            Optional.of(createdShortcuts::incrementAndGet))
        .run(List.of(arguments));
  }

  private Path jar(final String version) {
    return directories.versions().resolve(version).resolve("wpcleaner-ng.jar");
  }
}
