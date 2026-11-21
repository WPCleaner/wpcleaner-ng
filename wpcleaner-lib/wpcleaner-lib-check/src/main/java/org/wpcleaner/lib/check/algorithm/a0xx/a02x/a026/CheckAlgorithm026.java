package org.wpcleaner.lib.check.algorithm.a0xx.a02x.a026;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Component;
import org.wpcleaner.api.analysis.Element;
import org.wpcleaner.api.analysis.PageAnalysis;
import org.wpcleaner.api.analysis.tag.CompleteTagElement;
import org.wpcleaner.api.analysis.tag.HtmlTagType;
import org.wpcleaner.api.repository.namespace.CommonNamespaces;
import org.wpcleaner.lib.check.CheckAlgorithm;
import org.wpcleaner.lib.check.CheckError;
import org.wpcleaner.lib.check.Replacement;
import org.wpcleaner.lib.check.algorithm.UnwantedTags;

@Component
public class CheckAlgorithm026 implements CheckAlgorithm {

  private static final int ALGORITHM_NUMBER = 26;
  private static final UnwantedTags UNWANTED_TAGS = new UnwantedTags(Set.of(HtmlTagType.B));
  private static final String BOLD = "'''";
  private static final List<Replacement> DELETION = List.of(new Replacement("", false));

  @Override
  public int number() {
    return ALGORITHM_NUMBER;
  }

  @Override
  public List<CheckError> analyze(final PageAnalysis analysis) {
    if (analysis.getPageTitle().namespace() == CommonNamespaces.FILE.id) {
      return List.of();
    }
    final String text = analysis.getText();
    return UNWANTED_TAGS.findComplete(analysis).stream()
        .map(
            tag ->
                new CheckError(ALGORITHM_NUMBER, tag.begin(), tag.end(), replacements(text, tag)))
        .toList();
  }

  private static List<Replacement> replacements(final String text, final Element tag) {
    if (!(tag instanceof final CompleteTagElement completeTag)) {
      return DELETION;
    }
    final String contents =
        text.substring(completeTag.startTag().end(), completeTag.endTag().begin());
    if (contents.isBlank()) {
      return DELETION;
    }
    return List.of(
        new Replacement(BOLD + contents + BOLD, false), new Replacement(contents, false));
  }
}
