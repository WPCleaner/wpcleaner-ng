package org.wpcleaner.application.bot.script;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Set;
import org.jspecify.annotations.Nullable;

public record ReportedAlgorithm(int algorithm, @Nullable Set<Integer> namespaces) {
  @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
  public static ReportedAlgorithm of(final int algorithm) {
    return new ReportedAlgorithm(algorithm, null);
  }

  @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
  public static ReportedAlgorithm of(
      @JsonProperty(value = "algorithm", required = true) final int algorithm,
      @JsonProperty("namespaces") @Nullable final Set<Integer> namespaces) {
    return namespaces == null
        ? of(algorithm)
        : new ReportedAlgorithm(algorithm, Set.copyOf(namespaces));
  }
}
