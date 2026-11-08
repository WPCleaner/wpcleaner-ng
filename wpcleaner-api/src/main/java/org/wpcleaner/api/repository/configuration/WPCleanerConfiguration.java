package org.wpcleaner.api.repository.configuration;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

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
    return getString(parameter.isUser(), userProperties, parameter)
        .or(() -> getString(parameter.isGeneral(), generalProperties, parameter));
  }

  private static Optional<String> getString(
      final boolean allowed,
      final Map<String, String> properties,
      final WPCleanerParameter parameter) {
    return Optional.ofNullable(allowed ? properties.get(parameter.value) : null)
        .filter(value -> !value.isEmpty());
  }
}
