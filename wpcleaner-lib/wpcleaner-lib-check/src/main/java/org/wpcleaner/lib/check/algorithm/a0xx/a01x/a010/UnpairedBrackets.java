package org.wpcleaner.lib.check.algorithm.a0xx.a01x.a010;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.wpcleaner.api.analysis.PageAnalysis;
import org.wpcleaner.api.analysis.image.ImageElement;
import org.wpcleaner.api.analysis.internallink.InternalLinkBuilder;
import org.wpcleaner.api.analysis.internallink.InternalLinkElement;
import org.wpcleaner.lib.check.CheckError;
import org.wpcleaner.lib.check.Replacement;
import org.wpcleaner.lib.check.algorithm.TextExclusions.Area;

record UnpairedBrackets(PageAnalysis analysis, SingleBrackets singleBrackets) {

  private static final String ALTERNATE_TEXT = "img_alt";
  private static final int LINK_END_LENGTH = 2;

  List<CheckError> analyze(final int algorithm) {
    return Stream.concat(
            analysis.getInternalLinks().stream().flatMap(link -> analyzeLink(link).stream()),
            analysis.getImages().stream().flatMap(image -> analyzeImage(image).stream()))
        .map(
            replacement ->
                new CheckError(
                    algorithm,
                    replacement.begin(),
                    replacement.end(),
                    List.of(new Replacement(replacement.text(), false))))
        .toList();
  }

  private Optional<ElementReplacement> analyzeLink(final InternalLinkElement link) {
    final String linkText = link.text();
    if (linkText == null) {
      return Optional.empty();
    }
    final int textEnd = link.end() - LINK_END_LENGTH;
    final String cleanText = singleBrackets.removeUnpaired(textEnd - linkText.length(), textEnd);
    if (cleanText.equals(linkText)) {
      return Optional.empty();
    }
    return Optional.of(
        new ElementReplacement(
            link.begin(),
            link.end(),
            InternalLinkBuilder.to(link.target())
                .withAnchor(link.anchor())
                .withText(cleanText)
                .withColon(link.leadingColon())
                .build()));
  }

  private Optional<ElementReplacement> analyzeImage(final ImageElement image) {
    final String text = analysis.getText();
    final List<Area> areas =
        Stream.of(image.findCaption(), image.findParameter(ALTERNATE_TEXT))
            .flatMap(Optional::stream)
            .map(parameter -> new Area(parameter.begin(), parameter.end()))
            .sorted(Comparator.comparingInt(Area::begin))
            .toList();
    final StringBuilder replacement = new StringBuilder();
    int previousEnd = image.begin();
    for (final Area area : areas) {
      replacement
          .append(text, previousEnd, area.begin())
          .append(singleBrackets.removeUnpaired(area.begin(), area.end()));
      previousEnd = area.end();
    }
    replacement.append(text, previousEnd, image.end());
    if (replacement.toString().equals(text.substring(image.begin(), image.end()))) {
      return Optional.empty();
    }
    return Optional.of(new ElementReplacement(image.begin(), image.end(), replacement.toString()));
  }

  private record ElementReplacement(int begin, int end, String text) {}
}
