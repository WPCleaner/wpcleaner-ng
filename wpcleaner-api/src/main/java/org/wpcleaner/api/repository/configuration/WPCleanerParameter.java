package org.wpcleaner.api.repository.configuration;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public enum WPCleanerParameter {
  APOSTROPHE_TEMPLATE("general_apostrophe_template", true, true, false),
  CHECK_WIKI_COMMENT("check_wiki_comment", true, false, false),
  CHECK_WIKI_PROJECT_PAGE("check_wiki_project_page", true, false, false),
  CHECK_WIKI_TRANSLATION_PAGE("check_wiki_translation_page", true, false, false),
  TEMPLATE_GROUPS("general_template_groups", true, true, true);

  public final String value;
  private final boolean general;
  private final boolean user;
  private final boolean combined;

  WPCleanerParameter(
      final String value, final boolean general, final boolean user, final boolean combined) {
    this.value = value;
    this.general = general;
    this.user = user;
    this.combined = combined;
  }

  Optional<String> getString(
      final Map<String, String> generalProperties, final Map<String, String> userProperties) {
    return getValue(user, userProperties).or(() -> getValue(general, generalProperties));
  }

  List<String> getLines(
      final Map<String, String> generalProperties, final Map<String, String> userProperties) {
    if (!combined) {
      return getString(generalProperties, userProperties)
          .map(ConfigurationParser::toLines)
          .orElse(List.of());
    }
    return Stream.of(getValue(general, generalProperties), getValue(user, userProperties))
        .flatMap(Optional::stream)
        .flatMap(propertyValue -> ConfigurationParser.toLines(propertyValue).stream())
        .toList();
  }

  private Optional<String> getValue(final boolean allowed, final Map<String, String> properties) {
    return Optional.ofNullable(allowed ? properties.get(value) : null)
        .filter(propertyValue -> !propertyValue.isEmpty());
  }
}
