package org.wpcleaner.api.analysis.tag;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class TagTypeRepository {

  private static final Map<String, TagType> TYPES_BY_STANDARDIZED_NAME =
      Stream.<TagType>concat(
              Arrays.stream(HtmlTagType.values()), Arrays.stream(WikiTagType.values()))
          .collect(Collectors.toUnmodifiableMap(TagType::getNormalizedName, Function.identity()));

  private TagTypeRepository() {
    // Utility class
  }

  public static Optional<TagType> findTagType(final String normalizedName) {
    return Optional.ofNullable(TYPES_BY_STANDARDIZED_NAME.get(normalizedName));
  }
}
