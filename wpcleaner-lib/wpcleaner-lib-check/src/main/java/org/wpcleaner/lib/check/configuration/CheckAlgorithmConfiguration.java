package org.wpcleaner.lib.check.configuration;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.Map;
import java.util.Optional;

public record CheckAlgorithmConfiguration(int number, Map<String, String> properties) {

  private static final String PRIORITY = "prio";
  private static final String SHORT_DESCRIPTION = "head";
  private static final int FIRST_ACTIVE_PRIORITY = 1;
  private static final int LAST_ACTIVE_PRIORITY = 4;

  public CheckAlgorithmConfiguration {
    properties = Map.copyOf(properties);
  }

  public boolean isActive() {
    return getProperty(PRIORITY)
        .filter(priority -> priority.chars().allMatch(Character::isDigit))
        .map(Integer::parseInt)
        .filter(priority -> FIRST_ACTIVE_PRIORITY <= priority && priority <= LAST_ACTIVE_PRIORITY)
        .isPresent();
  }

  public Optional<String> shortDescription() {
    return getProperty(SHORT_DESCRIPTION);
  }

  private Optional<String> getProperty(final String name) {
    return Optional.ofNullable(properties.get(name)).filter(value -> !value.isEmpty());
  }
}
