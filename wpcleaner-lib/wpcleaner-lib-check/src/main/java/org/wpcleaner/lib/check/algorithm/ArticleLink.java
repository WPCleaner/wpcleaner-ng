package org.wpcleaner.lib.check.algorithm;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.jspecify.annotations.Nullable;
import org.wpcleaner.api.analysis.PageAnalysis;
import org.wpcleaner.api.analysis.externallink.ExternalLinkElement;
import org.wpcleaner.api.analysis.pagetitle.PageTitle;
import org.wpcleaner.api.analysis.template.TemplateElement;
import org.wpcleaner.api.analysis.template.TemplateParameter;
import org.wpcleaner.api.repository.namespace.CommonNamespaces;
import org.wpcleaner.lib.check.configuration.TemplateDefinition;

record ArticleLink(
    ArticleUrl url,
    @Nullable PageTitle title,
    int begin,
    int end,
    @Nullable String text,
    boolean colon,
    boolean automatic) {

  static final String URL_FIELD = "url";
  static final String TEXT_FIELD = "text";
  private static final String VISUAL_EDITOR_NOTIFY = "venotify";
  private static final String VISUAL_EDITOR_CREATED = "created";
  private static final String RED_LINK = "redlink";
  private static final String SCROLL_TO_TEXT = ":~:";
  private static final char OPEN = '[';
  private static final char CLOSE = ']';
  private static final char PIPE = '|';
  private static final char SPACE = ' ';

  static ArticleLink of(
      final PageAnalysis analysis,
      final ExternalLinkElement link,
      final ArticleUrl url,
      final List<TemplateDefinition> linkTemplates) {
    final PageTitle title =
        analysis.normalizeTitle(url.title(), CommonNamespaces.MAIN.id).orElse(null);
    final boolean colon =
        title != null
            && (title.namespace() == CommonNamespaces.FILE.id
                || title.namespace() == CommonNamespaces.CATEGORY.id);
    final boolean automatic = isAutomatic(analysis, link, url, title);
    return findLinkTemplate(analysis, link, linkTemplates)
        .map(
            template ->
                new ArticleLink(
                    url,
                    title,
                    template.template().begin(),
                    template.template().end(),
                    template.text(),
                    colon,
                    automatic))
        .orElseGet(
            () -> {
              final String text = analysis.getText();
              final boolean doubled =
                  link.bracketed()
                      && link.begin() > 0
                      && text.charAt(link.begin() - 1) == OPEN
                      && link.end() < text.length()
                      && text.charAt(link.end()) == CLOSE;
              return new ArticleLink(
                  url,
                  title,
                  doubled ? link.begin() - 1 : link.begin(),
                  doubled ? link.end() + 1 : link.end(),
                  cleanText(link.text()),
                  colon,
                  automatic);
            });
  }

  private static boolean isAutomatic(
      final PageAnalysis analysis,
      final ExternalLinkElement link,
      final ArticleUrl url,
      @Nullable final PageTitle title) {
    return analysis.getPageTitle().namespace() != CommonNamespaces.USER.id
        && !analysis.getPageTitle().equals(title)
        && url.attributes().entrySet().stream().allMatch(ArticleLink::isAutomaticAttribute)
        && !link.target().contains(SCROLL_TO_TEXT);
  }

  private static boolean isAutomaticAttribute(final Map.Entry<String, String> attribute) {
    return switch (attribute.getKey()) {
      case RED_LINK -> true;
      case VISUAL_EDITOR_NOTIFY -> VISUAL_EDITOR_CREATED.equals(attribute.getValue());
      default -> false;
    };
  }

  @Nullable
  private static String cleanText(@Nullable final String text) {
    if (text == null) {
      return null;
    }
    int begin = 0;
    while (begin < text.length() && (text.charAt(begin) == PIPE || text.charAt(begin) == SPACE)) {
      begin++;
    }
    return begin < text.length() ? text.substring(begin) : null;
  }

  private static Optional<LinkTemplate> findLinkTemplate(
      final PageAnalysis analysis,
      final ExternalLinkElement link,
      final List<TemplateDefinition> linkTemplates) {
    return analysis.getTemplates().stream()
        .filter(link::isInside)
        .max(Comparator.comparingInt(TemplateElement::begin))
        .flatMap(
            template ->
                linkTemplates.stream()
                    .filter(
                        definition ->
                            analysis
                                .normalizeTitle(definition.template(), CommonNamespaces.TEMPLATE.id)
                                .filter(template.title()::equals)
                                .isPresent())
                    .filter(
                        definition ->
                            definition
                                .parameter(URL_FIELD)
                                .flatMap(name -> findParameterValue(template, name))
                                .filter(value -> value.equals(link.target().trim()))
                                .isPresent())
                    .findFirst()
                    .map(
                        definition ->
                            new LinkTemplate(
                                template,
                                definition
                                    .parameter(TEXT_FIELD)
                                    .flatMap(name -> findParameterValue(template, name))
                                    .filter(value -> !value.isEmpty())
                                    .orElse(null))));
  }

  private static Optional<String> findParameterValue(
      final TemplateElement template, final String name) {
    int position = 0;
    for (final TemplateParameter parameter : template.parameters()) {
      final String parameterName = parameter.name();
      if (parameterName == null) {
        position++;
      }
      if (name.equals(parameterName == null ? Integer.toString(position) : parameterName.trim())) {
        return Optional.of(parameter.value().trim());
      }
    }
    return Optional.empty();
  }

  ArticleLink withoutAutomatic() {
    return new ArticleLink(url, title, begin, end, text, colon, false);
  }

  String internalLink(@Nullable final String displayedText) {
    final String target =
        (colon || url.title().charAt(0) == '/' ? ":" : "") + url.titleAndFragment();
    if (displayedText == null) {
      return "[[" + target + "]]";
    }
    if (url.fragment() == null && displayedText.length() >= url.title().length()) {
      final String prefix = displayedText.substring(0, url.title().length());
      final String trail = displayedText.substring(url.title().length());
      if (!colon && isSameTitle(prefix) && trail.chars().allMatch(Character::isLetter)) {
        return "[[" + prefix + "]]" + trail;
      }
    }
    return "[[" + target + PIPE + displayedText + "]]";
  }

  boolean isSameTitle(final String otherTitle) {
    return !otherTitle.isEmpty()
        && Character.toUpperCase(otherTitle.charAt(0))
            == Character.toUpperCase(url.title().charAt(0))
        && otherTitle.substring(1).replace('_', ' ').equals(url.title().substring(1));
  }

  private record LinkTemplate(TemplateElement template, @Nullable String text) {}
}
