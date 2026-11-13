package org.wpcleaner.api.analysis.internallink;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.wpcleaner.api.analysis.PageAnalysis;
import org.wpcleaner.api.analysis.PageAnalysisTestFactory;

class InternalLinkBuilderTest {

  private static final String PARIS = "Paris";
  private static final String CATEGORY = "Catégorie:Lyon";

  @DisplayName("should build a link with its text")
  @Test
  void buildWithText() {
    // GIVEN
    final InternalLinkBuilder builder = InternalLinkBuilder.to(PARIS);

    // WHEN
    final String withoutText = builder.build();
    final String withText = builder.withText("la capitale").build();
    final String withEmptyText = builder.withText("").build();

    // THEN
    Assertions.assertThat(withoutText).isEqualTo("[[Paris]]");
    Assertions.assertThat(withText).isEqualTo("[[Paris|la capitale]]");
    Assertions.assertThat(withEmptyText).isEqualTo("[[Paris|]]");
    assertLink(withoutText, PARIS, null, PARIS);
    assertLink(withText, PARIS, null, "la capitale");
  }

  @DisplayName("should drop the text when it is the target")
  @Test
  void simplifySameText() {
    // GIVEN
    final InternalLinkBuilder builder = InternalLinkBuilder.to("Tour Eiffel");

    // WHEN
    final String sameText = builder.withText("Tour Eiffel").build();
    final String lowerCase = builder.withText("tour Eiffel").build();
    final String underscore = builder.withText("Tour_Eiffel").build();
    final String otherCase = builder.withText("Tour eiffel").build();
    final String singleCharacter = InternalLinkBuilder.to("A").withText("a").build();

    // THEN
    Assertions.assertThat(sameText).isEqualTo("[[Tour Eiffel]]");
    Assertions.assertThat(lowerCase).isEqualTo("[[tour Eiffel]]");
    Assertions.assertThat(underscore).isEqualTo("[[Tour_Eiffel]]");
    Assertions.assertThat(otherCase).isEqualTo("[[Tour Eiffel|Tour eiffel]]");
    Assertions.assertThat(singleCharacter).isEqualTo("[[a]]");
    assertLink(lowerCase, "Tour Eiffel", null, "tour Eiffel");
  }

  @DisplayName("should use the link trail when the text continues with letters")
  @Test
  void useLinkTrail() {
    // GIVEN
    final InternalLinkBuilder builder = InternalLinkBuilder.to(PARIS);

    // WHEN
    final String trail = builder.withText("Parisiens").build();
    final String otherCharacters = builder.withText("Paris 2024").build();
    final String digits = builder.withText("Paris2").build();
    final String withoutTrail = builder.withText("Parisiens").withoutLinkTrail().build();
    final String sameWithoutTrail = builder.withText(PARIS).withoutLinkTrail().build();

    // THEN
    Assertions.assertThat(trail).isEqualTo("[[Paris]]iens");
    Assertions.assertThat(otherCharacters).isEqualTo("[[Paris|Paris 2024]]");
    Assertions.assertThat(digits).isEqualTo("[[Paris|Paris2]]");
    Assertions.assertThat(withoutTrail).isEqualTo("[[Paris|Parisiens]]");
    Assertions.assertThat(sameWithoutTrail).isEqualTo("[[Paris]]");
    assertLink(trail, PARIS, null, "Parisiens");
  }

  @DisplayName("should add the anchor to the target")
  @Test
  void buildWithAnchor() {
    // GIVEN
    final InternalLinkBuilder builder = InternalLinkBuilder.to(PARIS).withAnchor("Histoire");

    // WHEN
    final String withoutText = builder.build();
    final String withText = builder.withText("Histoire").build();
    final String sameText = builder.withText("Paris#Histoire").build();
    final String anchorOnly =
        InternalLinkBuilder.to("").withAnchor("Notes").withText("notes").build();

    // THEN
    Assertions.assertThat(withoutText).isEqualTo("[[Paris#Histoire]]");
    Assertions.assertThat(withText).isEqualTo("[[Paris#Histoire|Histoire]]");
    Assertions.assertThat(sameText).isEqualTo("[[Paris#Histoire]]");
    Assertions.assertThat(anchorOnly).isEqualTo("[[#Notes|notes]]");
    assertLink(withText, PARIS, "Histoire", "Histoire");
  }

  @DisplayName("should add a colon before the target and keep the text")
  @Test
  void buildWithColon() {
    // GIVEN
    final InternalLinkBuilder builder = InternalLinkBuilder.to(CATEGORY).withColon(true);

    // WHEN
    final String withoutText = builder.build();
    final String sameText = builder.withText(CATEGORY).build();

    // THEN
    Assertions.assertThat(withoutText).isEqualTo("[[:Catégorie:Lyon]]");
    Assertions.assertThat(sameText).isEqualTo("[[:Catégorie:Lyon|Catégorie:Lyon]]");
    assertLink(withoutText, "Lyon", null, CATEGORY);
  }

  private static void assertLink(
      final String wikitext, final String title, final String anchor, final String displayedText) {
    final PageAnalysis analysis = PageAnalysisTestFactory.createPageAnalysis(wikitext);
    Assertions.assertThat(analysis.getInternalLinks())
        .singleElement()
        .satisfies(
            link -> {
              Assertions.assertThat(link.title()).isNotNull();
              Assertions.assertThat(link.title().text()).isEqualTo(title);
              Assertions.assertThat(link.anchor()).isEqualTo(anchor);
              Assertions.assertThat(link.displayedText() + wikitext.substring(link.end()))
                  .isEqualTo(displayedText);
            });
  }
}
