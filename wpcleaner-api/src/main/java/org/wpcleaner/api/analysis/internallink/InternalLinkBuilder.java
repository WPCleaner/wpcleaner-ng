package org.wpcleaner.api.analysis.internallink;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import org.jspecify.annotations.Nullable;

public record InternalLinkBuilder(
    String target,
    @Nullable String anchor,
    @Nullable String text,
    boolean colon,
    boolean linkTrail) {

  private static final String LINK_START = "[[";
  private static final String LINK_END = "]]";
  private static final String COLON = ":";
  private static final char ANCHOR = '#';
  private static final char PIPE = '|';
  private static final char UNDERSCORE = '_';
  private static final char SPACE = ' ';

  public static InternalLinkBuilder to(final String target) {
    return new InternalLinkBuilder(target, null, null, false, true);
  }

  public InternalLinkBuilder withAnchor(@Nullable final String newAnchor) {
    return new InternalLinkBuilder(target, newAnchor, text, colon, linkTrail);
  }

  public InternalLinkBuilder withText(@Nullable final String newText) {
    return new InternalLinkBuilder(target, anchor, newText, colon, linkTrail);
  }

  public InternalLinkBuilder withColon(final boolean newColon) {
    return new InternalLinkBuilder(target, anchor, text, newColon, linkTrail);
  }

  public InternalLinkBuilder withoutLinkTrail() {
    return new InternalLinkBuilder(target, anchor, text, colon, false);
  }

  public String build() {
    final String fullTarget = anchor == null ? target : target + ANCHOR + anchor;
    final String start = LINK_START + (colon ? COLON : "");
    if (text == null) {
      return start + fullTarget + LINK_END;
    }
    if (!colon && text.length() >= fullTarget.length()) {
      final String prefix = text.substring(0, fullTarget.length());
      final String trail = text.substring(fullTarget.length());
      if (isSameTitle(fullTarget, prefix) && isLinkTrail(trail)) {
        return LINK_START + prefix + LINK_END + trail;
      }
    }
    return start + fullTarget + PIPE + text + LINK_END;
  }

  private boolean isLinkTrail(final String trail) {
    return trail.isEmpty() || (linkTrail && trail.chars().allMatch(Character::isLetter));
  }

  private static boolean isSameTitle(final String title, final String otherTitle) {
    return !title.isEmpty()
        && Character.toUpperCase(title.charAt(0)) == Character.toUpperCase(otherTitle.charAt(0))
        && title
            .substring(1)
            .replace(UNDERSCORE, SPACE)
            .equals(otherTitle.substring(1).replace(UNDERSCORE, SPACE));
  }
}
