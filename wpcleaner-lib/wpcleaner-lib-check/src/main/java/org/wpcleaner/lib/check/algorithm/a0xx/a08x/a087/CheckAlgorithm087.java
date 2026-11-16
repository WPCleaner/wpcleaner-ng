package org.wpcleaner.lib.check.algorithm.a0xx.a08x.a087;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import java.util.stream.Stream;
import org.springframework.stereotype.Component;
import org.wpcleaner.api.analysis.PageAnalysis;
import org.wpcleaner.lib.check.CheckAlgorithm;
import org.wpcleaner.lib.check.CheckError;
import org.wpcleaner.lib.check.Replacement;
import org.wpcleaner.lib.check.algorithm.HtmlCharacter;
import org.wpcleaner.lib.check.algorithm.HtmlNamedEntity;

@Component
public class CheckAlgorithm087 implements CheckAlgorithm {

  private static final int ALGORITHM_NUMBER = 87;
  private static final List<HtmlCharacter> HTML_CHARACTERS = List.of(HtmlCharacter.values());
  private static final String ESCAPED_AMPERSAND = "&amp;";
  private static final char SPACE = ' ';
  private static final char SEMICOLON = ';';

  @Override
  public int number() {
    return ALGORITHM_NUMBER;
  }

  @Override
  public List<CheckError> analyze(final PageAnalysis analysis) {
    final String text = analysis.getText();
    return HtmlNamedEntity.findWithoutSemicolon(analysis, HTML_CHARACTERS).stream()
        .map(entity -> createError(text, entity))
        .toList();
  }

  private static CheckError createError(final String text, final HtmlNamedEntity entity) {
    final int end = findEndWithSeparatedSemicolon(text, entity.end());
    final String value = String.valueOf(entity.character().getValue());
    final String separatedSemicolon = text.substring(entity.end(), end);
    return new CheckError(
        ALGORITHM_NUMBER,
        entity.begin(),
        end,
        Stream.of(
                value,
                value + separatedSemicolon,
                ESCAPED_AMPERSAND + text.substring(entity.begin() + 1, end))
            .distinct()
            .map(replacement -> new Replacement(replacement, false))
            .toList());
  }

  private static int findEndWithSeparatedSemicolon(final String text, final int nameEnd) {
    int index = nameEnd;
    while (index < text.length() && text.charAt(index) == SPACE) {
      index++;
    }
    return index < text.length() && text.charAt(index) == SEMICOLON ? index + 1 : nameEnd;
  }
}
