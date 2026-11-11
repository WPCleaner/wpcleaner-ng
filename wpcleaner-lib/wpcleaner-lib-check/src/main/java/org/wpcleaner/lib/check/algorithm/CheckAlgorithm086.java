package org.wpcleaner.lib.check.algorithm;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Component;
import org.wpcleaner.api.analysis.PageAnalysis;
import org.wpcleaner.api.analysis.externallink.ExternalLinkElement;
import org.wpcleaner.lib.check.CheckAlgorithm;
import org.wpcleaner.lib.check.CheckError;
import org.wpcleaner.lib.check.Replacement;

@Component
public class CheckAlgorithm086 implements CheckAlgorithm {

  private static final int ALGORITHM_NUMBER = 86;
  private static final char OPEN = '[';
  private static final char CLOSE = ']';
  private static final String PIPE = "|";
  private static final String SPACE = " ";

  @Override
  public int number() {
    return ALGORITHM_NUMBER;
  }

  @Override
  public List<CheckError> analyze(final PageAnalysis analysis) {
    final String text = analysis.getText();
    return analysis.getExternalLinks().stream()
        .filter(ExternalLinkElement::bracketed)
        .filter(link -> link.begin() > 0 && text.charAt(link.begin() - 1) == OPEN)
        .map(link -> createError(text, link))
        .toList();
  }

  private static CheckError createError(final String text, final ExternalLinkElement link) {
    final int end =
        link.end() < text.length() && text.charAt(link.end()) == CLOSE
            ? link.end() + 1
            : link.end();
    final String externalLink = text.substring(link.begin(), link.end());
    final Set<String> replacements = new LinkedHashSet<>();
    if (link.target().contains(PIPE)) {
      replacements.add(externalLink.replaceFirst("\\|", SPACE));
    }
    replacements.add(externalLink);
    return new CheckError(
        ALGORITHM_NUMBER,
        link.begin() - 1,
        end,
        replacements.stream().map(replacement -> new Replacement(replacement, false)).toList());
  }
}
