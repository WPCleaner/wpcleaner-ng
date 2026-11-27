package org.wpcleaner.lib.check.algorithm.a0xx.a03x.a032;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import org.springframework.stereotype.Component;
import org.wpcleaner.api.analysis.Element;
import org.wpcleaner.api.analysis.PageAnalysis;
import org.wpcleaner.api.analysis.internallink.InternalLinkBuilder;
import org.wpcleaner.api.analysis.internallink.InternalLinkElement;
import org.wpcleaner.lib.check.CheckAlgorithm;
import org.wpcleaner.lib.check.CheckError;
import org.wpcleaner.lib.check.Replacement;
import org.wpcleaner.lib.check.algorithm.TextExclusions;

@Component
public class CheckAlgorithm032 implements CheckAlgorithm {

  private static final int ALGORITHM_NUMBER = 32;
  private static final char PIPE = '|';
  private static final String LINK_END = "]]";

  @Override
  public int number() {
    return ALGORITHM_NUMBER;
  }

  @Override
  public List<CheckError> analyze(final PageAnalysis analysis) {
    final TextExclusions exclusions = TextExclusions.of(analysis);
    final List<Element> nestedElements = nestedElements(analysis);
    return analysis.getInternalLinks().stream()
        .map(link -> createError(analysis.getText(), link, exclusions, nestedElements))
        .flatMap(Optional::stream)
        .toList();
  }

  private static List<Element> nestedElements(final PageAnalysis analysis) {
    return Stream.of(
            analysis.getTemplates(),
            analysis.getFunctions(),
            analysis.getParameters(),
            analysis.getTags())
        .<Element>flatMap(List::stream)
        .toList();
  }

  private static Optional<CheckError> createError(
      final String text,
      final InternalLinkElement link,
      final TextExclusions exclusions,
      final List<Element> nestedElements) {
    final String linkText = link.text();
    if (linkText == null) {
      return Optional.empty();
    }
    final int textBegin = link.end() - LINK_END.length() - linkText.length();
    final List<Integer> pipes =
        findPipes(text, textBegin, link.end() - LINK_END.length(), exclusions, nestedElements);
    if (pipes.isEmpty()) {
      return Optional.empty();
    }
    return Optional.of(
        new CheckError(
            ALGORITHM_NUMBER,
            link.begin(),
            link.end(),
            createReplacements(text, link, linkText, splitText(text, textBegin, pipes, link))));
  }

  private static List<Integer> findPipes(
      final String text,
      final int textBegin,
      final int textEnd,
      final TextExclusions exclusions,
      final List<Element> nestedElements) {
    final List<Integer> pipes = new ArrayList<>();
    for (int index = text.indexOf(PIPE, textBegin);
        index >= 0 && index < textEnd;
        index = text.indexOf(PIPE, index + 1)) {
      if (!exclusions.contains(index) && !isInsideNestedElement(index, textBegin, nestedElements)) {
        pipes.add(index);
      }
    }
    return pipes;
  }

  private static boolean isInsideNestedElement(
      final int index, final int textBegin, final List<Element> nestedElements) {
    return nestedElements.stream()
        .anyMatch(
            element ->
                element.begin() >= textBegin && element.begin() <= index && index < element.end());
  }

  private static List<String> splitText(
      final String text,
      final int textBegin,
      final List<Integer> pipes,
      final InternalLinkElement link) {
    final List<String> parts = new ArrayList<>();
    int partBegin = textBegin;
    for (final int pipe : pipes) {
      parts.add(text.substring(partBegin, pipe));
      partBegin = pipe + 1;
    }
    parts.add(text.substring(partBegin, link.end() - LINK_END.length()));
    return parts;
  }

  private static List<Replacement> createReplacements(
      final String text,
      final InternalLinkElement link,
      final String linkText,
      final List<String> parts) {
    final boolean emptyTarget = link.target().isEmpty() && link.anchor() == null;
    final Set<String> links = new LinkedHashSet<>();
    for (final String part : parts) {
      if (!part.isBlank()) {
        links.add(
            InternalLinkBuilder.to(link.target())
                .withAnchor(link.anchor())
                .withText(part)
                .withColon(link.leadingColon())
                .build());
      }
    }
    final boolean automatic = !emptyTarget && links.size() == 1;
    final List<Replacement> replacements = new ArrayList<>();
    if (emptyTarget) {
      replacements.add(new Replacement(InternalLinkBuilder.to(linkText).build(), false));
    }
    links.forEach(replacement -> replacements.add(new Replacement(replacement, automatic)));
    replacements.add(
        new Replacement(
            "{{" + text.substring(link.begin() + 2, link.end() - LINK_END.length()) + "}}", false));
    return List.copyOf(replacements);
  }
}
