package org.wpcleaner.api.analysis;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.wpcleaner.api.analysis.image.ImageElement;
import org.wpcleaner.api.analysis.image.ImageParameter;
import org.wpcleaner.api.analysis.internallink.InternalLinkElement;
import org.wpcleaner.api.analysis.pagetitle.PageTitle;
import org.wpcleaner.api.analysis.template.TemplateElement;
import org.wpcleaner.api.analysis.template.TemplateParameter;

class PageAnalysisImageTest {

  @DisplayName("should analyze image without parameters")
  @Test
  void analyzeImage() {
    // GIVEN
    final String text = "[[ Fichier : Exemple.jpg ]]";

    // WHEN
    final PageAnalysis analysis = PageAnalysisTestFactory.createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getImages())
        .containsExactly(
            new ImageElement(
                0,
                text.length(),
                "Fichier",
                "Exemple.jpg",
                new PageTitle(6, "Exemple.jpg"),
                List.of()));
    Assertions.assertThat(analysis.getInternalLinks()).isEmpty();
  }

  @DisplayName("should analyze image with a namespace alias")
  @Test
  void analyzeImageWithNamespaceAlias() {
    // GIVEN
    final String text = "[[Image:Exemple.jpg]]";

    // WHEN
    final PageAnalysis analysis = PageAnalysisTestFactory.createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getImages())
        .containsExactly(
            new ImageElement(
                0,
                text.length(),
                "Image",
                "Exemple.jpg",
                new PageTitle(6, "Exemple.jpg"),
                List.of()));
  }

  @DisplayName("should analyze image options and caption")
  @Test
  void analyzeImageParameters() {
    // GIVEN
    final String text = "[[File:X.jpg|thumb|200px|upright=1.2|alt=Texte|Une légende]]";

    // WHEN
    final PageAnalysis analysis = PageAnalysisTestFactory.createPageAnalysis(text);

    // THEN
    final ImageParameter caption = new ImageParameter(47, 58, "Une légende", null, null);
    Assertions.assertThat(analysis.getImages())
        .containsExactly(
            new ImageElement(
                0,
                text.length(),
                "File",
                "X.jpg",
                new PageTitle(6, "X.jpg"),
                List.of(
                    new ImageParameter(13, 18, "thumb", "img_thumbnail", null),
                    new ImageParameter(19, 24, "200px", "img_width", "200"),
                    new ImageParameter(25, 36, "upright=1.2", "img_upright", "1.2"),
                    new ImageParameter(37, 46, "alt=Texte", "img_alt", "Texte"),
                    caption)));
    Assertions.assertThat(analysis.getImages().getFirst().findCaption()).contains(caption);
    Assertions.assertThat(analysis.getImages().getFirst().findParameter("img_alt"))
        .map(ImageParameter::argument)
        .contains("Texte");
  }

  @DisplayName("should use the last unknown parameter as caption")
  @Test
  void analyzeImageCaptionIsLastUnknownParameter() {
    // GIVEN
    final String text = "[[File:X.jpg|first|thumb|second|Thumb]]";

    // WHEN
    final PageAnalysis analysis = PageAnalysisTestFactory.createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getImages().getFirst().findCaption())
        .map(ImageParameter::value)
        .contains("Thumb");
  }

  @DisplayName("should not recognize an option with an invalid argument")
  @Test
  void analyzeImageOptionWithInvalidArgument() {
    // GIVEN
    final String text = "[[File:X.jpg|upright=abc|bigpx]]";

    // WHEN
    final PageAnalysis analysis = PageAnalysisTestFactory.createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getImages().getFirst().parameters())
        .containsExactly(
            new ImageParameter(13, 24, "upright=abc", null, null),
            new ImageParameter(25, 30, "bigpx", null, null));
  }

  @DisplayName("should analyze links inside the caption")
  @Test
  void analyzeImageWithInternalLinkInCaption() {
    // GIVEN
    final String text = "[[File:X.jpg|vignette|Voir [[Paris|la ville]]]]";

    // WHEN
    final PageAnalysis analysis = PageAnalysisTestFactory.createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getImages())
        .containsExactly(
            new ImageElement(
                0,
                text.length(),
                "File",
                "X.jpg",
                new PageTitle(6, "X.jpg"),
                List.of(
                    new ImageParameter(13, 21, "vignette", "img_thumbnail", null),
                    new ImageParameter(22, 45, "Voir [[Paris|la ville]]", null, null))));
    Assertions.assertThat(analysis.getInternalLinks())
        .containsExactly(
            new InternalLinkElement(
                27, 45, "Paris", new PageTitle(0, "Paris"), null, "la ville", false));
  }

  @DisplayName("should keep the closing bracket of an external link inside the caption")
  @Test
  void analyzeImageWithExternalLinkInCaption() {
    // GIVEN
    final String text = "[[File:X.jpg|[http://a.org b]]]";

    // WHEN
    final PageAnalysis analysis = PageAnalysisTestFactory.createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getImages())
        .containsExactly(
            new ImageElement(
                0,
                text.length(),
                "File",
                "X.jpg",
                new PageTitle(6, "X.jpg"),
                List.of(new ImageParameter(13, 29, "[http://a.org b]", null, null))));
  }

  @DisplayName("should not split parameters at a pipe inside a template")
  @Test
  void analyzeImageWithTemplateInCaption() {
    // GIVEN
    final String text = "[[File:X.jpg|thumb|{{T|a}}]]";

    // WHEN
    final PageAnalysis analysis = PageAnalysisTestFactory.createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getImages().getFirst().parameters())
        .containsExactly(
            new ImageParameter(13, 18, "thumb", "img_thumbnail", null),
            new ImageParameter(19, 26, "{{T|a}}", null, null));
    Assertions.assertThat(analysis.getTemplates())
        .containsExactly(
            new TemplateElement(
                19,
                26,
                "T",
                new PageTitle(10, "T"),
                List.of(new TemplateParameter(23, 24, null, "a")),
                List.of()));
  }

  @DisplayName("should not split parameters at a pipe inside a comment")
  @Test
  void analyzeImageWithPipeInComment() {
    // GIVEN
    final String text = "[[File:X.jpg|a<!-- | -->b]]";

    // WHEN
    final PageAnalysis analysis = PageAnalysisTestFactory.createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getImages().getFirst().parameters())
        .containsExactly(new ImageParameter(13, 25, "a<!-- | -->b", null, null));
  }

  @DisplayName("should analyze image inside a template parameter")
  @Test
  void analyzeImageInsideTemplate() {
    // GIVEN
    final String text = "{{T|[[File:X.jpg|thumb]]}}";

    // WHEN
    final PageAnalysis analysis = PageAnalysisTestFactory.createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getImages())
        .containsExactly(
            new ImageElement(
                4,
                24,
                "File",
                "X.jpg",
                new PageTitle(6, "X.jpg"),
                List.of(new ImageParameter(17, 22, "thumb", "img_thumbnail", null))));
  }

  @DisplayName("should not analyze unclosed image")
  @Test
  void analyzeUnclosedImage() {
    // GIVEN
    final String text = "[[File:X.jpg|thumb|caption";

    // WHEN
    final PageAnalysis analysis = PageAnalysisTestFactory.createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getImages()).isEmpty();
  }

  @DisplayName("should analyze link to a file page as internal link")
  @Test
  void analyzeLinkToFilePage() {
    // GIVEN
    final String text = "[[:File:X.jpg]]";

    // WHEN
    final PageAnalysis analysis = PageAnalysisTestFactory.createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getImages()).isEmpty();
    Assertions.assertThat(analysis.getInternalLinks())
        .containsExactly(
            new InternalLinkElement(
                0, text.length(), "File:X.jpg", new PageTitle(6, "X.jpg"), null, null, true));
  }
}
