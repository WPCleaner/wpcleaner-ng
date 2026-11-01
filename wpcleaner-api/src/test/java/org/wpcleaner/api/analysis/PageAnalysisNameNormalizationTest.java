package org.wpcleaner.api.analysis;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.wpcleaner.api.analysis.category.CategoryElement;
import org.wpcleaner.api.analysis.image.ImageElement;
import org.wpcleaner.api.analysis.internallink.InternalLinkElement;
import org.wpcleaner.api.analysis.pagetitle.PageTitle;
import org.wpcleaner.api.analysis.template.TemplateElement;

class PageAnalysisNameNormalizationTest {

  @DisplayName("should normalize template names in the template namespace")
  @Test
  void normalizeTemplateNames() {
    // GIVEN
    final String text = "{{cite_web}}{{ Modèle : infobox  Pays }}{{template:X}}{{X#section}}";

    // WHEN
    final PageAnalysis analysis = PageAnalysisTestFactory.createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getTemplates())
        .map(TemplateElement::title)
        .containsExactly(
            new PageTitle(10, "Cite web"),
            new PageTitle(10, "Infobox Pays"),
            new PageTitle(10, "X"),
            new PageTitle(10, "X"));
  }

  @DisplayName("should normalize transclusions of pages in other namespaces")
  @Test
  void normalizeTransclusionsInOtherNamespaces() {
    // GIVEN
    final String text = "{{:main page}}{{Utilisatrice:Bob/sub}}{{Foo:bar}}";

    // WHEN
    final PageAnalysis analysis = PageAnalysisTestFactory.createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getTemplates())
        .map(TemplateElement::title)
        .containsExactly(
            new PageTitle(0, "Main page"),
            new PageTitle(2, "Bob/sub"),
            new PageTitle(10, "Foo:bar"));
  }

  @DisplayName("should not analyze template with an empty normalized name")
  @Test
  void normalizeEmptyTemplateName() {
    // GIVEN
    final String text = "{{_}}";

    // WHEN
    final PageAnalysis analysis = PageAnalysisTestFactory.createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getTemplates()).isEmpty();
  }

  @DisplayName("should normalize image names in the file namespace")
  @Test
  void normalizeImageNames() {
    // GIVEN
    final String text = "[[file:my_picture.jpg]][[Image:x  y.png|thumb]]";

    // WHEN
    final PageAnalysis analysis = PageAnalysisTestFactory.createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getImages())
        .map(ImageElement::title)
        .containsExactly(new PageTitle(6, "My picture.jpg"), new PageTitle(6, "X y.png"));
  }

  @DisplayName("should normalize internal link titles")
  @Test
  void normalizeInternalLinkTitles() {
    // GIVEN
    final String text =
        "[[paris_(ville)#Histoire|Paris]] [[:catégorie:Villes]] [[#Section]] [[Utilisateur:bob]]";

    // WHEN
    final PageAnalysis analysis = PageAnalysisTestFactory.createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getInternalLinks())
        .map(InternalLinkElement::title)
        .containsExactly(
            new PageTitle(0, "Paris (ville)"),
            new PageTitle(14, "Villes"),
            null,
            new PageTitle(2, "Bob"));
  }

  @DisplayName("should normalize category titles")
  @Test
  void normalizeCategoryTitles() {
    // GIVEN
    final String text = "[[Catégorie:villes_de  France|Paris]]";

    // WHEN
    final PageAnalysis analysis = PageAnalysisTestFactory.createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getCategories())
        .map(CategoryElement::title)
        .containsExactly(new PageTitle(14, "Villes de France"));
  }

  @DisplayName("should decode character references in titles")
  @Test
  void normalizeTitlesWithCharacterReferences() {
    // GIVEN
    final String text =
        "[[World War&nbsp;I]] [[Caf&eacute;]] [[A&#38;B]] [[&#x4E2D;&#25991;]] [[Fake&unknown;x]]"
            + " {{Mod&egrave;le:X}}";

    // WHEN
    final PageAnalysis analysis = PageAnalysisTestFactory.createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getInternalLinks())
        .extracting(InternalLinkElement::target, InternalLinkElement::title)
        .containsExactly(
            Assertions.tuple("World War&nbsp;I", new PageTitle(0, "World War I")),
            Assertions.tuple("Caf&eacute;", new PageTitle(0, "Café")),
            Assertions.tuple("A&#38;B", new PageTitle(0, "A&B")),
            Assertions.tuple("&#x4E2D;&#25991;", new PageTitle(0, "中文")),
            Assertions.tuple("Fake&unknown;x", new PageTitle(0, "Fake&unknown;x")));
    Assertions.assertThat(analysis.getTemplates())
        .map(TemplateElement::title)
        .containsExactly(new PageTitle(10, "X"));
  }

  @DisplayName("should not split anchor inside a numeric character reference")
  @Test
  void normalizeTitleWithReferenceAndAnchor() {
    // GIVEN
    final String text = "[[A&#38;B#Section]]";

    // WHEN
    final PageAnalysis analysis = PageAnalysisTestFactory.createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getInternalLinks())
        .extracting(
            InternalLinkElement::target, InternalLinkElement::anchor, InternalLinkElement::title)
        .containsExactly(Assertions.tuple("A&#38;B", "Section", new PageTitle(0, "A&B")));
  }

  @DisplayName("should use title case for the first letter like MediaWiki")
  @Test
  void normalizeTitleFirstLetterWithTitleCase() {
    // GIVEN
    final String text = "[[ტბეთი]] [[ǆungla]] [[ßtraße]] [[𐐨x]] [[élan]]";

    // WHEN
    final PageAnalysis analysis = PageAnalysisTestFactory.createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getInternalLinks())
        .map(InternalLinkElement::title)
        .containsExactly(
            new PageTitle(0, "ტბეთი"),
            new PageTitle(0, "ǅungla"),
            new PageTitle(0, "ßtraße"),
            new PageTitle(0, "𐐀x"),
            new PageTitle(0, "Élan"));
  }
}
