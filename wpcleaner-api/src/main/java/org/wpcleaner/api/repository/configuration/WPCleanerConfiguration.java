package org.wpcleaner.api.repository.configuration;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import java.util.Map;
import java.util.Optional;

public record WPCleanerConfiguration(
    Map<String, String> generalProperties, Map<String, String> userProperties) {

  public static final WPCleanerConfiguration EMPTY = new WPCleanerConfiguration(Map.of(), Map.of());

  public WPCleanerConfiguration {
    generalProperties = Map.copyOf(generalProperties);
    userProperties = Map.copyOf(userProperties);
  }

  public Optional<String> getString(final WPCleanerParameter parameter) {
    return parameter.getString(generalProperties, userProperties);
  }

  public List<String> getLines(final WPCleanerParameter parameter) {
    return parameter.getLines(generalProperties, userProperties);
  }
}
