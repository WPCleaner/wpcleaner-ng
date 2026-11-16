package org.wpcleaner.lib.check.algorithm.a0xx.a05x.a050;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import org.springframework.stereotype.Component;
import org.wpcleaner.api.analysis.PageAnalysis;
import org.wpcleaner.lib.check.CheckAlgorithm;
import org.wpcleaner.lib.check.CheckError;
import org.wpcleaner.lib.check.Replacement;
import org.wpcleaner.lib.check.algorithm.HtmlCharacter;
import org.wpcleaner.lib.check.algorithm.HtmlNamedEntity;

@Component
public class CheckAlgorithm050 implements CheckAlgorithm {

  private static final int ALGORITHM_NUMBER = 50;
  private static final List<HtmlCharacter> HTML_CHARACTERS =
      List.of(HtmlCharacter.SYMBOL_EM_DASH, HtmlCharacter.SYMBOL_EN_DASH);

  @Override
  public int number() {
    return ALGORITHM_NUMBER;
  }

  @Override
  public List<CheckError> analyze(final PageAnalysis analysis) {
    return HtmlNamedEntity.findWithSemicolon(analysis, HTML_CHARACTERS).stream()
        .map(
            entity ->
                new CheckError(
                    ALGORITHM_NUMBER,
                    entity.begin(),
                    entity.end(),
                    List.of(new Replacement(String.valueOf(entity.character().getValue()), true))))
        .toList();
  }
}
