package org.wpcleaner.api.analysis;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.wpcleaner.api.analysis.category.CategoryElement;
import org.wpcleaner.api.analysis.externallink.ExternalLinkElement;
import org.wpcleaner.api.analysis.image.ImageElement;
import org.wpcleaner.api.analysis.internallink.InternalLinkElement;
import org.wpcleaner.api.analysis.languagelink.LanguageLinkElement;
import org.wpcleaner.api.analysis.pagetitle.PageTitle;

class PageAnalysisLinkCommentTest {

  @DisplayName("should remove comments from internal link target but keep them in the text")
  @Test
  void analyzeInternalLinkWithComments() {
    // GIVEN
    final String text =
        "[[Beethoven's violin sonatas (disambiguation)<!-- intentional -->]]"
            + " [[Paris<!-- c -->|ville<!-- d -->]] [[A<!-- | -->B]]";

    // WHEN
    final PageAnalysis analysis = PageAnalysisTestFactory.createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getInternalLinks())
        .extracting(
            InternalLinkElement::target, InternalLinkElement::title, InternalLinkElement::text)
        .containsExactly(
            Assertions.tuple(
                "Beethoven's violin sonatas (disambiguation)",
                new PageTitle(0, "Beethoven's violin sonatas (disambiguation)"),
                null),
            Assertions.tuple("Paris", new PageTitle(0, "Paris"), "ville<!-- d -->"),
            Assertions.tuple("AB", new PageTitle(0, "AB"), null));
  }

  @DisplayName("should remove comments from category, language link, image and external link")
  @Test
  void analyzeOtherLinksWithComments() {
    // GIVEN
    final String text =
        "[[Catégorie:<!-- x -->Villes]] [[en:<!-- y -->Paris]]"
            + " [[Fichier:A<!-- z -->.jpg|thumb]] [http://a.org/<!-- u -->b texte]";

    // WHEN
    final PageAnalysis analysis = PageAnalysisTestFactory.createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getCategories())
        .extracting(CategoryElement::name, CategoryElement::title)
        .containsExactly(Assertions.tuple("Villes", new PageTitle(14, "Villes")));
    Assertions.assertThat(analysis.getLanguageLinks())
        .extracting(LanguageLinkElement::language, LanguageLinkElement::target)
        .containsExactly(Assertions.tuple("en", "Paris"));
    Assertions.assertThat(analysis.getImages())
        .extracting(ImageElement::name, ImageElement::title)
        .containsExactly(Assertions.tuple("A.jpg", new PageTitle(6, "A.jpg")));
    Assertions.assertThat(analysis.getExternalLinks())
        .extracting(ExternalLinkElement::target, ExternalLinkElement::text)
        .containsExactly(Assertions.tuple("http://a.org/b", "texte"));
  }
}
