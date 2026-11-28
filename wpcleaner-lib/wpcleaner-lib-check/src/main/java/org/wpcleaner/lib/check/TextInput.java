package org.wpcleaner.lib.check;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

public record TextInput(
    String label, String question, String prefix, String suffix, String unauthorizedCharacters)
    implements Suggestion {

  public String replacement(final String value) {
    return prefix + value + suffix;
  }

  public boolean isAuthorized(final String value) {
    return !value.isBlank()
        && value.chars().noneMatch(character -> unauthorizedCharacters.indexOf(character) >= 0);
  }
}
