package org.wpcleaner.lib.check.algorithm.a0xx.a01x.a012;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Component;
import org.wpcleaner.api.analysis.PageAnalysis;
import org.wpcleaner.api.analysis.tag.HtmlTagType;
import org.wpcleaner.api.analysis.tag.TagElement;
import org.wpcleaner.lib.check.CheckAlgorithm;
import org.wpcleaner.lib.check.CheckError;
import org.wpcleaner.lib.check.algorithm.UnwantedTags;

@Component
public class CheckAlgorithm012 implements CheckAlgorithm {

  private static final int ALGORITHM_NUMBER = 12;
  private static final String VALUE = "value";
  private static final UnwantedTags UNWANTED_TAGS =
      new UnwantedTags(Set.of(HtmlTagType.LI, HtmlTagType.OL, HtmlTagType.UL));

  @Override
  public int number() {
    return ALGORITHM_NUMBER;
  }

  @Override
  public List<CheckError> analyze(final PageAnalysis analysis) {
    return UNWANTED_TAGS.find(analysis).stream()
        .filter(tag -> !isListItemWithValue(tag))
        .map(tag -> new CheckError(ALGORITHM_NUMBER, tag.begin(), tag.end(), List.of()))
        .toList();
  }

  private static boolean isListItemWithValue(final TagElement tag) {
    return HtmlTagType.LI.getNormalizedName().equals(tag.normalizedName())
        && tag.attributes().stream()
            .anyMatch(attribute -> VALUE.equalsIgnoreCase(attribute.name()));
  }
}
