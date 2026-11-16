package org.wpcleaner.lib.check.algorithm.a5xx.a56x.a566;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.jspecify.annotations.Nullable;
import org.wpcleaner.lib.check.Replacement;
import org.wpcleaner.lib.check.configuration.CheckAlgorithmConfiguration;

record ReplacementConfiguration(
    Map<String, Replacement> replacementsByValue,
    Map<String, Map<String, Replacement>> replacementsByValueAndTitle) {

  private static final String PARAMETER_REPLACEMENTS = "replacements";
  private static final String PARAMETER_REPLACEMENTS_TITLE = "replacements_title";
  private static final String FIELD_VALUE = "value";
  private static final String FIELD_TITLE = "title";
  private static final String FIELD_AUTOMATIC = "automatic";
  private static final String FIELD_REPLACEMENT = "replacement";
  private static final List<String> REPLACEMENTS_FIELDS =
      List.of(FIELD_VALUE, FIELD_AUTOMATIC, FIELD_REPLACEMENT);
  private static final List<String> REPLACEMENTS_TITLE_FIELDS =
      List.of(FIELD_VALUE, FIELD_TITLE, FIELD_AUTOMATIC, FIELD_REPLACEMENT);

  ReplacementConfiguration {
    replacementsByValue = Map.copyOf(replacementsByValue);
    replacementsByValueAndTitle = Map.copyOf(replacementsByValueAndTitle);
  }

  static ReplacementConfiguration of(final CheckAlgorithmConfiguration configuration) {
    final Map<String, Replacement> replacementsByValue = new HashMap<>();
    configuration.getFieldValues(PARAMETER_REPLACEMENTS, REPLACEMENTS_FIELDS).stream()
        .filter(fieldValues -> isComplete(fieldValues, REPLACEMENTS_FIELDS))
        .forEach(
            fieldValues ->
                replacementsByValue.put(
                    fieldValues.getOrDefault(FIELD_VALUE, ""), toReplacement(fieldValues)));
    final Map<String, Map<String, Replacement>> replacementsByValueAndTitle = new HashMap<>();
    configuration.getFieldValues(PARAMETER_REPLACEMENTS_TITLE, REPLACEMENTS_TITLE_FIELDS).stream()
        .filter(fieldValues -> isComplete(fieldValues, REPLACEMENTS_TITLE_FIELDS))
        .forEach(
            fieldValues ->
                replacementsByValueAndTitle
                    .computeIfAbsent(
                        fieldValues.getOrDefault(FIELD_VALUE, ""), _ -> new HashMap<>())
                    .put(fieldValues.getOrDefault(FIELD_TITLE, ""), toReplacement(fieldValues)));
    return new ReplacementConfiguration(replacementsByValue, replacementsByValueAndTitle);
  }

  Optional<Replacement> getReplacement(final String abbreviation, @Nullable final String title) {
    final boolean withTitle = title != null && !title.isEmpty();
    final Optional<Replacement> replacementByTitle =
        Optional.ofNullable(title)
            .filter(_ -> withTitle)
            .map(String::trim)
            .flatMap(
                trimmedTitle ->
                    Optional.ofNullable(replacementsByValueAndTitle.get(abbreviation))
                        .map(replacements -> replacements.get(trimmedTitle)));
    return replacementByTitle.or(
        () ->
            Optional.ofNullable(replacementsByValue.get(abbreviation))
                .map(
                    replacement ->
                        new Replacement(
                            replacement.text(), replacement.automatic() && !withTitle)));
  }

  private static boolean isComplete(
      final Map<String, String> fieldValues, final List<String> fields) {
    return fieldValues.keySet().containsAll(fields)
        && !fieldValues.getOrDefault(FIELD_VALUE, "").isEmpty();
  }

  private static Replacement toReplacement(final Map<String, String> fieldValues) {
    return new Replacement(
        fieldValues.getOrDefault(FIELD_REPLACEMENT, ""),
        Boolean.parseBoolean(fieldValues.getOrDefault(FIELD_AUTOMATIC, "")));
  }
}
