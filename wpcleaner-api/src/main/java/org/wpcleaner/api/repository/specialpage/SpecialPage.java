package org.wpcleaner.api.repository.specialpage;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

public record SpecialPage(String realName, List<String> aliases) {

  public SpecialPage {
    aliases = List.copyOf(aliases);
  }

  public boolean hasName(final String name) {
    final String normalizedName = normalize(name);
    return Stream.concat(Stream.of(realName), aliases.stream())
        .anyMatch(alias -> normalize(alias).equals(normalizedName));
  }

  private static String normalize(final String name) {
    return name.trim().replace(' ', '_').toLowerCase(Locale.ROOT);
  }
}
