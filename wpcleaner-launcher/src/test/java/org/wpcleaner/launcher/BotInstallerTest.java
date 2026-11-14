package org.wpcleaner.launcher;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.wpcleaner.launcher.install.BotInstaller;
import org.wpcleaner.launcher.platform.LauncherDirectories;
import org.wpcleaner.launcher.platform.OperatingSystem;
import org.wpcleaner.launcher.release.ReleaseRepository;
import org.wpcleaner.launcher.runtime.JavaRuntimeFinder;
import org.wpcleaner.launcher.start.BotStartScript;

class BotInstallerTest {

  private static final String FIRST_VERSION = "0.0.6";
  private static final String SECOND_VERSION = "0.0.7";

  @TempDir private Path temporaryFolder;

  private ReleaseServer server;
  private LauncherDirectories directories;
  private Path javaExecutable;
  private RecordingDisplay display;

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
  }

  @AfterEach
  void stopServer() {
    server.close();
  }

  @DisplayName("Install the bot with a start script")
  @Test
  void installBot() throws IOException {
    // GIVEN
    server.publishWithBot(FIRST_VERSION, "application", "bot");

    // WHEN
    final boolean installed = install();

    // THEN
    Assertions.assertThat(installed).isTrue();
    Assertions.assertThat(botJar(FIRST_VERSION)).hasContent("bot");
    Assertions.assertThat(directories.versions()).doesNotExist();
    final Path script = directories.bot().resolve("wpcleaner-ng-bot.sh");
    Assertions.assertThat(script)
        .isExecutable()
        .hasContent(
            "#!/bin/sh\nexec '%s' '--enable-native-access=ALL-UNNAMED' '-jar' '%s' \"$@\""
                .formatted(javaExecutable, botJar(FIRST_VERSION)));
    Assertions.assertThat(display.informations())
        .singleElement()
        .asString()
        .contains(script.toString());
    Assertions.assertThat(display.errors()).isEmpty();
    Assertions.assertThat(display.isProgressOpen()).isFalse();
  }

  @DisplayName("Update the bot and remove the previous version")
  @Test
  void updateBot() throws IOException {
    // GIVEN
    server.publishWithBot(FIRST_VERSION, "application", "first bot");
    install();
    server.publishWithBot(SECOND_VERSION, "application", "second bot");

    // WHEN
    final boolean installed = install();

    // THEN
    Assertions.assertThat(installed).isTrue();
    Assertions.assertThat(botJar(SECOND_VERSION)).hasContent("second bot");
    Assertions.assertThat(botJar(FIRST_VERSION)).doesNotExist();
    Assertions.assertThat(directories.bot().resolve("wpcleaner-ng-bot.sh"))
        .content()
        .contains(botJar(SECOND_VERSION).toString());
  }

  @DisplayName("Keep the installed bot when it is already the latest version")
  @Test
  void keepInstalledBot() throws IOException {
    // GIVEN
    server.publishWithBot(FIRST_VERSION, "application", "bot");
    install();
    Files.writeString(botJar(FIRST_VERSION), "installed bot");

    // WHEN
    final boolean installed = install();

    // THEN
    Assertions.assertThat(installed).isTrue();
    Assertions.assertThat(botJar(FIRST_VERSION)).hasContent("installed bot");
  }

  @DisplayName("Report an error when the release has no bot")
  @Test
  void installWithoutBot() throws IOException {
    // GIVEN
    server.publish(FIRST_VERSION, "application");

    // WHEN
    final boolean installed = install();

    // THEN
    Assertions.assertThat(installed).isFalse();
    Assertions.assertThat(display.errors()).hasSize(1);
    Assertions.assertThat(directories.botVersions()).doesNotExist();
  }

  private boolean install() throws IOException {
    return new BotInstaller(
            directories,
            new ReleaseRepository(server.releasesUrl()),
            new JavaRuntimeFinder(
                OperatingSystem.LINUX, List.of(javaExecutable.getParent().getParent())),
            display,
            new BotStartScript(directories, OperatingSystem.LINUX))
        .install();
  }

  private Path botJar(final String version) {
    return directories.botVersions().resolve(version).resolve("wpcleaner-ng-bot.jar");
  }
}
