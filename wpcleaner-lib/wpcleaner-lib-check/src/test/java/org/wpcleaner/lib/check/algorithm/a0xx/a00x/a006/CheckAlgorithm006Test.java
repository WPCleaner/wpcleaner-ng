package org.wpcleaner.lib.check.algorithm.a0xx.a00x.a006;

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
import org.wpcleaner.api.wiki.definition.WikiDefinition;
import org.wpcleaner.api.wiki.definition.WikipediaDefinitions;
import org.wpcleaner.lib.check.CheckError;
import org.wpcleaner.lib.check.CheckSpringBootTest;
import org.wpcleaner.lib.check.Information;
import org.wpcleaner.lib.check.Replacement;

@CheckSpringBootTest
class CheckAlgorithm006Test {

  private static final int ALGORITHM = 6;

  @Autowired private CheckAlgorithm006 algorithm;

  @DisplayName("should not report a sort key with only authorized characters")
  @Test
  void ignoreAuthorizedCharacters() {
    Assertions.assertThat(
            analyze("{{DEFAULTSORT:Paris, Texas (film) 2}}{{CLEFDETRI:\n Az-:.!?'#/*\t}}"))
        .isEmpty();
  }

  @DisplayName("should offer to replace special characters")
  @Test
  void replaceSpecialCharacters() {
    final List<CheckError> errors = analyze("a {{DEFAULTSORT: Élan Œuvre }} b");

    Assertions.assertThat(errors)
        .containsExactly(
            new CheckError(
                ALGORITHM, 2, 30, List.of(new Replacement("{{DEFAULTSORT:Elan Oeuvre}}", true))));
  }

  @DisplayName("should report each sort key written with an alias of the magic word")
  @Test
  void reportEachSortKey() {
    final List<CheckError> errors = analyze("{{CLEFDETRI:Été}}{{DEFAULTSORT:Abc}}{{CLEFDETRI:Çà}}");

    Assertions.assertThat(errors)
        .containsExactly(
            new CheckError(ALGORITHM, 0, 17, List.of(new Replacement("{{CLEFDETRI:Ete}}", true))),
            new CheckError(ALGORITHM, 36, 52, List.of(new Replacement("{{CLEFDETRI:Ca}}", true))));
  }

  @DisplayName("should list the special characters when none of them can be replaced")
  @Test
  void reportUnknownSpecialCharacters() {
    final List<CheckError> errors = analyze("{{DEFAULTSORT:A_B}}{{DEFAULTSORT:A&B_C&}}");

    Assertions.assertThat(errors)
        .containsExactly(
            new CheckError(
                ALGORITHM, 0, 19, List.of(new Information("Unable to replace the characters [_]"))),
            new CheckError(
                ALGORITHM,
                19,
                41,
                List.of(new Information("Unable to replace the characters [&_]"))));
  }

  @DisplayName("should keep the special characters that can't be replaced")
  @Test
  void keepUnknownSpecialCharacters() {
    final List<CheckError> errors = analyze("{{DEFAULTSORT:É_é&}}");

    Assertions.assertThat(errors)
        .containsExactly(
            new CheckError(
                ALGORITHM, 0, 20, List.of(new Replacement("{{DEFAULTSORT:E_e&}}", true))));
  }

  @DisplayName("should remove invisible characters and the spaces left around the sort key")
  @Test
  void removeInvisibleCharacters() {
    final List<CheckError> errors = analyze("{{DEFAULTSORT:‎ Ab​c ﻿}}");

    Assertions.assertThat(errors)
        .containsExactly(
            new CheckError(
                ALGORITHM, 0, 24, List.of(new Replacement("{{DEFAULTSORT:Abc}}", true))));
  }

  @DisplayName("should ignore templates, functions, parameters and comments in the sort key")
  @Test
  void ignoreElementsInsideSortKey() {
    Assertions.assertThat(
            analyze("{{DEFAULTSORT:{{Modèle|é{{é}}}} {{{é|è}}} {{lc:É}} {{NOMPAGE}}<!-- é -->}}"))
        .isEmpty();
  }

  @DisplayName("should keep templates and comments when replacing special characters")
  @Test
  void keepElementsInsideSortKey() {
    final List<CheckError> errors = analyze("{{DEFAULTSORT:É{{Modèle|é}}é<!-- é -->{{{é}}}}}");

    Assertions.assertThat(errors)
        .extracting(CheckError::replacements)
        .containsExactly(
            List.of(new Replacement("{{DEFAULTSORT:E{{Modèle|é}}e<!-- é -->{{{é}}}}}", true)));
  }

  @DisplayName("should report special characters only in the sort key")
  @Test
  void ignoreOtherFunctions() {
    Assertions.assertThat(analyze("{{lc:É}} {{Modèle|É}} {{{É}}} É")).isEmpty();
  }

  @DisplayName("should report the separator of a second parameter")
  @Test
  void reportSecondParameter() {
    final List<CheckError> errors = analyze("a {{DEFAULTSORT:Abc|noerror|x}}");

    Assertions.assertThat(errors).containsExactly(new CheckError(ALGORITHM, 19, 20, List.of()));
  }

  @DisplayName("should report the special characters rather than the second parameter")
  @Test
  void removeSecondParameter() {
    final List<CheckError> errors = analyze("{{DEFAULTSORT:Ébc|noerror}}");

    Assertions.assertThat(errors)
        .containsExactly(
            new CheckError(
                ALGORITHM, 0, 27, List.of(new Replacement("{{DEFAULTSORT:Ebc}}", true))));
  }

  @DisplayName("should keep the modifier and the spaces before the sort key")
  @Test
  void keepFunctionStart() {
    final List<CheckError> errors = analyze("{{subst: DEFAULTSORT :É}}");

    Assertions.assertThat(errors)
        .extracting(CheckError::replacements)
        .containsExactly(List.of(new Replacement("{{subst: DEFAULTSORT :E}}", true)));
  }

  @DisplayName("should report sort keys in any namespace")
  @Test
  void reportOtherNamespaces() {
    Assertions.assertThat(
            algorithm.analyze(
                PageAnalysisTestFactory.createPageAnalysis(
                    "Utilisateur:Paris", "{{DEFAULTSORT:É}}")))
        .hasSize(1);
  }

  @DisplayName("should use only the general rules on a wiki without specific rules")
  @Test
  void useGeneralRules() {
    final List<CheckError> errors = analyze("{{DEFAULTSORT:Åñά}}");

    Assertions.assertThat(errors)
        .containsExactly(
            new CheckError(
                ALGORITHM, 0, 19, List.of(new Replacement("{{DEFAULTSORT:Anά}}", true))));
  }

  @DisplayName("should authorize the characters of the alphabet of the wiki")
  @Test
  void authorizeLocalCharacters() {
    Assertions.assertThat(analyze(WikipediaDefinitions.SV, "{{DEFAULTSORT:Åäö}}")).isEmpty();
    Assertions.assertThat(analyze(WikipediaDefinitions.EL, "{{DEFAULTSORT:αβ}}")).isEmpty();
    Assertions.assertThat(analyze(WikipediaDefinitions.SV, "{{DEFAULTSORT:Åé}}"))
        .containsExactly(
            new CheckError(ALGORITHM, 0, 18, List.of(new Replacement("{{DEFAULTSORT:Åe}}", true))));
  }

  @DisplayName("should prefer the replacement defined for the wiki")
  @Test
  void useLocalReplacements() {
    Assertions.assertThat(analyze(WikipediaDefinitions.ES, "{{DEFAULTSORT:ñé}}"))
        .containsExactly(
            new CheckError(
                ALGORITHM, 0, 18, List.of(new Replacement("{{DEFAULTSORT:nzze}}", true))));
    Assertions.assertThat(analyze(WikipediaDefinitions.EL, "{{DEFAULTSORT:άβ}}"))
        .containsExactly(
            new CheckError(ALGORITHM, 0, 18, List.of(new Replacement("{{DEFAULTSORT:αβ}}", true))));
  }

  private List<CheckError> analyze(final String text) {
    return algorithm.analyze(PageAnalysisTestFactory.createPageAnalysis("Paris", text));
  }

  private List<CheckError> analyze(final WikiDefinition wiki, final String text) {
    return algorithm.analyze(PageAnalysisTestFactory.createPageAnalysis(wiki, text));
  }
}
