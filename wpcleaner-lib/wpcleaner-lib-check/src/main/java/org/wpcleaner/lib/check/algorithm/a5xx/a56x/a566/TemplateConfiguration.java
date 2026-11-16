package org.wpcleaner.lib.check.algorithm.a5xx.a56x.a566;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import java.util.Optional;
import org.jspecify.annotations.Nullable;
import org.wpcleaner.api.analysis.template.TemplateBuilder;
import org.wpcleaner.lib.check.configuration.CheckAlgorithmConfiguration;
import org.wpcleaner.lib.check.configuration.TemplateDefinition;

record TemplateConfiguration(
    String templateName, String paramAbbreviation, String paramMeaning, String defaultMeaning) {

  private static final String PARAMETER_TEMPLATES = "templates";
  private static final String FIELD_ABBREVIATION = "abbreviation";
  private static final String FIELD_MEANING = "meaning";
  private static final String FIELD_DEFAULT_MEANING = "default_meaning";

  static List<TemplateConfiguration> of(final CheckAlgorithmConfiguration configuration) {
    return configuration
        .getTemplateDefinitions(
            PARAMETER_TEMPLATES, List.of(FIELD_ABBREVIATION, FIELD_MEANING, FIELD_DEFAULT_MEANING))
        .stream()
        .flatMap(definition -> of(definition).stream())
        .toList();
  }

  private static Optional<TemplateConfiguration> of(final TemplateDefinition definition) {
    return definition
        .parameter(FIELD_ABBREVIATION)
        .flatMap(
            paramAbbreviation ->
                definition
                    .parameter(FIELD_MEANING)
                    .map(
                        paramMeaning ->
                            new TemplateConfiguration(
                                definition.template(),
                                paramAbbreviation,
                                paramMeaning,
                                definition.parameter(FIELD_DEFAULT_MEANING).orElse(""))));
  }

  String getReplacement(final String abbreviation, @Nullable final String title) {
    return TemplateBuilder.from(templateName)
        .withParameter(paramAbbreviation, abbreviation)
        .withParameter(paramMeaning, Optional.ofNullable(title).orElse(defaultMeaning).trim())
        .build();
  }
}
