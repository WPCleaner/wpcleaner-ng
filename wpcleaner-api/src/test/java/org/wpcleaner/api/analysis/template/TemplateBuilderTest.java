package org.wpcleaner.api.analysis.template;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.LinkedHashMap;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.wpcleaner.api.analysis.PageAnalysisTestFactory;

class TemplateBuilderTest {

  private static final String TEMPLATE = "Historique";
  private static final String PAGE = "Paris";
  private static final String TEXT = "historique";

  @DisplayName("should build a template without parameters")
  @Test
  void buildWithoutParameters() {
    // GIVEN
    final TemplateBuilder builder = TemplateBuilder.from("'");

    // WHEN
    final String template = builder.build();

    // THEN
    Assertions.assertThat(template).isEqualTo("{{'}}");
    assertParameters(template, builder);
  }

  @DisplayName("should write the positional parameters without their names")
  @Test
  void buildWithPositionalParameters() {
    // GIVEN
    final TemplateBuilder builder =
        TemplateBuilder.from(TEMPLATE).withParameter("1", PAGE).withParameter("2", TEXT);

    // WHEN
    final String template = builder.build();

    // THEN
    Assertions.assertThat(template).isEqualTo("{{Historique|Paris|historique}}");
    assertParameters(template, builder);
  }

  @DisplayName("should write the named parameters with their names")
  @Test
  void buildWithNamedParameters() {
    // GIVEN
    final TemplateBuilder builder =
        TemplateBuilder.from(TEMPLATE).withParameter("page", PAGE).withParameter("texte", TEXT);

    // WHEN
    final String template = builder.build();

    // THEN
    Assertions.assertThat(template).isEqualTo("{{Historique|page=Paris|texte=historique}}");
    assertParameters(template, builder);
  }

  @DisplayName("should name a positional parameter when it is not the next unnamed one")
  @Test
  void buildWithNamedPositionalParameters() {
    // GIVEN
    final TemplateBuilder afterNamed =
        TemplateBuilder.from(TEMPLATE).withParameter("page", PAGE).withParameter("2", TEXT);
    final TemplateBuilder afterEqual =
        TemplateBuilder.from(TEMPLATE).withParameter("1", "A=b").withParameter("2", "c");
    final TemplateBuilder reversed =
        TemplateBuilder.from(TEMPLATE).withParameter("2", TEXT).withParameter("1", PAGE);

    // WHEN
    final String afterNamedTemplate = afterNamed.build();
    final String afterEqualTemplate = afterEqual.build();
    final String reversedTemplate = reversed.build();

    // THEN
    Assertions.assertThat(afterNamedTemplate).isEqualTo("{{Historique|page=Paris|2=historique}}");
    Assertions.assertThat(afterEqualTemplate).isEqualTo("{{Historique|1=A=b|2=c}}");
    Assertions.assertThat(reversedTemplate).isEqualTo("{{Historique|2=historique|Paris}}");
    assertParameters(afterNamedTemplate, afterNamed);
    assertParameters(afterEqualTemplate, afterEqual);
    assertParameters(reversedTemplate, reversed);
  }

  private static void assertParameters(final String wikitext, final TemplateBuilder builder) {
    final Map<String, String> expected = new LinkedHashMap<>();
    builder.parameters().forEach(parameter -> expected.put(parameter.name(), parameter.value()));
    Assertions.assertThat(PageAnalysisTestFactory.createPageAnalysis(wikitext).getTemplates())
        .singleElement()
        .satisfies(
            template -> {
              Assertions.assertThat(template.name()).isEqualTo(builder.name());
              Assertions.assertThat(effectiveParameters(template)).isEqualTo(expected);
            });
  }

  private static Map<String, String> effectiveParameters(final TemplateElement template) {
    final Map<String, String> parameters = new LinkedHashMap<>();
    int unnamedCount = 0;
    for (final TemplateParameter parameter : template.parameters()) {
      final String name = parameter.name();
      if (name == null) {
        unnamedCount++;
        parameters.put(Integer.toString(unnamedCount), parameter.value());
      } else {
        parameters.put(name.trim(), parameter.value());
      }
    }
    return parameters;
  }
}
