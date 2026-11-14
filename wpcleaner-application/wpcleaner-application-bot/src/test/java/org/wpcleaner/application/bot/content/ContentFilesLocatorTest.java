package org.wpcleaner.application.bot.content;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.wpcleaner.api.wiki.definition.WikiDefinition;
import org.wpcleaner.api.wiki.definition.WikipediaDefinitions;
import org.wpcleaner.application.bot.BotSpringBootTest;

@BotSpringBootTest
class ContentFilesLocatorTest {

  private static final WikiDefinition WIKI = WikipediaDefinitions.FR;
  private static final String CHECKSUM = "0".repeat(64);

  @Autowired private ContentFilesLocator locator;

  @TempDir private Path folder;

  @DisplayName("should use the most recent date with a list of checksums")
  @Test
  void locateMostRecentFiles() throws IOException {
    // GIVEN
    givenFiles("2026-10-01", "frwiki-2026-10-01-p1p2.xml.bz2");
    final Path first = givenFiles("2026-11-01", "frwiki-2026-11-01-p1p2.xml.bz2");
    final Path second = first.resolveSibling("frwiki-2026-11-01-p3p4.xml.bz2");
    Files.createFile(second);
    Files.writeString(
        first.resolveSibling(ContentFilesLocator.CHECKSUMS_FILE),
        "%s  %s\n%s *%s\n".formatted(CHECKSUM, first.getFileName(), CHECKSUM, second.getFileName()),
        StandardCharsets.UTF_8);
    Files.createDirectories(folder.resolve("frwiki").resolve("2026-12-01"));
    Files.createDirectories(folder.resolve("frwiki").resolve("latest"));

    // WHEN
    final ContentFiles files = locator.locate(folder, WIKI);

    // THEN
    Assertions.assertThat(files.name()).isEqualTo("frwiki-2026-11-01");
    Assertions.assertThat(files.files()).containsExactly(first, second);
  }

  @DisplayName("should fail when no content files are available for the wiki")
  @Test
  void locateMissingFiles() throws IOException {
    // GIVEN
    Files.createDirectories(folder.resolve("frwiki").resolve("2026-11-01"));

    // WHEN / THEN
    Assertions.assertThatThrownBy(() -> locator.locate(folder, WIKI))
        .isInstanceOf(IOException.class)
        .hasMessageContaining("SHA256SUMS");
    Assertions.assertThatThrownBy(() -> locator.locate(folder, WikipediaDefinitions.EN))
        .isInstanceOf(IOException.class);
  }

  @DisplayName("should fail when a listed file is missing")
  @Test
  void locateMissingListedFile() throws IOException {
    // GIVEN
    final Path file = givenFiles("2026-11-01", "frwiki-2026-11-01-p1p2.xml.bz2");
    Files.delete(file);

    // WHEN / THEN
    Assertions.assertThatThrownBy(() -> locator.locate(folder, WIKI))
        .isInstanceOf(IOException.class)
        .hasMessageContaining(file.getFileName().toString());
  }

  private Path givenFiles(final String date, final String name) throws IOException {
    final Path dateFolder = Files.createDirectories(folder.resolve("frwiki").resolve(date));
    final Path file = Files.createFile(dateFolder.resolve(name));
    Files.writeString(
        dateFolder.resolve(ContentFilesLocator.CHECKSUMS_FILE),
        "%s  %s\n".formatted(CHECKSUM, name),
        StandardCharsets.UTF_8);
    return file;
  }
}
