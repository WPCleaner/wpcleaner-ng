package org.wpcleaner.lib.check.algorithm;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;
import org.wpcleaner.api.analysis.Element;
import org.wpcleaner.api.analysis.PageAnalysis;
import org.wpcleaner.api.analysis.include.IncludeAreaType;
import org.wpcleaner.api.analysis.internallink.InternalLinkElement;
import org.wpcleaner.api.analysis.pagetitle.PageTitle;
import org.wpcleaner.api.analysis.tag.CompleteTagElement;
import org.wpcleaner.api.analysis.tag.WikiTagType;
import org.wpcleaner.api.analysis.template.TemplateElement;
import org.wpcleaner.api.analysis.title.TitleElement;
import org.wpcleaner.api.repository.configuration.WPCleanerConfiguration;
import org.wpcleaner.api.repository.configuration.WPCleanerConfigurationRepository;
import org.wpcleaner.api.repository.configuration.WPCleanerParameter;
import org.wpcleaner.api.repository.namespace.CommonNamespaces;
import org.wpcleaner.lib.check.CheckAlgorithm;
import org.wpcleaner.lib.check.CheckError;
import org.wpcleaner.lib.check.Replacement;
import org.wpcleaner.lib.check.configuration.CheckAlgorithmConfiguration;
import org.wpcleaner.lib.check.configuration.CheckWikiConfigurationRepository;
import org.wpcleaner.lib.check.configuration.TemplateGroups;

@Component
public class CheckAlgorithm048 implements CheckAlgorithm {

  private static final int ALGORITHM_NUMBER = 48;
  private static final String PARAMETER_IGNORE_GROUPS = "ignore_groups";
  private static final String PARAMETER_IGNORE_TEMPLATES = "ignore_templates";
  private static final String PARAMETER_IMAGEMAP = "imagemap";
  private static final String PARAMETER_TEMPLATE_GROUPS = "template_groups";
  private static final Set<String> SECTION_TRANSCLUSION_MAGIC_WORDS = Set.of("lst", "lsth", "lstx");
  private static final List<String> FOOTNOTE_ANCHOR_PREFIXES =
      List.of("cite", "fn", "ftn", "sdfootnote");
  private static final char APOSTROPHE = '\'';
  private static final String BOLD = "'''";
  private static final char LINE_END = '\n';

  private final CheckWikiConfigurationRepository checkWikiRepository;
  private final WPCleanerConfigurationRepository wpcleanerRepository;

  public CheckAlgorithm048(
      final CheckWikiConfigurationRepository checkWikiRepository,
      final WPCleanerConfigurationRepository wpcleanerRepository) {
    this.checkWikiRepository = checkWikiRepository;
    this.wpcleanerRepository = wpcleanerRepository;
  }

  @Override
  public int number() {
    return ALGORITHM_NUMBER;
  }

  @Override
  public List<CheckError> analyze(final PageAnalysis analysis) {
    if (isIgnoredPage(analysis)) {
      return List.of();
    }
    final CheckAlgorithmConfiguration configuration =
        checkWikiRepository
            .getConfiguration(analysis.getWiki())
            .getAlgorithm(ALGORITHM_NUMBER)
            .orElseGet(() -> new CheckAlgorithmConfiguration(ALGORITHM_NUMBER, Map.of()));
    final WPCleanerConfiguration wpcleanerConfiguration =
        wpcleanerRepository.getConfiguration(analysis.getWiki());
    final boolean imagemapReported = configuration.getBoolean(PARAMETER_IMAGEMAP);
    final List<CompleteTagElement> imagemaps = findImagemaps(analysis);
    final List<Element> ignoredAreas = findIgnoredAreas(analysis, imagemapReported, imagemaps);
    final Set<PageTitle> ignoredTemplates =
        findIgnoredTemplates(analysis, configuration, wpcleanerConfiguration);
    final ErrorContext context =
        new ErrorContext(
            analysis.getText(),
            analysis.getTitles().stream()
                .findFirst()
                .map(TitleElement::begin)
                .orElse(analysis.getText().length()),
            wpcleanerConfiguration.getString(WPCleanerParameter.APOSTROPHE_TEMPLATE).orElse(null));
    final PageTitle pageTitle = analysis.getPageTitle();
    final boolean anchorReported = pageTitle.namespace() == CommonNamespaces.MAIN.id;
    return analysis.getInternalLinks().stream()
        .filter(link -> pageTitle.equals(link.title()))
        .filter(link -> anchorReported || link.anchor() == null)
        .filter(link -> !link.isInsideOne(ignoredAreas))
        .filter(link -> !isInIgnoredTemplate(analysis, link, ignoredTemplates))
        .map(
            link ->
                imagemaps.stream()
                    .filter(link::isInside)
                    .findFirst()
                    .map(imagemap -> createImagemapError(context.text(), imagemap, link))
                    .orElseGet(() -> createError(context, link)))
        .toList();
  }

  private static CheckError createError(
      final ErrorContext context, final InternalLinkElement link) {
    final String anchor = link.anchor();
    if (anchor != null) {
      return new CheckError(
          ALGORITHM_NUMBER, link.begin(), link.end(), createAnchorReplacements(link, anchor));
    }
    final String text = context.text();
    final String displayedText = link.displayedText();
    final boolean inBold = isInBold(text, link);
    final boolean apostropheBefore =
        link.begin() > 1
            && text.charAt(link.begin() - 1) == APOSTROPHE
            && text.charAt(link.begin() - 2) != APOSTROPHE;
    final String prefix = apostropheBefore ? String.valueOf(APOSTROPHE) : "";
    final List<Replacement> replacements = new ArrayList<>();
    replacements.add(
        new Replacement(prefix + displayedText, link.end() > context.firstTitleBegin() || inBold));
    if (!inBold) {
      final String apostropheTemplate = context.apostropheTemplate();
      if (apostropheBefore && apostropheTemplate != null) {
        replacements.add(
            new Replacement("{{" + apostropheTemplate + "}}" + BOLD + displayedText + BOLD, false));
      }
      replacements.add(new Replacement(prefix + BOLD + displayedText + BOLD, false));
    }
    return new CheckError(
        ALGORITHM_NUMBER,
        apostropheBefore ? link.begin() - 1 : link.begin(),
        link.end(),
        replacements);
  }

  private static CheckError createImagemapError(
      final String text, final CompleteTagElement imagemap, final InternalLinkElement link) {
    final int lineBegin = text.lastIndexOf(LINE_END, link.begin() - 1) + 1;
    final int nextLineEnd = text.indexOf(LINE_END, link.end());
    final int lineEnd =
        Math.min(nextLineEnd < 0 ? text.length() : nextLineEnd, imagemap.endTag().begin());
    final boolean deletable =
        lineBegin > imagemap.startTag().end()
            && lineEnd < text.length()
            && text.charAt(lineEnd) == LINE_END;
    return new CheckError(
        ALGORITHM_NUMBER,
        lineBegin,
        lineEnd,
        deletable ? List.of(new Replacement("", false)) : List.of());
  }

  private static List<Replacement> createAnchorReplacements(
      final InternalLinkElement link, final String anchor) {
    if (anchor.isEmpty()) {
      return List.of();
    }
    final String linkText = link.text();
    if (linkText == null) {
      return List.of(
          new Replacement(createAnchorLink(anchor, anchor), false),
          new Replacement(createAnchorLink(anchor, link.displayedText()), false));
    }
    final boolean footnote = FOOTNOTE_ANCHOR_PREFIXES.stream().anyMatch(anchor::startsWith);
    return List.of(new Replacement(createAnchorLink(anchor, linkText), !footnote));
  }

  private static String createAnchorLink(final String anchor, final String linkText) {
    return "[[#" + anchor + "|" + linkText + "]]";
  }

  private static boolean isInBold(final String text, final InternalLinkElement link) {
    final int boldBegin = link.begin() - BOLD.length();
    return boldBegin >= 0
        && text.startsWith(BOLD, boldBegin)
        && (boldBegin == 0 || text.charAt(boldBegin - 1) != APOSTROPHE)
        && text.startsWith(BOLD, link.end())
        && (link.end() + BOLD.length() >= text.length()
            || text.charAt(link.end() + BOLD.length()) != APOSTROPHE);
  }

  private static boolean isIgnoredPage(final PageAnalysis analysis) {
    return analysis.getRedirect().isPresent()
        || analysis.getPageTitle().namespace() == CommonNamespaces.USER_TALK.id
        || analysis.getFunctions().stream()
            .anyMatch(function -> SECTION_TRANSCLUSION_MAGIC_WORDS.contains(function.magicWord()))
        || analysis.getTags().stream()
            .anyMatch(tag -> WikiTagType.SECTION.getNormalizedName().equals(tag.normalizedName()));
  }

  private static List<CompleteTagElement> findImagemaps(final PageAnalysis analysis) {
    return analysis.getCompleteTags().stream()
        .filter(
            tag -> WikiTagType.IMAGEMAP.getNormalizedName().equals(tag.startTag().normalizedName()))
        .toList();
  }

  private static List<Element> findIgnoredAreas(
      final PageAnalysis analysis,
      final boolean imagemapReported,
      final List<CompleteTagElement> imagemaps) {
    return Stream.<Element>concat(
            analysis.getIncludeAreas().stream()
                .filter(includeArea -> includeArea.type() != IncludeAreaType.NO_INCLUDE),
            imagemapReported ? Stream.empty() : imagemaps.stream())
        .toList();
  }

  private static Set<PageTitle> findIgnoredTemplates(
      final PageAnalysis analysis,
      final CheckAlgorithmConfiguration configuration,
      final WPCleanerConfiguration wpcleanerConfiguration) {
    final TemplateGroups groups =
        configuration
            .getTemplateGroups(PARAMETER_TEMPLATE_GROUPS)
            .merge(
                TemplateGroups.of(
                    wpcleanerConfiguration.getLines(WPCleanerParameter.TEMPLATE_GROUPS)));
    return configuration
        .getTemplateNames(PARAMETER_IGNORE_TEMPLATES, PARAMETER_IGNORE_GROUPS, groups)
        .stream()
        .flatMap(name -> analysis.normalizeTitle(name, CommonNamespaces.TEMPLATE.id).stream())
        .collect(Collectors.toSet());
  }

  private static boolean isInIgnoredTemplate(
      final PageAnalysis analysis,
      final InternalLinkElement link,
      final Set<PageTitle> ignoredTemplates) {
    return !ignoredTemplates.isEmpty()
        && analysis.getTemplates().stream()
            .filter(link::isInside)
            .max(Comparator.comparingInt(TemplateElement::begin))
            .filter(template -> ignoredTemplates.contains(template.title()))
            .isPresent();
  }

  private record ErrorContext(
      String text, int firstTitleBegin, @Nullable String apostropheTemplate) {}
}
