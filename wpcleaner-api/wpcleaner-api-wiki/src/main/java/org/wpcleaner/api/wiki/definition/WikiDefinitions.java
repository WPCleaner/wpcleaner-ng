package org.wpcleaner.api.wiki.definition;

/*
 * SPDX-FileCopyrightText: © 2024 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public interface WikiDefinitions {

  default Set<WikiDefinition> getDefinitions() {
    return Arrays.stream(getClass().getDeclaredFields())
        .filter(field -> Modifier.isStatic(field.getModifiers()))
        .map(
            field -> {
              try {
                return field.get(this);
              } catch (IllegalAccessException e) {
                return null;
              }
            })
        .filter(WikiDefinition.class::isInstance)
        .map(WikiDefinition.class::cast)
        .collect(Collectors.toUnmodifiableSet());
  }
}
