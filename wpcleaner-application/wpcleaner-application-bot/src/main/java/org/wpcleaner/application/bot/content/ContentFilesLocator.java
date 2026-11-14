package org.wpcleaner.application.bot.content;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;
import org.wpcleaner.api.wiki.definition.WikiDefinition;

@Service
public class ContentFilesLocator {

  public static final String CHECKSUMS_FILE = "SHA256SUMS";

  private static final Pattern CHECKSUM_LINE = Pattern.compile("^[0-9a-fA-F]{64} [ *](.+)$");

  public ContentFiles locate(final Path contentFolder, final WikiDefinition wiki)
      throws IOException {
    final String wikiName = wiki.checkWikiCode();
    if (wikiName == null) {
      throw new IOException("No content files are available for wiki %s".formatted(wiki.code()));
    }
    final Path wikiFolder = contentFolder.toAbsolutePath().normalize().resolve(wikiName);
    if (!Files.isDirectory(wikiFolder)) {
      throw new IOException("No content files found in %s".formatted(wikiFolder));
    }
    final Path dateFolder =
        findMostRecentDateFolder(wikiFolder)
            .orElseThrow(
                () ->
                    new IOException(
                        "No folder with a %s file found in %s"
                            .formatted(CHECKSUMS_FILE, wikiFolder)));
    return new ContentFiles(
        wikiName, String.valueOf(dateFolder.getFileName()), readFiles(dateFolder));
  }

  private static Optional<Path> findMostRecentDateFolder(final Path wikiFolder) throws IOException {
    try (Stream<Path> folders = Files.list(wikiFolder)) {
      return folders
          .filter(Files::isDirectory)
          .filter(folder -> Files.isRegularFile(folder.resolve(CHECKSUMS_FILE)))
          .filter(folder -> parseDate(folder).isPresent())
          .max(Comparator.comparing(folder -> parseDate(folder).orElseThrow()));
    }
  }

  private static Optional<LocalDate> parseDate(final Path folder) {
    try {
      return Optional.of(LocalDate.parse(String.valueOf(folder.getFileName())));
    } catch (final DateTimeParseException _) {
      return Optional.empty();
    }
  }

  private static List<Path> readFiles(final Path dateFolder) throws IOException {
    final List<Path> files =
        Files.readAllLines(dateFolder.resolve(CHECKSUMS_FILE), StandardCharsets.UTF_8).stream()
            .map(String::strip)
            .filter(line -> !line.isEmpty())
            .map(line -> parseChecksumLine(dateFolder, line))
            .toList();
    if (files.isEmpty()) {
      throw new IOException("No file listed in %s".formatted(dateFolder.resolve(CHECKSUMS_FILE)));
    }
    for (final Path file : files) {
      if (!Files.isRegularFile(file)) {
        throw new IOException("Missing content file %s".formatted(file));
      }
    }
    return files;
  }

  private static Path parseChecksumLine(final Path dateFolder, final String line) {
    final Matcher matcher = CHECKSUM_LINE.matcher(line);
    if (!matcher.matches()) {
      throw new IllegalArgumentException("Invalid line in %s: %s".formatted(CHECKSUMS_FILE, line));
    }
    final Path file = dateFolder.resolve(matcher.group(1)).normalize();
    if (!dateFolder.equals(file.getParent())) {
      throw new IllegalArgumentException(
          "Invalid file name in %s: %s".formatted(CHECKSUMS_FILE, line));
    }
    return file;
  }
}
