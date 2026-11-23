package org.wpcleaner.lib.check.algorithm.a0xx.a02x.a027;

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
class CheckAlgorithm027Test {

  private static final int ALGORITHM = 27;

  @Autowired private CheckAlgorithm027 algorithm;

  @DisplayName("should replace automatically decimal and hexadecimal entities of HTML characters")
  @Test
  void replaceHtmlCharacters() {
    // GIVEN
    final String text = "Caf&#233; &#x2014; &#X00E8; &#000000000233;";

    // WHEN
    final List<CheckError> errors = analyze("Paris", text);

    // THEN
    Assertions.assertThat(errors)
        .containsExactly(
            error(text, "&#233;", "é", true),
            error(text, "&#x2014;", "—", true),
            error(text, "&#X00E8;", "è", true),
            error(text, "&#000000000233;", "é", true));
  }

  @DisplayName("should replace automatically ASCII letters and digits only")
  @Test
  void replaceAsciiCharacters() {
    // GIVEN
    final String text = "&#65; &#x37; &#42; &#34; &#33;";

    // WHEN
    final List<CheckError> errors = analyze("Paris", text);

    // THEN
    Assertions.assertThat(errors)
        .containsExactly(
            error(text, "&#65;", "A", true),
            error(text, "&#x37;", "7", true),
            error(text, "&#42;", "*", false),
            error(text, "&#34;", "\"", false),
            error(text, "&#33;", "!", false));
  }

  @DisplayName("should suggest other characters without automatic replacement")
  @Test
  void replaceOtherCharacters() {
    // GIVEN
    final String text = "&#x17D; &#128512; &#x416;";

    // WHEN
    final List<CheckError> errors = analyze("Paris", text);

    // THEN
    Assertions.assertThat(errors)
        .containsExactly(
            error(text, "&#x17D;", "Ž", false),
            error(text, "&#128512;", "😀", false),
            error(text, "&#x416;", "Ж", false));
  }

  @DisplayName("should read entities between 128 and 159 as Windows-1252 characters")
  @Test
  void replaceWindows1252Characters() {
    // GIVEN
    final String text = "&#150; &#x80; &#142; &#129;";

    // WHEN
    final List<CheckError> errors = analyze("Paris", text);

    // THEN
    Assertions.assertThat(errors)
        .containsExactly(
            error(text, "&#150;", "–", true),
            error(text, "&#x80;", "€", true),
            error(text, "&#142;", "Ž", false));
  }

  @DisplayName("should not report characters with a wiki or HTML meaning")
  @Test
  void ignoreKeptCharacters() {
    // GIVEN
    final String text =
        "&#38; &#39; &#58; &#59; &#60; &#61; &#62; &#91; &#93; &#123; &#124; &#125; &#126; &#8209; &#x7C;";

    // WHEN
    final List<CheckError> errors = analyze("Paris", text);

    // THEN
    Assertions.assertThat(errors).isEmpty();
  }

  @DisplayName("should not report invisible characters")
  @Test
  void ignoreInvisibleCharacters() {
    // GIVEN
    final String text =
        "&#32; &#160; &#8201; &#8239; &#173; &#8206; &#8204; &#9; &#xE000; &#x2028; &#x2029; &#x378;";

    // WHEN
    final List<CheckError> errors = analyze("Paris", text);

    // THEN
    Assertions.assertThat(errors).isEmpty();
  }

  @DisplayName("should not report invalid or incomplete entities")
  @Test
  void ignoreInvalidEntities() {
    // GIVEN
    final String text = "&#xD800; &#x110000; &#12345678901; &#; &#x; &233; &#233 &#x2G; &x41; &#a;";

    // WHEN
    final List<CheckError> errors = analyze("Paris", text);

    // THEN
    Assertions.assertThat(errors).isEmpty();
  }

  @DisplayName("should not report entities in comments and non wiki content")
  @Test
  void ignoreNonWikiContent() {
    // GIVEN
    final String text =
        "<!-- &#233; --> <nowiki>&#233;</nowiki> <pre>&#233;</pre> <math>&#233;</math> <source lang=\"x\">&#233;</source> <syntaxhighlight lang=\"x\">&#233;</syntaxhighlight>";

    // WHEN
    final List<CheckError> errors = analyze("Paris", text);

    // THEN
    Assertions.assertThat(errors).isEmpty();
  }

  @DisplayName("should report entities in all namespaces")
  @Test
  void reportAllNamespaces() {
    // GIVEN
    final String text = "&#233;";

    // WHEN
    final List<CheckError> fileErrors = analyze("Fichier:Paris.jpg", text);
    final List<CheckError> talkErrors = analyze("Discussion utilisateur:Paris", text);

    // THEN
    Assertions.assertThat(fileErrors).hasSize(1);
    Assertions.assertThat(talkErrors).hasSize(1);
  }

  private List<CheckError> analyze(final String title, final String text) {
    return algorithm.analyze(PageAnalysisTestFactory.createPageAnalysis(title, text));
  }

  private static CheckError error(
      final String text, final String found, final String replacement, final boolean automatic) {
    final int begin = text.indexOf(found);
    return new CheckError(
        ALGORITHM, begin, begin + found.length(), List.of(new Replacement(replacement, automatic)));
  }
}
