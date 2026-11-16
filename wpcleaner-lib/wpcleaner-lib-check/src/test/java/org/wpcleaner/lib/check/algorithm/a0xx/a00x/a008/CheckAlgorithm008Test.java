package org.wpcleaner.lib.check.algorithm.a0xx.a00x.a008;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.wpcleaner.api.analysis.PageAnalysisTestFactory;
import org.wpcleaner.lib.check.CheckError;
import org.wpcleaner.lib.check.CheckSpringBootTest;
import org.wpcleaner.lib.check.Replacement;

@CheckSpringBootTest
class CheckAlgorithm008Test {

  private static final int ALGORITHM = 8;

  @Autowired private CheckAlgorithm008 algorithm;

  @DisplayName("should not report titles ending with an equal sign")
  @ParameterizedTest
  @ValueSource(
      strings = {
        "== a ==\nb",
        "a\n=== b ===  \nc",
        "== a == <!-- c -->\nb",
        "== a == <!-- c\nd -->\nb",
        "== a = b ==",
        "==== a ==",
        "a\n=====\nb"
      })
  void ignoreTitles(final String text) {
    Assertions.assertThat(analyze(text)).isEmpty();
  }

  @DisplayName("should not report equal signs elsewhere than at the beginning of a line")
  @Test
  void ignoreEqualsInsideLine() {
    Assertions.assertThat(analyze("a = b\n c == d\n{{T|e=f}}")).isEmpty();
  }

  @DisplayName("should offer to end the title")
  @Test
  void endTitle() {
    Assertions.assertThat(analyze("a\n== b\nc"))
        .containsExactly(
            new CheckError(ALGORITHM, 2, 6, List.of(new Replacement("== b ==", false))));
  }

  @DisplayName("should report the first line and the last line")
  @Test
  void reportFirstAndLastLines() {
    Assertions.assertThat(analyze("==a\nb\n===c"))
        .containsExactly(
            new CheckError(ALGORITHM, 0, 3, List.of(new Replacement("== a ==", false))),
            new CheckError(ALGORITHM, 6, 10, List.of(new Replacement("=== c ===", false))));
  }

  @DisplayName("should offer to remove a line made only of equal signs")
  @Test
  void removeEqualsAlone() {
    Assertions.assertThat(analyze("a\n==\nb"))
        .containsExactly(new CheckError(ALGORITHM, 2, 4, List.of(new Replacement("", false))));
  }

  @DisplayName("should keep whitespace and comments at the end of the line outside the title")
  @ParameterizedTest
  @ValueSource(strings = {"== a \t\nb", "== a <!-- c -->\nb", "== a <!-- c --> <!-- d\ne -->\nb"})
  void keepTrailingWhitespaceAndComments(final String text) {
    Assertions.assertThat(analyze(text))
        .containsExactly(
            new CheckError(ALGORITHM, 0, 4, List.of(new Replacement("== a ==", false))));
  }

  @DisplayName("should keep a comment inside the title when text follows it")
  @Test
  void keepCommentInsideTitle() {
    Assertions.assertThat(analyze("== a <!-- c --> b"))
        .containsExactly(
            new CheckError(
                ALGORITHM, 0, 17, List.of(new Replacement("== a <!-- c --> b ==", false))));
  }

  @DisplayName("should offer to split or merge when text follows a closed title")
  @Test
  void textAfterClosedTitle() {
    Assertions.assertThat(analyze("== a ==  b"))
        .containsExactly(
            new CheckError(
                ALGORITHM,
                0,
                10,
                List.of(
                    new Replacement("== a ==\nb", false), new Replacement("== a b ==", false))));
  }

  @DisplayName("should treat a longer second run of equal signs as closing the title")
  @Test
  void textAfterLongerSecondRun() {
    Assertions.assertThat(analyze("== a === b").getFirst().replacements())
        .containsExactly(new Replacement("== a ==\nb", false), new Replacement("== a b ==", false));
  }

  @DisplayName("should offer each title level when the second run of equal signs is shorter")
  @Test
  void textAfterShorterSecondRun() {
    Assertions.assertThat(analyze("== a = b"))
        .containsExactly(
            new CheckError(
                ALGORITHM,
                0,
                8,
                List.of(
                    new Replacement("== a = b ==", false),
                    new Replacement("== a ==", false),
                    new Replacement("= a =", false),
                    new Replacement("== a ==\nb", false))));
  }

  @DisplayName("should offer a title without text when there is none before the second run")
  @Test
  void emptyTitleBeforeSecondRun() {
    Assertions.assertThat(analyze("== == b").getFirst().replacements())
        .containsExactly(new Replacement("== ==\nb", false), new Replacement("== b ==", false));
  }

  @DisplayName("should not report lines inside comments")
  @Test
  void ignoreComments() {
    Assertions.assertThat(analyze("<!--\n== a\n-->\n<!-- b\n== c")).isEmpty();
  }

  @DisplayName("should not report lines inside tags with content that is not a title")
  @ParameterizedTest
  @ValueSource(
      strings = {
        "ce",
        "chem",
        "code",
        "math",
        "nowiki",
        "pre",
        "score",
        "source",
        "syntaxhighlight"
      })
  void ignoreTags(final String tagName) {
    Assertions.assertThat(analyze("<" + tagName + ">\n== a\n</" + tagName + ">")).isEmpty();
  }

  @DisplayName("should report lines inside other tags")
  @Test
  void reportOtherTags() {
    Assertions.assertThat(analyze("<div>\n== a\n</div>")).hasSize(1);
  }

  @DisplayName("should report without replacement a line inside a template, function or parameter")
  @ParameterizedTest
  @ValueSource(
      strings = {"{{T|a\n=b|c}}", "{{#if:|a\n=b|c}}", "{{{1|a\n=b|c}}}", "{{T|a\n=b|c\n}}"})
  void reportInsideArgumentWithoutReplacement(final String text) {
    final int begin = text.indexOf("=b");
    final int newLine = text.indexOf('\n', begin);
    final int end = newLine < 0 ? text.length() : newLine;
    Assertions.assertThat(analyze(text))
        .containsExactly(new CheckError(ALGORITHM, begin, end, List.of()));
  }

  @DisplayName("should offer replacements for a line after a template")
  @Test
  void offerReplacementsAfterTemplate() {
    Assertions.assertThat(analyze("{{T|a}}\n== b").getFirst().replacements())
        .containsExactly(new Replacement("== b ==", false));
  }

  @DisplayName("should not report a line ending with an equal sign inside a template")
  @Test
  void ignoreClosedLineInsideTemplate() {
    Assertions.assertThat(analyze("{{T|a\n= b =\n}}")).isEmpty();
  }

  @DisplayName("should report titles in any namespace")
  @Test
  void reportOtherNamespaces() {
    Assertions.assertThat(
            algorithm.analyze(
                PageAnalysisTestFactory.createPageAnalysis("Utilisateur:Paris", "== a")))
        .hasSize(1);
  }

  private List<CheckError> analyze(final String text) {
    return algorithm.analyze(PageAnalysisTestFactory.createPageAnalysis("Paris", text));
  }
}
