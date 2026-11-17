package org.wpcleaner.lib.check.algorithm.a0xx.a02x.a024;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Component;
import org.wpcleaner.api.analysis.PageAnalysis;
import org.wpcleaner.api.analysis.tag.WikiTagType;
import org.wpcleaner.lib.check.CheckAlgorithm;
import org.wpcleaner.lib.check.CheckError;
import org.wpcleaner.lib.check.algorithm.UnclosedTags;

@Component
public class CheckAlgorithm024 implements CheckAlgorithm {

  private static final int ALGORITHM_NUMBER = 24;
  private static final UnclosedTags UNCLOSED_TAGS = new UnclosedTags(Set.of(WikiTagType.PRE), true);

  @Override
  public int number() {
    return ALGORITHM_NUMBER;
  }

  @Override
  public List<CheckError> analyze(final PageAnalysis analysis) {
    return UNCLOSED_TAGS.analyze(ALGORITHM_NUMBER, analysis);
  }
}
