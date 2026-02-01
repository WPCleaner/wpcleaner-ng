package org.wpcleaner.api.wiki.definition;

/*
 * SPDX-FileCopyrightText: © 2024 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import org.wpcleaner.api.utils.AutoCatch;

public interface WikiDefinitions {

  default Set<WikiDefinition> getDefinitions() {
    return Arrays.stream(getClass().getDeclaredFields())
        .filter(field -> Modifier.isStatic(field.getModifiers()))
        .map(field -> AutoCatch.runOrNull(() -> field.get(this)))
        .filter(WikiDefinition.class::isInstance)
        .map(WikiDefinition.class::cast)
        .collect(Collectors.toUnmodifiableSet());
  }
}
