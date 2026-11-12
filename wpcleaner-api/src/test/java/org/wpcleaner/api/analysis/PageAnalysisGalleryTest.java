package org.wpcleaner.api.analysis;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.wpcleaner.api.analysis.externallink.ExternalLinkElement;
import org.wpcleaner.api.analysis.gallery.GalleryElement;
import org.wpcleaner.api.analysis.gallery.GalleryImage;
import org.wpcleaner.api.analysis.image.ImageParameter;
import org.wpcleaner.api.analysis.internallink.InternalLinkElement;
import org.wpcleaner.api.analysis.pagetitle.PageTitle;
import org.wpcleaner.api.analysis.tag.TagAttribute;

class PageAnalysisGalleryTest {

  @DisplayName("should analyze gallery images with their options and caption")
  @Test
  void analyzeGallery() {
    // GIVEN
    final String text =
        """
        <gallery mode=packed>
        Fichier:A b.jpg|Légende [[Paris|ville]]
        Image:c_d.png|alt=Texte|200px|Autre

        E.jpg
        </gallery>""";

    // WHEN
    final PageAnalysis analysis = PageAnalysisTestFactory.createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getGalleries()).hasSize(1);
    final GalleryElement gallery = analysis.getGalleries().getFirst();
    Assertions.assertThat(gallery.begin()).isZero();
    Assertions.assertThat(gallery.end()).isEqualTo(text.length());
    Assertions.assertThat(gallery.tag().startTag().attributes())
        .containsExactly(new TagAttribute("mode", "packed", 14));
    Assertions.assertThat(gallery.images())
        .containsExactly(
            new GalleryImage(
                22,
                61,
                "Fichier:A b.jpg",
                new PageTitle(6, "A b.jpg"),
                List.of(new ImageParameter(38, 61, "Légende [[Paris|ville]]", null, null))),
            new GalleryImage(
                62,
                97,
                "Image:c_d.png",
                new PageTitle(6, "C d.png"),
                List.of(
                    new ImageParameter(76, 85, "alt=Texte", "img_alt", "Texte"),
                    new ImageParameter(86, 91, "200px", "img_width", "200"),
                    new ImageParameter(92, 97, "Autre", null, null))),
            new GalleryImage(99, 104, "E.jpg", new PageTitle(6, "E.jpg"), List.of()));
    Assertions.assertThat(gallery.images().get(1).findCaption())
        .map(ImageParameter::value)
        .contains("Autre");
    Assertions.assertThat(analysis.getInternalLinks())
        .map(InternalLinkElement::title)
        .containsExactly(new PageTitle(0, "Paris"));
  }

  @DisplayName("should analyze invalid gallery lines and skip lines without file")
  @Test
  void analyzeGalleryWithInvalidLines() {
    // GIVEN
    final String text =
        """
        <gallery>
        File:X.jpg<!-- c -->|thumb|a{{T|b}}c
        <!-- only comment -->
        |no name
        File:%C3%89t%C3%A9.jpg
        </gallery>""";

    // WHEN
    final PageAnalysis analysis = PageAnalysisTestFactory.createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getGalleries().getFirst().images())
        .containsExactly(
            new GalleryImage(
                10,
                46,
                "File:X.jpg<!-- c -->",
                null,
                List.of(
                    new ImageParameter(31, 36, "thumb", null, null),
                    new ImageParameter(37, 46, "a{{T|b}}c", null, null))),
            new GalleryImage(
                78, 100, "File:%C3%89t%C3%A9.jpg", new PageTitle(6, "Été.jpg"), List.of()));
  }

  @DisplayName("should analyze links in the gallery caption only")
  @Test
  void analyzeLinksInGalleryCaption() {
    // GIVEN
    final String text =
        """
        <gallery class="[[Nantes]]" Caption="Vu par [[Camille Pissarro|Pissarro]] à [[Paris]]">
        A.jpg
        </gallery>
        <gallery caption='[[Lyon [https://example.org Ex' >
        B.jpg|]]
        </gallery>
        <gallery caption>
        C.jpg
        </gallery>
        <div caption="[[Rennes]]"></div>""";

    // WHEN
    final PageAnalysis analysis = PageAnalysisTestFactory.createPageAnalysis(text);

    // THEN
    final int pissarro = text.indexOf("[[Camille");
    final int paris = text.indexOf("[[Paris");
    Assertions.assertThat(analysis.getInternalLinks())
        .containsExactly(
            new InternalLinkElement(
                pissarro,
                text.indexOf("]]", pissarro) + 2,
                "Camille Pissarro",
                new PageTitle(0, "Camille Pissarro"),
                null,
                "Pissarro",
                false),
            new InternalLinkElement(
                paris,
                paris + "[[Paris]]".length(),
                "Paris",
                new PageTitle(0, "Paris"),
                null,
                null,
                false));
    Assertions.assertThat(analysis.getExternalLinks())
        .map(ExternalLinkElement::bracketed)
        .containsExactly(false);
  }
}
