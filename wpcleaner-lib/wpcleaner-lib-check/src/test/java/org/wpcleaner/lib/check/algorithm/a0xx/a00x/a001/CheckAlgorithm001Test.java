package org.wpcleaner.lib.check.algorithm.a0xx.a00x.a001;

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
class CheckAlgorithm001Test {

  private static final int ALGORITHM = 1;

  @Autowired private CheckAlgorithm001 algorithm;

  @DisplayName("should automatically remove the template namespace from template names")
  @Test
  void removeTemplateNamespace() {
    // GIVEN
    final String first = "{{Modèle:Infobox}}";
    final String second = "{{Template:Ébauche|x}}";
    final String third = "{{ modèle : lien |a}}";
    final String fourth = "{{subst:Modèle:Date}}";
    final String text = String.join(" ", first, second, third, fourth);

    // WHEN
    final List<CheckError> errors = analyze("Paris", text);

    // THEN
    Assertions.assertThat(errors)
        .containsExactly(
            error(text.indexOf(first), first, "{{Infobox}}", "Modèle", true),
            error(text.indexOf(second), second, "{{Ébauche|x}}", "Template", true),
            error(text.indexOf(third), third, "{{ lien |a}}", "modèle", true),
            error(text.indexOf(fourth), fourth, "{{subst:Date}}", "Modèle", true));
  }

  @DisplayName("should fix manually outside the main namespace")
  @Test
  void fixManuallyOutsideMainNamespace() {
    // GIVEN
    final String text = "{{Modèle:Infobox}}";

    // WHEN
    final List<CheckError> errors = analyze("Utilisateur:Paris", text);

    // THEN
    Assertions.assertThat(errors).containsExactly(error(0, text, "{{Infobox}}", "Modèle", false));
  }

  @DisplayName("should not report templates without namespace, or whose name is a magic word")
  @Test
  void ignoreOtherTemplates() {
    // GIVEN
    final String text =
        "{{Mod<!-- -->èle:Infobox}} {{Infobox}} {{:Modèle:Infobox}} {{Utilisateur:Paris}}"
            + " {{Modèle:PAGENAME}} {{Modèle:CURRENTYEAR}} {{Modèle:lc:Paris}}"
            + " {{Modèle:#si:a|b}} {{Modèle:lc:{{Infobox}}}}";

    // WHEN
    final List<CheckError> errors = analyze("Paris", text);

    // THEN
    Assertions.assertThat(errors).isEmpty();
  }

  private List<CheckError> analyze(final String title, final String text) {
    return algorithm.analyze(PageAnalysisTestFactory.createPageAnalysis(title, text));
  }

  private static CheckError error(
      final int begin,
      final String template,
      final String replacement,
      final String namespace,
      final boolean automatic) {
    return new CheckError(
        ALGORITHM,
        begin,
        begin + template.length(),
        List.of(
            new Replacement(
                replacement, "Remove " + namespace + " namespace from template name", automatic)));
  }
}
