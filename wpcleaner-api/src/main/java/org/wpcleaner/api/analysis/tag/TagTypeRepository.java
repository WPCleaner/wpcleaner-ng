package org.wpcleaner.api.analysis.tag;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.HashMap;
import java.util.Map;

public final class TagTypeRepository {

  private static final Map<String, TagType> TYPES_BY_STANDARDIZED_NAME = new HashMap<>();

  private TagTypeRepository() {
    // Utility class
  }

  static void registerTagType(final TagType tagType) {
    if (TYPES_BY_STANDARDIZED_NAME.containsKey(tagType.getNormalizedName())) {
      throw new IllegalStateException(
          "Duplicate tag type %s".formatted(tagType.getNormalizedName()));
    }
    TYPES_BY_STANDARDIZED_NAME.put(tagType.getNormalizedName(), tagType);
  }
}
