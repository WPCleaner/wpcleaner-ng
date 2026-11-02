package org.wpcleaner.api.analysis;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.wpcleaner.api.analysis.category.CategoryElement;
import org.wpcleaner.api.analysis.internallink.InternalLinkElement;
import org.wpcleaner.api.analysis.interwikilink.InterwikiLinkElement;
import org.wpcleaner.api.analysis.listitem.ListItemElement;
import org.wpcleaner.api.analysis.pagetitle.PageTitle;
import org.wpcleaner.api.analysis.paragraph.ParagraphElement;
import org.wpcleaner.api.analysis.redirect.RedirectElement;
import org.wpcleaner.api.repository.namespace.CommonNamespaces;

class PageAnalysisRedirectTest {

  @DisplayName("should analyze a redirect and the text after it")
  @Test
  void analyzeRedirect() {
    // GIVEN
    final String text = "#REDIRECTION [[Paris]]\n[[Catégorie:X]]";

    // WHEN
    final PageAnalysis analysis = PageAnalysisTestFactory.createPageAnalysis(text);

    // THEN
    final InternalLinkElement link =
        new InternalLinkElement(
            13, 22, "Paris", new PageTitle(CommonNamespaces.MAIN.id, "Paris"), null, null, false);
    Assertions.assertThat(analysis.getRedirect())
        .contains(new RedirectElement(0, 22, "#REDIRECTION", link));
    Assertions.assertThat(analysis.getInternalLinks()).containsExactly(link);
    Assertions.assertThat(analysis.getCategories())
        .containsExactly(
            new CategoryElement(
                23, 38, "Catégorie", "X", new PageTitle(CommonNamespaces.CATEGORY.id, "X"), null));
    Assertions.assertThat(analysis.getListItems()).isEmpty();
  }

  @DisplayName("should analyze a redirect after whitespace, ignoring case, colon and link text")
  @Test
  void analyzeRedirectVariant() {
    // GIVEN
    final String text = "\n #redirect: [[Paris|foo]] bar";

    // WHEN
    final PageAnalysis analysis = PageAnalysisTestFactory.createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getRedirect())
        .contains(
            new RedirectElement(
                2,
                26,
                "#redirect",
                new InternalLinkElement(
                    13,
                    26,
                    "Paris",
                    new PageTitle(CommonNamespaces.MAIN.id, "Paris"),
                    null,
                    "foo",
                    false)));
    Assertions.assertThat(analysis.getParagraphs()).containsExactly(new ParagraphElement(27, 30));
  }

  @DisplayName("should analyze the text right after a redirect as the start of a line")
  @Test
  void analyzeListItemAfterRedirect() {
    // GIVEN
    final String text = "#REDIRECT [[Paris]]* item";

    // WHEN
    final PageAnalysis analysis = PageAnalysisTestFactory.createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getRedirect()).isPresent();
    Assertions.assertThat(analysis.getListItems())
        .containsExactly(new ListItemElement(19, 25, "*", "item"));
  }

  @DisplayName("should analyze a redirect to a category or a file as a link")
  @Test
  void analyzeRedirectToCategoryOrFile() {
    // GIVEN
    final String categoryText = "#REDIRECT [[Catégorie:Foo]]";
    final String fileText = "#REDIRECT [[Fichier:X.jpg]]";

    // WHEN
    final PageAnalysis categoryAnalysis = PageAnalysisTestFactory.createPageAnalysis(categoryText);
    final PageAnalysis fileAnalysis = PageAnalysisTestFactory.createPageAnalysis(fileText);

    // THEN
    Assertions.assertThat(categoryAnalysis.getRedirect().map(RedirectElement::link))
        .contains(
            new InternalLinkElement(
                10,
                27,
                "Catégorie:Foo",
                new PageTitle(CommonNamespaces.CATEGORY.id, "Foo"),
                null,
                null,
                false));
    Assertions.assertThat(categoryAnalysis.getCategories()).isEmpty();
    Assertions.assertThat(fileAnalysis.getRedirect()).isPresent();
    Assertions.assertThat(fileAnalysis.getImages()).isEmpty();
    Assertions.assertThat(fileAnalysis.getInternalLinks()).hasSize(1);
  }

  @DisplayName("should analyze a redirect to a language as an interwiki link")
  @Test
  void analyzeRedirectToLanguage() {
    // GIVEN
    final String text = "#REDIRECT [[en:Paris]]";

    // WHEN
    final PageAnalysis analysis = PageAnalysisTestFactory.createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getRedirect().map(RedirectElement::link))
        .contains(new InterwikiLinkElement(10, 22, "en", "Paris", null, null, false));
    Assertions.assertThat(analysis.getLanguageLinks()).isEmpty();
  }

  @DisplayName("should analyze a redirect to a percent-encoded target")
  @Test
  void analyzeRedirectToPercentEncodedTarget() {
    // GIVEN
    final String text = "#REDIRECT [[%3APar]]";

    // WHEN
    final PageAnalysis analysis = PageAnalysisTestFactory.createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getRedirect()).isPresent();
  }

  @DisplayName("should not analyze texts that MediaWiki doesn't treat as redirects")
  @Test
  void analyzeNotRedirects() {
    // GIVEN
    final List<String> texts =
        List.of(
            "x\n#REDIRECT [[Paris]]",
            "#REDIRECT <!-- c -->[[Paris]]",
            "#REDIRECTfoo [[Paris]]",
            "#REDIRECT [[Paris\n]]",
            "#REDIRECT [[#anchor]]",
            "#REDIRECT [[]]",
            "#REDIRECT [[Pa]ris]]",
            "#REDIRECT [[Pa%5Bris]]",
            "#REDIRECT [[{{PAGENAME}}]]");

    // WHEN
    final List<Optional<RedirectElement>> redirects =
        texts.stream()
            .map(text -> PageAnalysisTestFactory.createPageAnalysis(text).getRedirect())
            .toList();

    // THEN
    Assertions.assertThat(redirects).containsOnly(Optional.empty());
  }

  @DisplayName("should analyze a text that is not a redirect as a list item")
  @Test
  void analyzeNotRedirectAsListItem() {
    // GIVEN
    final String text = "#REDIRECT [[#anchor]]";

    // WHEN
    final PageAnalysis analysis = PageAnalysisTestFactory.createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getRedirect()).isEmpty();
    Assertions.assertThat(analysis.getListItems())
        .containsExactly(new ListItemElement(0, 21, "#", "REDIRECT [[#anchor]]"));
  }
}
