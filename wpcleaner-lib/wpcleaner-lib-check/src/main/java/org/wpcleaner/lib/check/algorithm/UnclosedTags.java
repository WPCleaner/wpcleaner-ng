package org.wpcleaner.lib.check.algorithm;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.wpcleaner.api.analysis.PageAnalysis;
import org.wpcleaner.api.analysis.tag.TagBuilder;
import org.wpcleaner.api.analysis.tag.TagElement;
import org.wpcleaner.api.analysis.tag.TagKind;
import org.wpcleaner.api.analysis.tag.TagType;
import org.wpcleaner.lib.check.CheckError;
import org.wpcleaner.lib.check.Replacement;

public record UnclosedTags(Set<TagType> types, boolean reportSelfClosing) {

  public UnclosedTags {
    types = Set.copyOf(types);
  }

  public List<CheckError> analyze(final int algorithm, final PageAnalysis analysis) {
    final Set<TagElement> completeTags =
        analysis.getCompleteTags().stream()
            .flatMap(tag -> Stream.of(tag.startTag(), tag.endTag()))
            .collect(Collectors.toSet());
    return analysis.getTags().stream()
        .filter(tag -> tag.type().filter(types::contains).isPresent())
        .flatMap(tag -> analyzeTag(algorithm, analysis, tag, completeTags).stream())
        .toList();
  }

  private Optional<CheckError> analyzeTag(
      final int algorithm,
      final PageAnalysis analysis,
      final TagElement tag,
      final Set<TagElement> completeTags) {
    if (tag.kind() == TagKind.SELF_CLOSING) {
      return reportSelfClosing ? Optional.of(createRemoval(algorithm, tag)) : Optional.empty();
    }
    if (!completeTags.contains(tag)) {
      return Optional.of(createRemoval(algorithm, tag));
    }
    if (tag.kind() == TagKind.END && hasWhitespaceBeforeEnd(analysis.getText(), tag)) {
      return Optional.of(
          new CheckError(
              algorithm,
              tag.begin(),
              tag.end(),
              List.of(
                  new Replacement(
                      TagBuilder.from(tag.name()).withKind(TagKind.END).build(),
                      tag.attributes().isEmpty()))));
    }
    return Optional.empty();
  }

  private static CheckError createRemoval(final int algorithm, final TagElement tag) {
    return new CheckError(algorithm, tag.begin(), tag.end(), List.of(new Replacement("", false)));
  }

  private static boolean hasWhitespaceBeforeEnd(final String text, final TagElement tag) {
    return Character.isWhitespace(text.charAt(tag.end() - 2));
  }
}
