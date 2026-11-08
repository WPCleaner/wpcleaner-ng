package org.wpcleaner.api.repository.configuration;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public final class ConfigurationParser {

  private static final char EQUAL = '=';
  private static final String END = "END";
  private static final String VALUE_END = " " + END;
  private static final String LINE_END = "\n";
  private static final String SEPARATOR = "|";
  private static final String ESCAPED_SEPARATOR = "\\|";
  private static final Pattern LINE_SEPARATOR = Pattern.compile("(?<!\\\\)\\|");

  private ConfigurationParser() {}

  public static Map<String, String> parse(final String text) {
    final Map<String, String> properties = new LinkedHashMap<>();
    final Iterator<String> lines = text.lines().iterator();
    while (lines.hasNext()) {
      final String line = lines.next();
      final int equalIndex = line.indexOf(EQUAL);
      final String name = equalIndex < 0 ? "" : line.substring(0, equalIndex).trim();
      if (isValidName(name)) {
        properties.put(name, readValue(line.substring(equalIndex + 1), lines));
      }
    }
    return Map.copyOf(properties);
  }

  private static String readValue(final String firstLine, final Iterator<String> lines) {
    if (END.equals(firstLine)) {
      return "";
    }
    final StringBuilder value = new StringBuilder(firstLine);
    int endIndex = value.indexOf(VALUE_END);
    while (endIndex < 0 && lines.hasNext()) {
      value.append(LINE_END).append(lines.next());
      endIndex = value.indexOf(VALUE_END);
    }
    return (endIndex < 0 ? value.toString() : value.substring(0, endIndex)).trim();
  }

  public static List<String> toLines(final String value) {
    return value.lines().map(String::trim).filter(line -> !line.isEmpty()).toList();
  }

  public static List<String> splitLine(final String line) {
    return Arrays.stream(LINE_SEPARATOR.split(line, -1))
        .map(element -> element.replace(ESCAPED_SEPARATOR, SEPARATOR))
        .toList();
  }

  private static boolean isValidName(final String name) {
    return !name.isEmpty()
        && name.chars()
            .allMatch(
                character ->
                    Character.isLetterOrDigit(character) || character == '_' || character == ' ');
  }
}
