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
import org.wpcleaner.api.analysis.comment.CommentElement;
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
        .map(link -> new BracketedLink(link, skipCommentsBefore(analysis, link.begin())))
        .filter(link -> link.begin() > 0 && text.charAt(link.begin() - 1) == OPEN)
        .map(link -> createError(text, link))
        .toList();
  }

  private static int skipCommentsBefore(final PageAnalysis analysis, final int index) {
    int begin = index;
    for (final CommentElement comment : analysis.getComments().reversed()) {
      if (comment.end() == begin) {
        begin = comment.begin();
      }
    }
    return begin;
  }

  private static CheckError createError(final String text, final BracketedLink bracketedLink) {
    final ExternalLinkElement link = bracketedLink.link();
    final int end =
        link.end() < text.length() && text.charAt(link.end()) == CLOSE
            ? link.end() + 1
            : link.end();
    final String comments = text.substring(bracketedLink.begin(), link.begin());
    final String externalLink = text.substring(link.begin(), link.end());
    final Set<String> replacements = new LinkedHashSet<>();
    if (link.target().contains(PIPE)) {
      replacements.add(comments + externalLink.replaceFirst("\\|", SPACE));
    }
    replacements.add(comments + externalLink);
    return new CheckError(
        ALGORITHM_NUMBER,
        bracketedLink.begin() - 1,
        end,
        replacements.stream().map(replacement -> new Replacement(replacement, false)).toList());
  }

  private record BracketedLink(ExternalLinkElement link, int begin) {}
}
