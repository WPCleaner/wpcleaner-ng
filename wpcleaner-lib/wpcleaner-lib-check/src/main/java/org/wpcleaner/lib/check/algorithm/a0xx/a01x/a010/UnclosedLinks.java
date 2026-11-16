package org.wpcleaner.lib.check.algorithm.a0xx.a01x.a010;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jspecify.annotations.Nullable;
import org.wpcleaner.api.analysis.Element;
import org.wpcleaner.api.analysis.PageAnalysis;
import org.wpcleaner.api.analysis.externallink.ExternalLinkElement;
import org.wpcleaner.api.analysis.tag.CompleteTagElement;
import org.wpcleaner.api.analysis.tag.HtmlTagType;
import org.wpcleaner.api.repository.namespace.CommonNamespaces;
import org.wpcleaner.lib.check.CheckError;
import org.wpcleaner.lib.check.Replacement;
import org.wpcleaner.lib.check.algorithm.TextExclusions;

record UnclosedLinks(PageAnalysis analysis, TextExclusions exclusions) {

  private static final String LINK_START = "[[";
  private static final String LINK_END = "]]";
  private static final String TEMPLATE_START = "{{";
  private static final String TEMPLATE_END = "}}";
  private static final char CURLY_CLOSE = '}';
  private static final char LINE_END = '\n';
  private static final char NAMESPACE_SEPARATOR = ':';
  private static final String LINK_BREAKERS = "\n[{";
  private static final String LINK_ENDS = "]}";

  List<CheckError> analyze(final int algorithm) {
    final String text = analysis.getText();
    final Map<Integer, Element> elements = findSquareBracketElements();
    final Set<Integer> externalLinkBegins =
        analysis.getExternalLinks().stream()
            .map(ExternalLinkElement::begin)
            .collect(Collectors.toSet());
    final List<CompleteTagElement> ignoredAreas = findIgnoredAreas();
    final List<CheckError> errors = new ArrayList<>();
    int index = text.indexOf(LINK_START);
    while (index >= 0) {
      final int bracketsEnd = SingleBrackets.skipForward(text, index, SingleBrackets.OPEN);
      int nextIndex = index + LINK_START.length();
      if (!elements.containsKey(index)
          && !externalLinkBegins.contains(bracketsEnd - 1)
          && !exclusions.contains(index)
          && !isInsideOne(ignoredAreas, index)
          && !isClosed(index, bracketsEnd)) {
        final CheckError error = createError(algorithm, index, bracketsEnd, elements);
        errors.add(error);
        nextIndex = error.end();
      }
      index = text.indexOf(LINK_START, nextIndex);
    }
    return errors;
  }

  private boolean isClosed(final int begin, final int bracketsEnd) {
    final String text = analysis.getText();
    return bracketsEnd - begin == LINK_START.length()
        && text.startsWith(
            LINK_END, indexOfAny(text, bracketsEnd, LINK_BREAKERS + SingleBrackets.CLOSE));
  }

  private Map<Integer, Element> findSquareBracketElements() {
    return Stream.<List<? extends Element>>of(
            analysis.getInternalLinks(),
            analysis.getImages(),
            analysis.getCategories(),
            analysis.getLanguageLinks(),
            analysis.getInterwikiLinks())
        .<Element>flatMap(List::stream)
        .collect(Collectors.toMap(Element::begin, element -> element, (first, _) -> first));
  }

  private List<CompleteTagElement> findIgnoredAreas() {
    return analysis.getCompleteTags().stream().filter(UnclosedLinks::isCode).toList();
  }

  private static boolean isCode(final CompleteTagElement tag) {
    return HtmlTagType.CODE.getNormalizedName().equals(tag.startTag().normalizedName());
  }

  private static boolean isInsideOne(final List<CompleteTagElement> areas, final int index) {
    return areas.stream().anyMatch(area -> area.begin() <= index && index < area.end());
  }

  private CheckError createError(
      final int algorithm,
      final int begin,
      final int bracketsEnd,
      final Map<Integer, Element> elements) {
    final String text = analysis.getText();
    if (bracketsEnd - begin > LINK_START.length()) {
      return createExtraBracketsError(
          algorithm, begin, bracketsEnd, elements.get(bracketsEnd - LINK_START.length()));
    }
    final int breaker = indexOfAny(text, bracketsEnd, LINK_BREAKERS + LINK_ENDS);
    if (breaker >= text.length() || LINK_ENDS.indexOf(text.charAt(breaker)) < 0) {
      return new CheckError(algorithm, begin, bracketsEnd, List.of());
    }
    if (text.charAt(breaker) == CURLY_CLOSE) {
      final int end = Math.min(breaker + TEMPLATE_END.length(), text.length());
      return new CheckError(
          algorithm,
          begin,
          text.charAt(end - 1) == CURLY_CLOSE ? end : breaker + 1,
          List.of(
              new Replacement(text.substring(begin, breaker) + LINK_END, false),
              new Replacement(
                  TEMPLATE_START + text.substring(bracketsEnd, breaker) + TEMPLATE_END, false)));
    }
    return createSingleBracketError(algorithm, begin, breaker);
  }

  private CheckError createExtraBracketsError(
      final int algorithm,
      final int begin,
      final int bracketsEnd,
      @Nullable final Element element) {
    final String text = analysis.getText();
    if (element == null) {
      return new CheckError(
          algorithm, begin, bracketsEnd, List.of(new Replacement(LINK_START, false)));
    }
    return new CheckError(
        algorithm,
        begin,
        SingleBrackets.skipForward(text, element.end(), SingleBrackets.CLOSE),
        List.of(new Replacement(text.substring(element.begin(), element.end()), isArticle())));
  }

  private CheckError createSingleBracketError(
      final int algorithm, final int begin, final int firstClose) {
    final String text = analysis.getText();
    final String unclosedLink = text.substring(begin, firstClose);
    final int secondClose = indexOfAny(text, firstClose + 1, LINK_BREAKERS + SingleBrackets.CLOSE);
    if (secondClose >= text.length() || text.charAt(secondClose) != SingleBrackets.CLOSE) {
      return new CheckError(
          algorithm,
          begin,
          firstClose + 1,
          List.of(new Replacement(unclosedLink + LINK_END, isAutomatic(begin, firstClose))));
    }
    final String suffix = text.substring(firstClose + 1, secondClose);
    return new CheckError(
        algorithm,
        begin,
        secondClose + 1,
        List.of(
            new Replacement(unclosedLink + LINK_END + suffix, false),
            new Replacement(unclosedLink + suffix + LINK_END, false)));
  }

  private boolean isAutomatic(final int begin, final int firstClose) {
    final String text = analysis.getText();
    final String target = text.substring(begin + LINK_START.length(), firstClose);
    return isArticle()
        && (begin == 0 || text.charAt(begin - 1) == LINE_END)
        && (firstClose + 1 == text.length() || text.charAt(firstClose + 1) == LINE_END)
        && target.indexOf(NAMESPACE_SEPARATOR) > 0
        && analysis
            .normalizeTitle(target, CommonNamespaces.MAIN.id)
            .filter(title -> title.namespace() == CommonNamespaces.CATEGORY.id)
            .isPresent();
  }

  private boolean isArticle() {
    return analysis.getPageTitle().namespace() == CommonNamespaces.MAIN.id;
  }

  private static int indexOfAny(final String text, final int from, final String characters) {
    int index = from;
    while (index < text.length() && characters.indexOf(text.charAt(index)) < 0) {
      index++;
    }
    return index;
  }
}
