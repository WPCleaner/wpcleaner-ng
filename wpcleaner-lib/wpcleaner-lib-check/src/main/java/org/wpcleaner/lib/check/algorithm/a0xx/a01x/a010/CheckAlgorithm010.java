package org.wpcleaner.lib.check.algorithm.a0xx.a01x.a010;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;
import org.springframework.stereotype.Component;
import org.wpcleaner.api.analysis.PageAnalysis;
import org.wpcleaner.api.analysis.externallink.ExternalLinkElement;
import org.wpcleaner.lib.check.CheckAlgorithm;
import org.wpcleaner.lib.check.CheckError;
import org.wpcleaner.lib.check.Replacement;
import org.wpcleaner.lib.check.algorithm.TextExclusions;

@Component
public class CheckAlgorithm010 implements CheckAlgorithm {

  private static final int ALGORITHM_NUMBER = 10;

  @Override
  public int number() {
    return ALGORITHM_NUMBER;
  }

  @Override
  public List<CheckError> analyze(final PageAnalysis analysis) {
    final TextExclusions exclusions = TextExclusions.of(analysis);
    return Stream.of(
            new UnclosedLinks(analysis, exclusions).analyze(ALGORITHM_NUMBER),
            new UnpairedBrackets(analysis, SingleBrackets.of(analysis, exclusions))
                .analyze(ALGORITHM_NUMBER),
            analyzeExternalLinks(analysis))
        .flatMap(List::stream)
        .sorted(Comparator.comparingInt(CheckError::begin))
        .toList();
  }

  private static List<CheckError> analyzeExternalLinks(final PageAnalysis analysis) {
    final String text = analysis.getText();
    return analysis.getExternalLinks().stream()
        .filter(ExternalLinkElement::bracketed)
        .filter(link -> isAt(text, link.begin() - 1, SingleBrackets.OPEN))
        .filter(link -> !isAt(text, link.end(), SingleBrackets.CLOSE))
        .map(
            link ->
                new CheckError(
                    ALGORITHM_NUMBER,
                    SingleBrackets.skipBackward(text, link.begin(), SingleBrackets.OPEN),
                    link.end(),
                    List.of(new Replacement(text.substring(link.begin(), link.end()), false))))
        .toList();
  }

  private static boolean isAt(final String text, final int index, final char character) {
    return index >= 0 && index < text.length() && text.charAt(index) == character;
  }
}
