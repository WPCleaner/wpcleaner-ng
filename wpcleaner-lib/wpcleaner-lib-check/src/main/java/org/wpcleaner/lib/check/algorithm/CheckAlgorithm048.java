package org.wpcleaner.lib.check.algorithm;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import org.springframework.stereotype.Component;
import org.wpcleaner.api.analysis.Element;
import org.wpcleaner.api.analysis.PageAnalysis;
import org.wpcleaner.api.analysis.include.IncludeAreaType;
import org.wpcleaner.api.analysis.pagetitle.PageTitle;
import org.wpcleaner.api.analysis.tag.WikiTagType;
import org.wpcleaner.api.repository.namespace.CommonNamespaces;
import org.wpcleaner.lib.check.CheckAlgorithm;
import org.wpcleaner.lib.check.CheckError;

@Component
public class CheckAlgorithm048 implements CheckAlgorithm {

  private static final int ALGORITHM_NUMBER = 48;
  private static final Set<String> SECTION_TRANSCLUSION_MAGIC_WORDS = Set.of("lst", "lsth", "lstx");

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
    return analysis.getInternalLinks().stream()
        .filter(link -> pageTitle.equals(link.title()))
        .filter(link -> anchorReported || link.anchor() == null)
        .filter(link -> !link.isInsideOne(ignoredAreas))
        .map(link -> new CheckError(ALGORITHM_NUMBER, link.begin(), link.end()))
        .toList();
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
