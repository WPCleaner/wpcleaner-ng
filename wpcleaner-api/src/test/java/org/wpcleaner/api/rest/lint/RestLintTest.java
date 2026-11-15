package org.wpcleaner.api.rest.lint;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.client.MockRestServiceServer;
import org.wpcleaner.api.ApiSpringBootTest;
import org.wpcleaner.api.wiki.definition.WikipediaDefinitions;

@ApiSpringBootTest
class RestLintTest {

  private static final String TITLE = "Projet:Correction syntaxique/Traduction";
  private static final String TEXT = "é <b>x <center>y</center>";

  @Autowired private RestLint restLint;
  @Autowired private MockRestLint mockRestLint;
  @Autowired private MockRestServiceServer mediaWikiServer;

  @AfterEach
  void tearDown() {
    mediaWikiServer.reset();
  }

  @DisplayName("RestLint sends the text to the REST API and returns the Linter errors")
  @Test
  void lint() {
    // GIVEN
    mockRestLint
        .givenLint(WikipediaDefinitions.FR, TITLE, TEXT)
        .willReturn(
            List.of(
                Map.of(
                    "type",
                    "missing-end-tag",
                    "dsr",
                    List.of(2, 25, 3, 0),
                    "params",
                    Map.of("name", "b", "inTable", false)),
                Map.of(
                    "type",
                    "bogus-image-options",
                    "dsr",
                    Arrays.asList(6, null, null, null),
                    "params",
                    Map.of("items", List.of("droite")),
                    "templateInfo",
                    Map.of("name", "Modèle:Image")),
                Map.of(
                    "type",
                    "stripped-tag",
                    "dsr",
                    List.of(),
                    "templateInfo",
                    Map.of("multiPartTemplateBlock", true))));

    // WHEN
    final List<LintError> errors = restLint.lint(WikipediaDefinitions.FR, TITLE, TEXT);

    // THEN
    mediaWikiServer.verify();
    Assertions.assertThat(errors)
        .extracting(
            LintError::type,
            LintError::begin,
            LintError::end,
            LintError::parameters,
            LintError::templateInfo)
        .containsExactly(
            Assertions.tuple("missing-end-tag", 2, 25, Map.of("name", "b", "inTable", false), null),
            Assertions.tuple(
                "bogus-image-options",
                6,
                LintError.UNKNOWN_POSITION,
                Map.of("items", List.of("droite")),
                new LintError.TemplateInfo(null, "Modèle:Image")),
            Assertions.tuple(
                "stripped-tag",
                LintError.UNKNOWN_POSITION,
                LintError.UNKNOWN_POSITION,
                Map.of(),
                new LintError.TemplateInfo(true, null)));
  }

  @DisplayName("RestLint returns no error for an empty answer")
  @Test
  void lintWithoutErrors() {
    // GIVEN
    mockRestLint.givenLint(WikipediaDefinitions.FR, TITLE, TEXT).willReturn(List.of());

    // WHEN
    final List<LintError> errors = restLint.lint(WikipediaDefinitions.FR, TITLE, TEXT);

    // THEN
    mediaWikiServer.verify();
    Assertions.assertThat(errors).isEmpty();
  }
}
