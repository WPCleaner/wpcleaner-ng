package org.wpcleaner.lib.check.algorithm;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.wpcleaner.api.analysis.Element;
import org.wpcleaner.api.analysis.PageAnalysis;
import org.wpcleaner.api.analysis.tag.CompleteTagElement;
import org.wpcleaner.api.analysis.tag.TagElement;
import org.wpcleaner.api.analysis.tag.TagType;

public record UnwantedTags(Set<TagType> types) {

  public UnwantedTags {
    types = Set.copyOf(types);
  }

  public List<TagElement> find(final PageAnalysis analysis) {
    if (analysis.getPageTitle().namespace() % 2 != 0) {
      return List.of();
    }
    return analysis.getTags().stream()
        .filter(tag -> tag.type().filter(types::contains).isPresent())
        .toList();
  }

  public List<Element> findComplete(final PageAnalysis analysis) {
    final Map<TagElement, CompleteTagElement> completeTags =
        analysis.getCompleteTags().stream()
            .collect(
                Collectors.toMap(
                    CompleteTagElement::startTag, Function.identity(), (first, _) -> first));
    final Set<TagElement> endTags =
        completeTags.values().stream().map(CompleteTagElement::endTag).collect(Collectors.toSet());
    return find(analysis).stream()
        .filter(tag -> !endTags.contains(tag))
        .map(tag -> Optional.<Element>ofNullable(completeTags.get(tag)).orElse(tag))
        .toList();
  }
}
