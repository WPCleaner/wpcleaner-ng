package org.wpcleaner.api.analysis;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.wpcleaner.api.analysis.category.CategoryElement;
import org.wpcleaner.api.analysis.include.IncludeAreaElement;
import org.wpcleaner.api.analysis.include.IncludeAreaType;
import org.wpcleaner.api.analysis.internallink.InternalLinkElement;
import org.wpcleaner.api.analysis.pagetitle.PageTitle;
import org.wpcleaner.api.analysis.tag.TagElement;
import org.wpcleaner.api.analysis.tag.TagKind;

class PageAnalysisIncludeAreaTest {

  @DisplayName("should analyze includeonly area and its content")
  @Test
  void analyzeIncludeOnly() {
    // GIVEN
    final String text = "<includeonly>[[Catégorie:A]]</includeonly>";

    // WHEN
    final PageAnalysis analysis = PageAnalysisTestFactory.createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getIncludeAreas())
        .containsExactly(new IncludeAreaElement(0, text.length(), IncludeAreaType.INCLUDE_ONLY));
    Assertions.assertThat(analysis.getCompleteTags()).hasSize(1);
    Assertions.assertThat(analysis.getCategories())
        .containsExactly(
            new CategoryElement(13, 28, "Catégorie", "A", new PageTitle(14, "A"), null));
  }

  @DisplayName("should run an unclosed includeonly to the end of the text")
  @Test
  void analyzeUnclosedIncludeOnly() {
    // GIVEN
    final String text = "[[Before]]<includeonly>[[Link]]";

    // WHEN
    final PageAnalysis analysis = PageAnalysisTestFactory.createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getIncludeAreas())
        .containsExactly(new IncludeAreaElement(10, text.length(), IncludeAreaType.INCLUDE_ONLY));
    Assertions.assertThat(analysis.getCompleteTags()).isEmpty();
    Assertions.assertThat(analysis.getInternalLinks()).hasSize(2);
  }

  @DisplayName("should end includeonly at its first end tag")
  @Test
  void analyzeNestedIncludeOnly() {
    // GIVEN
    final String text = "<includeonly>a<includeonly>b</includeonly>c</includeonly>";

    // WHEN
    final PageAnalysis analysis = PageAnalysisTestFactory.createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getIncludeAreas())
        .containsExactly(
            new IncludeAreaElement(0, 42, IncludeAreaType.INCLUDE_ONLY),
            new IncludeAreaElement(14, 28, IncludeAreaType.INCLUDE_ONLY));
    Assertions.assertThat(analysis.getTags())
        .last()
        .isEqualTo(new TagElement(43, text.length(), "includeonly", TagKind.END, List.of()));
    Assertions.assertThat(analysis.getCompleteTags()).hasSize(1);
  }

  @DisplayName("should analyze noinclude and includeonly areas on the same page")
  @Test
  void analyzeNoIncludeAndIncludeOnly() {
    // GIVEN
    final String text = "<NoInclude>doc</noinclude><includeonly>[[Link]]</includeonly>";

    // WHEN
    final PageAnalysis analysis = PageAnalysisTestFactory.createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getIncludeAreas())
        .containsExactly(
            new IncludeAreaElement(0, 26, IncludeAreaType.NO_INCLUDE),
            new IncludeAreaElement(26, text.length(), IncludeAreaType.INCLUDE_ONLY));
    Assertions.assertThat(analysis.getInternalLinks())
        .containsExactly(
            new InternalLinkElement(39, 47, "Link", new PageTitle(0, "Link"), null, null, false));
  }

  @DisplayName("should analyze onlyinclude areas")
  @Test
  void analyzeOnlyInclude() {
    // GIVEN
    final String text = "a<onlyinclude>b</onlyinclude>c<onlyinclude>d</onlyinclude>";

    // WHEN
    final PageAnalysis analysis = PageAnalysisTestFactory.createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getIncludeAreas())
        .containsExactly(
            new IncludeAreaElement(1, 29, IncludeAreaType.ONLY_INCLUDE),
            new IncludeAreaElement(30, text.length(), IncludeAreaType.ONLY_INCLUDE));
    Assertions.assertThat(analysis.getCompleteTags()).hasSize(2);
  }

  @DisplayName("should not analyze onlyinclude area when tags are not in lower case")
  @Test
  void analyzeOnlyIncludeWithUpperCase() {
    // GIVEN
    final String text = "<OnlyInclude>a</OnlyInclude>";

    // WHEN
    final PageAnalysis analysis = PageAnalysisTestFactory.createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getIncludeAreas()).isEmpty();
    Assertions.assertThat(analysis.getCompleteTags()).hasSize(1);
  }

  @DisplayName("should not analyze includeonly inside a comment")
  @Test
  void analyzeIncludeOnlyInsideComment() {
    // GIVEN
    final String text = "<!--<includeonly>-->[[Link]]";

    // WHEN
    final PageAnalysis analysis = PageAnalysisTestFactory.createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getIncludeAreas()).isEmpty();
    Assertions.assertThat(analysis.getInternalLinks()).hasSize(1);
  }
}
