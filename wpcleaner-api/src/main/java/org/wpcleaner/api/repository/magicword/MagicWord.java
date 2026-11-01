package org.wpcleaner.api.repository.magicword;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import java.util.Locale;

public record MagicWord(
    String name,
    List<String> aliases,
    boolean caseSensitive,
    boolean functionHook,
    boolean variable) {

  private static final String FUNCTION_SUFFIX = ":";

  public MagicWord {
    aliases = List.copyOf(aliases);
  }

  public boolean isPossibleAlias(final String value) {
    return aliases.stream()
        .map(
            alias ->
                alias.endsWith(FUNCTION_SUFFIX) ? alias.substring(0, alias.length() - 1) : alias)
        .anyMatch(
            alias ->
                caseSensitive
                    ? alias.equals(value)
                    : alias.toLowerCase(Locale.ROOT).equals(value.toLowerCase(Locale.ROOT)));
  }
}
