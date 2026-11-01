package org.wpcleaner.api.analysis;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.wpcleaner.api.analysis.image.ImageElement;
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
}
