package org.wpcleaner.lib.check.algorithm.a0xx.a00x.a002;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.wpcleaner.api.analysis.PageAnalysis;
import org.wpcleaner.api.analysis.tag.HtmlTagType;
import org.wpcleaner.api.analysis.tag.TagBuilder;
import org.wpcleaner.api.analysis.tag.TagElement;
import org.wpcleaner.api.analysis.tag.TagKind;
import org.wpcleaner.api.repository.namespace.CommonNamespaces;
import org.wpcleaner.lib.check.CheckError;
import org.wpcleaner.lib.check.Replacement;

record VoidTagAttributes(PageAnalysis analysis, ClearReplacements clearReplacements) {

  private static final String TAG_NAME = HtmlTagType.BR.getNormalizedName();

  List<CheckError> analyze(final int algorithm) {
    return analysis.getTags().stream()
        .filter(tag -> TAG_NAME.equals(tag.normalizedName()))
        .flatMap(tag -> analyzeTag(algorithm, tag).stream())
        .toList();
  }

  private Optional<CheckError> analyzeTag(final int algorithm, final TagElement tag) {
    final int begin = extendBackward(tag.begin());
    final int end = extendForward(tag.end());
    final boolean extra = begin < tag.begin() || end > tag.end();
    if (!extra && tag.attributes().isEmpty()) {
      return Optional.empty();
    }
    return Optional.of(new CheckError(algorithm, begin, end, createReplacements(tag)));
  }

  private List<Replacement> createReplacements(final TagElement tag) {
    final List<Replacement> replacements = new ArrayList<>();
    ClearReplacements.findValue(tag)
        .flatMap(clearReplacements::findReplacement)
        .ifPresent(
            replacement ->
                replacements.add(
                    new Replacement(
                        replacement,
                        !replacement.isEmpty()
                            && analysis.getPageTitle().namespace() == CommonNamespaces.MAIN.id)));
    final TagBuilder builder = TagBuilder.from(TAG_NAME);
    replacements.add(new Replacement(builder.withKind(TagKind.SELF_CLOSING).build(), false));
    replacements.add(new Replacement(builder.build(), false));
    return replacements;
  }

  private int extendBackward(final int index) {
    final String text = analysis.getText();
    int begin = index;
    while (begin > 0 && text.charAt(begin - 1) == TagElement.TOKEN_START) {
      begin--;
    }
    return begin;
  }

  private int extendForward(final int index) {
    final String text = analysis.getText();
    int end = index;
    while (end < text.length() && text.charAt(end) == TagElement.TOKEN_END) {
      end++;
    }
    return end;
  }
}
