package org.wpcleaner.api.analysis;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.wpcleaner.api.analysis.magiclink.MagicLinkElement;
import org.wpcleaner.api.repository.magiclink.MagicLinkType;

class PageAnalysisMagicLinkTest {

  @DisplayName("should analyze ISBN and PMID magic links")
  @Test
  void analyzeMagicLinks() {
    // GIVEN
    final String text = "Voir ISBN 978-2-07-036822-8 et PMID 12345.";

    // WHEN
    final PageAnalysis analysis = PageAnalysisTestFactory.createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getMagicLinks())
        .containsExactly(
            new MagicLinkElement(5, 27, MagicLinkType.ISBN, "978-2-07-036822-8"),
            new MagicLinkElement(31, 41, MagicLinkType.PMID, "12345"));
  }

  @DisplayName("should analyze magic link with a non-breaking space inside a template")
  @Test
  void analyzeMagicLinkWithNonBreakingSpace() {
    // GIVEN
    final String text = "{{T|ISBN&nbsp;123456789X}}";

    // WHEN
    final PageAnalysis analysis = PageAnalysisTestFactory.createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getMagicLinks())
        .containsExactly(new MagicLinkElement(4, 24, MagicLinkType.ISBN, "123456789X"));
  }

  @DisplayName("should not analyze disabled, invalid or glued magic links")
  @Test
  void analyzeInvalidMagicLinks() {
    // GIVEN
    final String text = "RFC 1234 ISBN 123 ISBN1234567890 xISBN 1234567890 ISBN 12345678901";

    // WHEN
    final PageAnalysis analysis = PageAnalysisTestFactory.createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getMagicLinks()).isEmpty();
  }

  @DisplayName("should not analyze magic links inside links, comments or non-wiki tags")
  @Test
  void analyzeMagicLinksInsideOtherElements() {
    // GIVEN
    final String text =
        "[[ISBN 1234567890]] [http://a.org ISBN 1234567890] <!-- ISBN 1234567890 -->"
            + " <nowiki>ISBN 1234567890</nowiki>";

    // WHEN
    final PageAnalysis analysis = PageAnalysisTestFactory.createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getMagicLinks()).isEmpty();
  }
}
