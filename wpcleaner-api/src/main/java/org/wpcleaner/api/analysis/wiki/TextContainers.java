package org.wpcleaner.api.analysis.wiki;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.wpcleaner.api.analysis.preprocessor.PreprocessorContainer;
import org.wpcleaner.api.analysis.tag.CompleteTagElement;
import org.wpcleaner.api.analysis.template.TemplateParameter;

final class TextContainers {

  private final Area wholeText;
  private final NavigableMap<Integer, Area> innermostContainers;

  TextContainers(final String text, final PreprocessorContainer preprocessor) {
    this.wholeText = new Area(0, text.length());
    final List<Area> containers =
        Stream.of(
                preprocessor.getTemplates().stream()
                    .flatMap(template -> template.parameters().stream())
                    .map(TextContainers::toArea),
                preprocessor.getFunctions().stream()
                    .flatMap(function -> function.parameters().stream())
                    .map(TextContainers::toArea),
                preprocessor.getCompleteTags().stream().map(TextContainers::toContentArea))
            .flatMap(areas -> areas)
            .toList();
    this.innermostContainers = computeInnermostContainers(containers);
  }

  Area findContainer(final int index) {
    final Map.Entry<Integer, Area> innermostContainer = innermostContainers.floorEntry(index);
    return innermostContainer == null ? wholeText : innermostContainer.getValue();
  }

  boolean isWholeText(final Area container) {
    return wholeText.equals(container);
  }

  private NavigableMap<Integer, Area> computeInnermostContainers(final List<Area> containers) {
    final List<Integer> containersByBegin =
        IntStream.range(0, containers.size())
            .boxed()
            .sorted(Comparator.comparingInt(container -> containers.get(container).begin()))
            .toList();
    final Queue<Integer> openContainers =
        new PriorityQueue<>(
            Comparator.<Integer>comparingInt(container -> containers.get(container).end())
                .thenComparingInt(container -> container));
    final SortedSet<Integer> boundaries = new TreeSet<>();
    containers.forEach(
        container -> {
          boundaries.add(container.begin());
          boundaries.add(container.end());
        });
    final NavigableMap<Integer, Area> result = new TreeMap<>();
    int nextContainer = 0;
    for (final int boundary : boundaries) {
      while (nextContainer < containersByBegin.size()
          && containers.get(containersByBegin.get(nextContainer)).begin() <= boundary) {
        openContainers.add(containersByBegin.get(nextContainer));
        nextContainer++;
      }
      Integer innermostContainer = openContainers.peek();
      while (innermostContainer != null && containers.get(innermostContainer).end() <= boundary) {
        openContainers.poll();
        innermostContainer = openContainers.peek();
      }
      result.put(
          boundary, innermostContainer == null ? wholeText : containers.get(innermostContainer));
    }
    return result;
  }

  private static Area toArea(final TemplateParameter parameter) {
    return new Area(parameter.begin(), parameter.end());
  }

  private static Area toContentArea(final CompleteTagElement completeTag) {
    final int contentBegin = completeTag.startTag().end();
    final int contentEnd = completeTag.endTag().begin();
    return new Area(contentBegin, contentEnd);
  }
}
