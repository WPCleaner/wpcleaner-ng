package org.wpcleaner.lib.check.algorithm.a0xx.a02x.a020;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.wpcleaner.api.analysis.PageAnalysisTestFactory;
import org.wpcleaner.lib.check.CheckError;
import org.wpcleaner.lib.check.CheckSpringBootTest;
import org.wpcleaner.lib.check.Replacement;

@CheckSpringBootTest
class CheckAlgorithm020Test {

  private static final int ALGORITHM = 20;

  @Autowired private CheckAlgorithm020 algorithm;

  @DisplayName("should suggest the character for the entity, without automatic replacement")
  @Test
  void replaceEntity() {
    // GIVEN
    final String text = "Mort &dagger; en 1900";

    // WHEN
    final List<CheckError> errors = analyze(text);

    // THEN
    final int begin = text.indexOf('&');
    Assertions.assertThat(errors)
        .containsExactly(
            new CheckError(
                ALGORITHM,
                begin,
                begin + "&dagger;".length(),
                List.of(new Replacement("†", false))));
  }

  @DisplayName("should not report other entities, nor the entity without semicolon")
  @Test
  void ignoreOtherEntities() {
    // GIVEN
    final String text = "&Dagger; &dagger &dagger ; &eacute; &daggers; & dagger;";

    // WHEN
    final List<CheckError> errors = analyze(text);

    // THEN
    Assertions.assertThat(errors).isEmpty();
  }

  @DisplayName("should not report entities in comments and source code")
  @Test
  void ignoreCommentsAndSourceCode() {
    // GIVEN
    final String text =
        "<!-- &dagger; --> <source>&dagger;</source>"
            + " <syntaxhighlight lang=\"html\">&dagger;</syntaxhighlight>";

    // WHEN
    final List<CheckError> errors = analyze(text);

    // THEN
    Assertions.assertThat(errors).isEmpty();
  }

  @DisplayName("should report entities in tags without wiki content other than source code")
  @Test
  void reportInNowiki() {
    // GIVEN
    final String text = "<nowiki>&dagger;</nowiki> <pre>&dagger;</pre>";

    // WHEN
    final List<CheckError> errors = analyze(text);

    // THEN
    Assertions.assertThat(errors)
        .extracting(CheckError::begin)
        .containsExactly(text.indexOf('&'), text.lastIndexOf('&'));
  }

  @DisplayName("should not report entities in the URL of external links")
  @Test
  void ignoreUrls() {
    // GIVEN
    final String text =
        "[http://example.org/?a=1&dagger;b Texte] [http://example.org/?a=1&dagger;b]"
            + " http://example.org/?a=1&dagger;b";

    // WHEN
    final List<CheckError> errors = analyze(text);

    // THEN
    Assertions.assertThat(errors).isEmpty();
  }

  @DisplayName("should report entities in the text of external links")
  @Test
  void reportInExternalLinkText() {
    // GIVEN
    final String text = "[http://example.org/?a=1&b=2 &dagger; Texte &dagger;]";

    // WHEN
    final List<CheckError> errors = analyze(text);

    // THEN
    Assertions.assertThat(errors)
        .extracting(CheckError::begin)
        .containsExactly(text.indexOf("&dagger;"), text.lastIndexOf("&dagger;"));
  }

  private List<CheckError> analyze(final String text) {
    return algorithm.analyze(PageAnalysisTestFactory.createPageAnalysis("Lyon", text));
  }
}
