package org.wpcleaner.lib.check.algorithm;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.wpcleaner.api.analysis.PageAnalysis;
import org.wpcleaner.api.analysis.image.ImageElement;
import org.wpcleaner.api.analysis.internallink.InternalLinkElement;
import org.wpcleaner.api.analysis.pagetitle.PageTitle;
import org.wpcleaner.api.analysis.tag.HtmlTagType;
import org.wpcleaner.api.analysis.tag.TagType;
import org.wpcleaner.api.analysis.tag.WikiTagType;
import org.wpcleaner.api.analysis.template.TemplateElement;
import org.wpcleaner.api.analysis.template.TemplateParameter;

enum FollowingText {
  END_OF_LINE,
  UPPERCASE,
  LOWERCASE,
  SAFE_TEMPLATE_PARAMETER,
  OTHER;

  private static final String SPACES = "  ";
  private static final String APOSTROPHE = "'";
  private static final String ITALIC = "''";
  private static final String LINK_START = "[[";
  private static final String LINK_END = "]]";
  private static final String TEMPLATE_END = "}}";
  private static final String PIPE = "|";
  private static final String CLOSING_QUOTE = "»";
  private static final char NEW_LINE = '\n';
  private static final char TAG_START = '<';
  private static final Set<TagType> END_OF_LINE_TAGS =
      Set.of(HtmlTagType.BLOCKQUOTE, HtmlTagType.BR, WikiTagType.GALLERY);

  static FollowingText at(
      final PageAnalysis analysis,
      final int index,
      final Map<PageTitle, Set<String>> safeTemplates) {
    final String text = analysis.getText();
    int current = skip(text, index, SPACES);
    if (text.startsWith(ITALIC, current)) {
      current = skip(text, current, APOSTROPHE);
    }
    final int imageBegin = current;
    final int position =
        analysis.getImages().stream()
            .filter(image -> image.begin() == imageBegin)
            .findFirst()
            .map(ImageElement::end)
            .orElse(current);
    if (position >= text.length()
        || text.charAt(position) == NEW_LINE
        || text.startsWith(CLOSING_QUOTE, position)) {
      return END_OF_LINE;
    }
    return ofCharacter(text.charAt(position))
        .orElseGet(() -> ofElement(analysis, position, safeTemplates));
  }

  private static FollowingText ofElement(
      final PageAnalysis analysis,
      final int index,
      final Map<PageTitle, Set<String>> safeTemplates) {
    final String text = analysis.getText();
    if (text.startsWith(LINK_END, index)) {
      return analysis.getImages().stream()
              .anyMatch(image -> image.end() == index + LINK_END.length())
          ? END_OF_LINE
          : OTHER;
    }
    if (text.startsWith(LINK_START, index)) {
      return analysis.getInternalLinks().stream()
          .filter(link -> link.begin() == index)
          .findFirst()
          .map(InternalLinkElement::displayedText)
          .filter(displayed -> !displayed.isEmpty())
          .flatMap(displayed -> ofCharacter(displayed.charAt(0)))
          .orElse(OTHER);
    }
    if (text.charAt(index) == TAG_START) {
      return analysis.getTags().stream()
              .filter(tag -> tag.begin() == index)
              .anyMatch(
                  tag ->
                      END_OF_LINE_TAGS.stream()
                          .anyMatch(type -> type.getNormalizedName().equals(tag.normalizedName())))
          ? END_OF_LINE
          : OTHER;
    }
    if (text.startsWith(TEMPLATE_END, index) || text.startsWith(PIPE, index)) {
      return analysis.getTemplates().stream()
              .filter(template -> template.begin() < index && index < template.end())
              .max(Comparator.comparingInt(TemplateElement::begin))
              .filter(
                  template ->
                      findParameterName(template, index)
                          .filter(
                              name ->
                                  safeTemplates
                                      .getOrDefault(template.title(), Set.of())
                                      .contains(name))
                          .isPresent())
              .isPresent()
          ? SAFE_TEMPLATE_PARAMETER
          : OTHER;
    }
    return OTHER;
  }

  private static Optional<FollowingText> ofCharacter(final char character) {
    if (Character.isUpperCase(character)) {
      return Optional.of(UPPERCASE);
    }
    if (Character.isLowerCase(character)) {
      return Optional.of(LOWERCASE);
    }
    return Optional.empty();
  }

  private static Optional<String> findParameterName(
      final TemplateElement template, final int index) {
    int position = 0;
    for (final TemplateParameter parameter : template.parameters()) {
      final String parameterName = parameter.name();
      if (parameterName == null) {
        position++;
      }
      if (parameter.begin() <= index && index <= parameter.end()) {
        return Optional.of(parameterName == null ? Integer.toString(position) : parameterName);
      }
    }
    return Optional.empty();
  }

  private static int skip(final String text, final int index, final String characters) {
    int current = index;
    while (current < text.length() && characters.indexOf(text.charAt(current)) >= 0) {
      current++;
    }
    return current;
  }
}
