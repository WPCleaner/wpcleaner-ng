package org.wpcleaner.api.repository.interwiki;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.jspecify.annotations.Nullable;

public record Interwiki(
    String prefix,
    boolean local,
    String url,
    @Nullable String language,
    @Nullable String deprecated) {

  public static Optional<Interwiki> findInterwiki(
      final List<Interwiki> interwikis, final String prefix) {
    return interwikis.stream()
        .filter(interwiki -> Objects.equals(interwiki.prefix(), prefix))
        .findFirst();
  }
}
