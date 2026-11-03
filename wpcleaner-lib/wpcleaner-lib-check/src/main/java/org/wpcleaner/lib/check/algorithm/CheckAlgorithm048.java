package org.wpcleaner.lib.check.algorithm;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import org.springframework.stereotype.Component;
import org.wpcleaner.api.analysis.Element;
import org.wpcleaner.api.analysis.PageAnalysis;
import org.wpcleaner.api.analysis.include.IncludeAreaType;
import org.wpcleaner.api.analysis.internallink.InternalLinkElement;
import org.wpcleaner.api.analysis.pagetitle.PageTitle;
import org.wpcleaner.api.analysis.tag.WikiTagType;
import org.wpcleaner.api.analysis.title.TitleElement;
import org.wpcleaner.api.repository.namespace.CommonNamespaces;
import org.wpcleaner.lib.check.CheckAlgorithm;
import org.wpcleaner.lib.check.CheckError;
import org.wpcleaner.lib.check.Replacement;

@Component
public class CheckAlgorithm048 implements CheckAlgorithm {

  private static final int ALGORITHM_NUMBER = 48;
  private static final Set<String> SECTION_TRANSCLUSION_MAGIC_WORDS = Set.of("lst", "lsth", "lstx");
  private static final List<String> FOOTNOTE_ANCHOR_PREFIXES =
      List.of("cite", "fn", "ftn", "sdfootnote");
  private static final char APOSTROPHE = '\'';
  private static final String BOLD = "'''";

  @Override
  public int number() {
    return ALGORITHM_NUMBER;
  }

  @Override
  public List<CheckError> analyze(final PageAnalysis analysis) {
    if (isIgnoredPage(analysis)) {
      return List.of();
    }
    final PageTitle pageTitle = analysis.getPageTitle();
    final boolean anchorReported = pageTitle.namespace() == CommonNamespaces.MAIN.id;
    final List<Element> ignoredAreas = findIgnoredAreas(analysis);
    final int firstTitleBegin =
        analysis.getTitles().stream()
            .findFirst()
            .map(TitleElement::begin)
            .orElse(analysis.getText().length());
    return analysis.getInternalLinks().stream()
        .filter(link -> pageTitle.equals(link.title()))
        .filter(link -> anchorReported || link.anchor() == null)
        .filter(link -> !link.isInsideOne(ignoredAreas))
        .map(link -> createError(analysis.getText(), firstTitleBegin, link))
        .toList();
  }

  private static CheckError createError(
      final String text, final int firstTitleBegin, final InternalLinkElement link) {
    final String anchor = link.anchor();
    if (anchor != null) {
      return new CheckError(
          ALGORITHM_NUMBER, link.begin(), link.end(), createAnchorReplacements(link, anchor));
    }
    final String displayedText = link.displayedText();
    final boolean inBold = isInBold(text, link);
    final boolean apostropheBefore =
        link.begin() > 1
            && text.charAt(link.begin() - 1) == APOSTROPHE
            && text.charAt(link.begin() - 2) != APOSTROPHE;
    final String prefix = apostropheBefore ? String.valueOf(APOSTROPHE) : "";
    final List<Replacement> replacements = new ArrayList<>();
    replacements.add(
        new Replacement(prefix + displayedText, link.end() > firstTitleBegin || inBold));
    if (!inBold) {
      replacements.add(new Replacement(prefix + BOLD + displayedText + BOLD, false));
    }
    return new CheckError(
        ALGORITHM_NUMBER,
        apostropheBefore ? link.begin() - 1 : link.begin(),
        link.end(),
        replacements);
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

  private static List<Element> findIgnoredAreas(final PageAnalysis analysis) {
    return Stream.<Element>concat(
            analysis.getIncludeAreas().stream()
                .filter(includeArea -> includeArea.type() != IncludeAreaType.NO_INCLUDE),
            analysis.getCompleteTags().stream()
                .filter(
                    tag ->
                        WikiTagType.IMAGEMAP
                            .getNormalizedName()
                            .equals(tag.startTag().normalizedName())))
        .toList();
  }
}
