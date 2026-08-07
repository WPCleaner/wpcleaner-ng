package org.wpcleaner.application.gui.core.style;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class StylePropertiesRegistry {

  public static final StyleProperties DEFAULT =
      StyleProperties.builder().withEnabled(false).build();

  private final Map<String, StyleProperties> defaultStyles;

  public StylePropertiesRegistry(final List<StylePropertiesInitializer> initializers) {
    this.defaultStyles =
        initializers.stream()
            .map(StylePropertiesInitializer::getDefaultStyles)
            .flatMap(map -> map.entrySet().stream())
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  public StyleProperties getStyle(final String name) {
    return defaultStyles.getOrDefault(name, DEFAULT);
  }
}
