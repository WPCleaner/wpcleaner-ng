package org.wpcleaner.lib.check.algorithm.a0xx.a02x.a027;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import org.wpcleaner.api.analysis.PageAnalysis;
import org.wpcleaner.lib.check.algorithm.TextExclusions;

record NumericEntity(int begin, int end, int codePoint) {

  private static final Pattern ENTITY =
      Pattern.compile("&#(?:0*([0-9]{1,8})|[xX]0*([0-9a-fA-F]{1,7}));");
  private static final int DECIMAL = 10;
  private static final int HEXADECIMAL = 16;

  static List<NumericEntity> find(final PageAnalysis analysis) {
    final TextExclusions exclusions = TextExclusions.of(analysis);
    return ENTITY
        .matcher(analysis.getText())
        .results()
        .filter(entity -> !exclusions.contains(entity.start()))
        .map(NumericEntity::of)
        .toList();
  }

  private static NumericEntity of(final MatchResult entity) {
    final int codePoint =
        entity.group(1) != null
            ? Integer.parseInt(entity.group(1), DECIMAL)
            : Integer.parseInt(entity.group(2), HEXADECIMAL);
    return new NumericEntity(entity.start(), entity.end(), codePoint);
  }
}
