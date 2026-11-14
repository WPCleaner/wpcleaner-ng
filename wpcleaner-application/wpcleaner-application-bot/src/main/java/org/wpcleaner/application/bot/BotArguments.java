package org.wpcleaner.application.bot;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.nio.file.Path;
import java.util.List;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.DefaultApplicationArguments;

public record BotArguments(String wiki, List<Path> scripts, Path contentFolder, Path outputFolder) {

  public static final String WIKI_OPTION = "wiki";
  public static final String SCRIPT_OPTION = "script";
  public static final String CONTENT_FOLDER_OPTION = "content-folder";
  public static final String OUTPUT_FOLDER_OPTION = "output-folder";
  public static final Path DEFAULT_CONTENT_FOLDER = Path.of("mediawiki_content_current");
  public static final Path DEFAULT_OUTPUT_FOLDER = Path.of("wpcleaner-bot-output");
  public static final String USAGE =
      """
      Usage: --%s=<wiki code> --%s=<script file> [--%s=<script file>...]
             [--%s=<folder, default %s>] [--%s=<folder, default %s>]"""
          .formatted(
              WIKI_OPTION,
              SCRIPT_OPTION,
              SCRIPT_OPTION,
              CONTENT_FOLDER_OPTION,
              DEFAULT_CONTENT_FOLDER,
              OUTPUT_FOLDER_OPTION,
              DEFAULT_OUTPUT_FOLDER);

  public BotArguments {
    scripts = List.copyOf(scripts);
  }

  public static BotArguments parse(final String... args) {
    final ApplicationArguments arguments = new DefaultApplicationArguments(args);
    return new BotArguments(
        getSingleValue(arguments, WIKI_OPTION),
        getValues(arguments, SCRIPT_OPTION).stream().map(Path::of).toList(),
        getOptionalPath(arguments, CONTENT_FOLDER_OPTION, DEFAULT_CONTENT_FOLDER),
        getOptionalPath(arguments, OUTPUT_FOLDER_OPTION, DEFAULT_OUTPUT_FOLDER));
  }

  private static String getSingleValue(final ApplicationArguments arguments, final String name) {
    final List<String> values = getValues(arguments, name);
    if (values.size() > 1) {
      throw new IllegalArgumentException("Option --%s must be given only once".formatted(name));
    }
    return values.getFirst();
  }

  private static List<String> getValues(final ApplicationArguments arguments, final String name) {
    final List<String> values = arguments.getOptionValues(name);
    if (values == null || values.isEmpty() || values.stream().anyMatch(String::isBlank)) {
      throw new IllegalArgumentException("Option --%s=<value> is required".formatted(name));
    }
    return values;
  }

  private static Path getOptionalPath(
      final ApplicationArguments arguments, final String name, final Path defaultPath) {
    if (!arguments.containsOption(name)) {
      return defaultPath;
    }
    return Path.of(getSingleValue(arguments, name));
  }
}
