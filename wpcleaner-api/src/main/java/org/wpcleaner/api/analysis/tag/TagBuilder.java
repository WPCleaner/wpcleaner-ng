package org.wpcleaner.api.analysis.tag;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

public record TagBuilder(String name, TagKind kind) {

  private static final String SELF_CLOSING_END = " /";

  public static TagBuilder from(final String name) {
    return new TagBuilder(name, TagKind.START);
  }

  public TagBuilder withKind(final TagKind newKind) {
    return new TagBuilder(name, newKind);
  }

  public String build() {
    final StringBuilder builder = new StringBuilder().append(TagElement.TOKEN_START);
    if (kind == TagKind.END) {
      builder.append(TagElement.TOKEN_SLASH);
    }
    builder.append(name);
    if (kind == TagKind.SELF_CLOSING) {
      builder.append(SELF_CLOSING_END);
    }
    return builder.append(TagElement.TOKEN_END).toString();
  }
}
