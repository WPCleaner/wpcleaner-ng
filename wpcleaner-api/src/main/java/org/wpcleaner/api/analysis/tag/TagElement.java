package org.wpcleaner.api.analysis.tag;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.wpcleaner.api.analysis.Element;

public record TagElement(
    int begin, int end, String name, TagKind kind, List<TagAttribute> attributes)
    implements Element {

  public static final char TOKEN_START = '<';
  public static final char TOKEN_END = '>';
  public static final char TOKEN_ATTRIBUTE_VALUE = '=';
  public static final char TOKEN_SLASH = '/';

  public TagElement {
    attributes = List.copyOf(attributes);
  }

  public String normalizedName() {
    return name.toLowerCase(Locale.ROOT);
  }

  public Optional<TagType> type() {
    return TagTypeRepository.findTagType(normalizedName());
  }

  public boolean hasWikiContent() {
    return type().map(TagType::hasWikiContent).orElse(true);
  }
}
