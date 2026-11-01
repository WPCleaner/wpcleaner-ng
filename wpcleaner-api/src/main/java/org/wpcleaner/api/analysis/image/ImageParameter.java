package org.wpcleaner.api.analysis.image;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import java.util.Optional;
import org.jspecify.annotations.Nullable;

public record ImageParameter(
    int begin, int end, String value, @Nullable String magicWord, @Nullable String argument) {

  public static Optional<ImageParameter> findCaption(final List<ImageParameter> parameters) {
    return parameters.reversed().stream()
        .filter(parameter -> parameter.magicWord() == null)
        .findFirst();
  }

  public static Optional<ImageParameter> findParameter(
      final List<ImageParameter> parameters, final String magicWord) {
    return parameters.reversed().stream()
        .filter(parameter -> magicWord.equals(parameter.magicWord()))
        .findFirst();
  }
}
