package org.wpcleaner.lib.check.algorithm.a0xx.a03x.a031;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Component;
import org.wpcleaner.api.analysis.PageAnalysis;
import org.wpcleaner.api.analysis.tag.HtmlTagType;
import org.wpcleaner.lib.check.CheckAlgorithm;
import org.wpcleaner.lib.check.CheckError;
import org.wpcleaner.lib.check.algorithm.UnwantedTags;

@Component
public class CheckAlgorithm031 implements CheckAlgorithm {

  private static final int ALGORITHM_NUMBER = 31;
  private static final UnwantedTags UNWANTED_TAGS =
      new UnwantedTags(Set.of(HtmlTagType.TABLE, HtmlTagType.TD, HtmlTagType.TH, HtmlTagType.TR));

  @Override
  public int number() {
    return ALGORITHM_NUMBER;
  }

  @Override
  public List<CheckError> analyze(final PageAnalysis analysis) {
    return UNWANTED_TAGS.find(analysis).stream()
        .map(tag -> new CheckError(ALGORITHM_NUMBER, tag.begin(), tag.end(), List.of()))
        .toList();
  }
}
