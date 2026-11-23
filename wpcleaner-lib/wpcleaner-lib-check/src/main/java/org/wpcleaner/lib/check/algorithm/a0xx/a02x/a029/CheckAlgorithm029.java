package org.wpcleaner.lib.check.algorithm.a0xx.a02x.a029;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.stereotype.Component;
import org.wpcleaner.api.analysis.PageAnalysis;
import org.wpcleaner.api.analysis.tag.CompleteTagElement;
import org.wpcleaner.api.analysis.tag.TagElement;
import org.wpcleaner.api.analysis.tag.TagKind;
import org.wpcleaner.api.analysis.tag.WikiTagType;
import org.wpcleaner.lib.check.CheckAlgorithm;
import org.wpcleaner.lib.check.CheckError;
import org.wpcleaner.lib.check.Replacement;
import org.wpcleaner.lib.check.algorithm.UnclosedTags;

@Component
public class CheckAlgorithm029 implements CheckAlgorithm {

  private static final int ALGORITHM_NUMBER = 29;
  private static final UnclosedTags UNCLOSED_TAGS =
      new UnclosedTags(Set.of(WikiTagType.GALLERY), true);

  @Override
  public int number() {
    return ALGORITHM_NUMBER;
  }

  @Override
  public List<CheckError> analyze(final PageAnalysis analysis) {
    final List<TagElement> galleryTags =
        analysis.getTags().stream()
            .filter(tag -> tag.type().filter(WikiTagType.GALLERY::equals).isPresent())
            .toList();
    final Set<TagElement> completeStartTags =
        analysis.getCompleteTags().stream()
            .map(CompleteTagElement::startTag)
            .collect(Collectors.toSet());
    return UNCLOSED_TAGS.analyze(ALGORITHM_NUMBER, analysis).stream()
        .map(error -> extendToAdjacentStartTag(error, analysis, galleryTags, completeStartTags))
        .toList();
  }

  private static CheckError extendToAdjacentStartTag(
      final CheckError error,
      final PageAnalysis analysis,
      final List<TagElement> galleryTags,
      final Set<TagElement> completeStartTags) {
    return findAdjacentStartTags(analysis.getText(), error, galleryTags, completeStartTags)
        .map(tags -> mergeAdjacentStartTags(analysis.getText(), tags))
        .orElse(error);
  }

  private static Optional<AdjacentStartTags> findAdjacentStartTags(
      final String text,
      final CheckError error,
      final List<TagElement> galleryTags,
      final Set<TagElement> completeStartTags) {
    for (int index = 1; index < galleryTags.size(); index++) {
      final TagElement unclosedTag = galleryTags.get(index);
      final TagElement previousTag = galleryTags.get(index - 1);
      if (unclosedTag.begin() == error.begin()
          && unclosedTag.kind() == TagKind.START
          && completeStartTags.contains(previousTag)
          && isOnlySpacesAndNewLines(text.substring(previousTag.end(), unclosedTag.begin()))
          && (previousTag.attributes().isEmpty() || unclosedTag.attributes().isEmpty())) {
        return Optional.of(new AdjacentStartTags(previousTag, unclosedTag));
      }
    }
    return Optional.empty();
  }

  private static boolean isOnlySpacesAndNewLines(final String text) {
    return text.chars().allMatch(character -> character == ' ' || character == '\n');
  }

  private static CheckError mergeAdjacentStartTags(
      final String text, final AdjacentStartTags tags) {
    final String previousTag = text.substring(tags.previous().begin(), tags.previous().end());
    final String unclosedTag = text.substring(tags.unclosed().begin(), tags.unclosed().end());
    final Stream<String> keptTags =
        tags.unclosed().attributes().isEmpty()
            ? Stream.of(previousTag, unclosedTag)
            : Stream.of(unclosedTag, previousTag);
    return new CheckError(
        ALGORITHM_NUMBER,
        tags.previous().begin(),
        tags.unclosed().end(),
        keptTags.distinct().map(keptTag -> new Replacement(keptTag, false)).toList());
  }

  private record AdjacentStartTags(TagElement previous, TagElement unclosed) {}
}
