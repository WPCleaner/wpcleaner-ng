package org.wpcleaner.api.analysis.wiki;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;
import org.wpcleaner.api.analysis.preprocessor.PreprocessorContainer;
import org.wpcleaner.api.analysis.tag.CompleteTagElement;
import org.wpcleaner.api.analysis.template.TemplateParameter;

final class TextContainers {

  private final Area wholeText;
  private final List<Area> containers;

  TextContainers(final String text, final PreprocessorContainer preprocessor) {
    this.wholeText = new Area(0, text.length());
    this.containers =
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
  }

  Area findContainer(final int index) {
    return containers.stream()
        .filter(container -> container.begin() <= index && index < container.end())
        .min(Comparator.comparingInt(Area::end))
        .orElse(wholeText);
  }

  boolean isWholeText(final Area container) {
    return wholeText.equals(container);
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
