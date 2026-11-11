package org.wpcleaner.lib.check.algorithm;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.springframework.stereotype.Component;
import org.wpcleaner.api.analysis.PageAnalysis;
import org.wpcleaner.api.analysis.externallink.ExternalLinkElement;
import org.wpcleaner.api.analysis.image.ImageElement;
import org.wpcleaner.api.analysis.internallink.InternalLinkElement;
import org.wpcleaner.api.analysis.tag.CompleteTagElement;
import org.wpcleaner.api.analysis.tag.HtmlTagType;
import org.wpcleaner.api.analysis.tag.TagType;
import org.wpcleaner.api.analysis.tag.WikiTagType;
import org.wpcleaner.api.repository.namespace.CommonNamespaces;
import org.wpcleaner.api.repository.specialpage.SpecialPage;
import org.wpcleaner.api.repository.specialpage.SpecialPageRepository;
import org.wpcleaner.lib.check.CheckAlgorithm;
import org.wpcleaner.lib.check.CheckError;
import org.wpcleaner.lib.check.Replacement;
import org.wpcleaner.lib.check.configuration.CheckAlgorithmConfiguration;
import org.wpcleaner.lib.check.configuration.CheckWikiConfigurationRepository;
import org.wpcleaner.lib.check.configuration.TemplateDefinition;

@Component
public class CheckAlgorithm090 implements CheckAlgorithm {

  private static final int ALGORITHM_NUMBER = 90;
  private static final String PARAMETER_HISTORY_TEMPLATES = "history_templates";
  private static final String PARAMETER_LINK_TEMPLATES = "link_templates";
  private static final String PARAMETER_OLDID_TEMPLATES = "oldid_templates";
  private static final String PARAMETER_PREFIX_INDEX_TEMPLATES = "prefix_index_templates";
  private static final String IMAGE_LINK = "img_link";
  private static final String PREFIX_INDEX = "Prefixindex";
  private static final char SPACE = ' ';

  private final CheckWikiConfigurationRepository checkWikiRepository;
  private final SpecialPageRepository specialPageRepository;

  public CheckAlgorithm090(
      final CheckWikiConfigurationRepository checkWikiRepository,
      final SpecialPageRepository specialPageRepository) {
    this.checkWikiRepository = checkWikiRepository;
    this.specialPageRepository = specialPageRepository;
  }

  @Override
  public int number() {
    return ALGORITHM_NUMBER;
  }

  @Override
  public List<CheckError> analyze(final PageAnalysis analysis) {
    final int namespace = analysis.getPageTitle().namespace();
    if (namespace % 2 != 0) {
      return List.of();
    }
    final CheckAlgorithmConfiguration configuration =
        checkWikiRepository
            .getConfiguration(analysis.getWiki())
            .getAlgorithm(ALGORITHM_NUMBER)
            .orElseGet(() -> new CheckAlgorithmConfiguration(ALGORITHM_NUMBER, Map.of()));
    final ArticleLinkTemplates templates =
        new ArticleLinkTemplates(
            configuration.getTemplateDefinitions(
                PARAMETER_HISTORY_TEMPLATES, ArticleLinkTemplates.HISTORY_FIELDS),
            configuration.getTemplateDefinitions(
                PARAMETER_OLDID_TEMPLATES, ArticleLinkTemplates.OLDID_FIELDS),
            configuration.getTemplateDefinitions(
                PARAMETER_PREFIX_INDEX_TEMPLATES, ArticleLinkTemplates.PREFIX_INDEX_FIELDS));
    final List<TemplateDefinition> linkTemplates =
        configuration.getTemplateDefinitions(
            PARAMETER_LINK_TEMPLATES, List.of(ArticleLink.URL_FIELD, ArticleLink.TEXT_FIELD));
    final List<CompleteTagElement> codeTags = findTags(analysis, HtmlTagType.CODE);
    final List<CheckError> errors = new ArrayList<>();
    analysis.getImages().forEach(image -> analyzeImage(analysis, image).ifPresent(errors::add));
    analysis.getInternalLinks().stream()
        .filter(link -> isLinkToHost(analysis, link))
        .forEach(
            link ->
                errors.add(new CheckError(ALGORITHM_NUMBER, link.begin(), link.end(), List.of())));
    analysis.getExternalLinks().stream()
        .filter(link -> !link.isInsideOne(codeTags))
        .forEach(
            link ->
                ArticleUrl.of(analysis.getWiki(), link.target())
                    .map(url -> ArticleLink.of(analysis, link, url, linkTemplates))
                    .ifPresent(
                        articleLink ->
                            errors.add(analyzeLink(analysis, link, articleLink, templates))));
    return errors.stream().sorted(Comparator.comparingInt(CheckError::begin)).toList();
  }

  private static List<CompleteTagElement> findTags(
      final PageAnalysis analysis, final TagType type) {
    return analysis.getCompleteTags().stream()
        .filter(tag -> type.getNormalizedName().equals(tag.startTag().normalizedName()))
        .toList();
  }

  private static Optional<CheckError> analyzeImage(
      final PageAnalysis analysis, final ImageElement image) {
    return image
        .findParameter(IMAGE_LINK)
        .flatMap(
            parameter ->
                Optional.ofNullable(parameter.argument())
                    .flatMap(argument -> ArticleUrl.of(analysis.getWiki(), argument))
                    .map(
                        url -> {
                          final boolean automatic =
                              analysis.getPageTitle().namespace() != CommonNamespaces.USER.id
                                  && url.attributes().isEmpty()
                                  && url.fragment() == null
                                  && analysis
                                      .normalizeTitle(url.title(), CommonNamespaces.MAIN.id)
                                      .filter(image.title()::equals)
                                      .isPresent();
                          return new CheckError(
                              ALGORITHM_NUMBER,
                              parameter.begin() - 1,
                              parameter.end(),
                              List.of(new Replacement("", automatic)));
                        }));
  }

  private static boolean isLinkToHost(final PageAnalysis analysis, final InternalLinkElement link) {
    final String target = link.target().trim().toLowerCase(Locale.ROOT);
    return analysis.getWiki().hosts().stream().anyMatch(target::startsWith);
  }

  private CheckError analyzeLink(
      final PageAnalysis analysis,
      final ExternalLinkElement link,
      final ArticleLink initialLink,
      final ArticleLinkTemplates templates) {
    final Optional<CompleteTagElement> ref =
        findTags(analysis, WikiTagType.REF).stream().filter(link::isInside).findFirst();
    final ArticleLink articleLink =
        ref.filter(tag -> isFullContent(analysis.getText(), tag, initialLink)).isPresent()
            ? initialLink.withoutAutomatic()
            : initialLink;
    final Optional<CheckError> refError =
        ref.flatMap(tag -> analyzeRef(analysis, tag, articleLink));
    if (refError.isPresent()) {
      return refError.get();
    }
    final Set<Replacement> replacements = new LinkedHashSet<>();
    templates.createReplacements(articleLink, isPrefixIndex(analysis, articleLink)).stream()
        .map(replacement -> new Replacement(replacement, false))
        .forEach(replacements::add);
    final String text = articleLink.text();
    if (text == null) {
      final int textEnd = skipSpacesBackward(analysis.getText(), articleLink.begin());
      final int textBegin = textEnd - articleLink.url().title().length();
      if (replacements.isEmpty()
          && textBegin >= 0
          && articleLink.isSameTitle(analysis.getText().substring(textBegin, textEnd))) {
        return new CheckError(
            ALGORITHM_NUMBER,
            textBegin,
            articleLink.end(),
            List.of(
                new Replacement(
                    articleLink.internalLink(analysis.getText().substring(textBegin, textEnd)),
                    articleLink.automatic())));
      }
      replacements.add(new Replacement(articleLink.internalLink(null), false));
    } else {
      replacements.add(new Replacement(articleLink.internalLink(text), articleLink.automatic()));
      replacements.add(new Replacement(text, false));
    }
    return new CheckError(
        ALGORITHM_NUMBER, articleLink.begin(), articleLink.end(), List.copyOf(replacements));
  }

  private boolean isPrefixIndex(final PageAnalysis analysis, final ArticleLink link) {
    return Optional.ofNullable(link.title())
        .filter(title -> title.namespace() == CommonNamespaces.SPECIAL.id)
        .flatMap(title -> specialPageRepository.findSpecialPage(analysis.getWiki(), title.text()))
        .map(SpecialPage::realName)
        .filter(PREFIX_INDEX::equals)
        .isPresent();
  }

  private static boolean isFullContent(
      final String text, final CompleteTagElement tag, final ArticleLink link) {
    return skipSpacesBackward(text, link.begin()) <= tag.startTag().end()
        && skipSpacesForward(text, link.end()) >= tag.endTag().begin();
  }

  private static Optional<CheckError> analyzeRef(
      final PageAnalysis analysis, final CompleteTagElement ref, final ArticleLink link) {
    final String text = analysis.getText();
    final int before = skipSpacesBackward(text, ref.begin());
    final Optional<InternalLinkElement> previousLink =
        analysis.getInternalLinks().stream()
            .filter(internalLink -> internalLink.end() == before)
            .filter(
                internalLink -> link.title() != null && link.title().equals(internalLink.title()))
            .findFirst();
    if (previousLink.isPresent()) {
      return Optional.of(
          new CheckError(
              ALGORITHM_NUMBER,
              previousLink.get().begin(),
              ref.end(),
              List.of(
                  new Replacement(
                      text.substring(previousLink.get().begin(), previousLink.get().end()),
                      false))));
    }
    final int textBegin = before - link.url().title().length();
    if (textBegin < 0 || !link.isSameTitle(text.substring(textBegin, before))) {
      return Optional.empty();
    }
    return Optional.of(
        new CheckError(
            ALGORITHM_NUMBER,
            textBegin,
            ref.end(),
            List.of(new Replacement(link.internalLink(text.substring(textBegin, before)), false))));
  }

  private static int skipSpacesBackward(final String text, final int index) {
    int begin = index;
    while (begin > 0 && text.charAt(begin - 1) == SPACE) {
      begin--;
    }
    return begin;
  }

  private static int skipSpacesForward(final String text, final int index) {
    int end = index;
    while (end < text.length() && text.charAt(end) == SPACE) {
      end++;
    }
    return end;
  }
}
