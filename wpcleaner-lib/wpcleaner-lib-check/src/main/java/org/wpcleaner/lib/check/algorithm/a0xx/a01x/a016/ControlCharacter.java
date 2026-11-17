package org.wpcleaner.lib.check.algorithm.a0xx.a01x.a016;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.wpcleaner.lib.check.algorithm.HtmlCharacter;

enum ControlCharacter {
  DELETE(0x007F, 0x007F, Removal.REMOVABLE, Safety.UNSAFE),
  NON_BREAKING_SPACE(0x00A0, 0x00A0, Removal.KEPT_EXCEPT_NEAR_PUNCTUATION, Safety.SAFE),
  SOFT_HYPHEN(0x00AD, 0x00AD, Removal.REMOVABLE, Safety.UNSAFE),
  THREE_PER_EM_SPACE(0x2004, 0x2004, Removal.KEPT_EXCEPT_NEAR_PUNCTUATION, Safety.UNSAFE),
  FOUR_PER_EM_SPACE(0x2005, 0x2005, Removal.KEPT_EXCEPT_NEAR_PUNCTUATION, Safety.UNSAFE),
  SIX_PER_EM_SPACE(0x2006, 0x2006, Removal.KEPT_EXCEPT_NEAR_PUNCTUATION, Safety.UNSAFE),
  FIGURE_SPACE(0x2007, 0x2007, Removal.KEPT_EXCEPT_NEAR_PUNCTUATION, Safety.UNSAFE),
  PUNCTUATION_SPACE(0x2008, 0x2008, Removal.KEPT, Safety.UNSAFE),
  ZERO_WIDTH_SPACE(0x200B, 0x200B, Removal.REMOVABLE, Safety.SAFE_IN_REDIRECT),
  LEFT_TO_RIGHT_MARK(0x200E, 0x200E, Removal.REMOVABLE, Safety.SAFE_IN_REDIRECT),
  RIGHT_TO_LEFT_MARK(0x200F, 0x200F, Removal.REMOVABLE, Safety.SAFE_IN_REDIRECT),
  LINE_SEPARATOR(0x2028, 0x2028, Removal.REMOVABLE, Safety.UNSAFE),
  LEFT_TO_RIGHT_EMBEDDING(0x202A, 0x202A, Removal.REMOVABLE, Safety.UNSAFE),
  RIGHT_TO_LEFT_EMBEDDING(0x202B, 0x202B, Removal.REMOVABLE, Safety.UNSAFE),
  POP_DIRECTIONAL_FORMATTING(0x202C, 0x202C, Removal.REMOVABLE, Safety.UNSAFE),
  LEFT_TO_RIGHT_OVERRIDE(0x202D, 0x202D, Removal.REMOVABLE, Safety.UNSAFE),
  RIGHT_TO_LEFT_OVERRIDE(0x202E, 0x202E, Removal.REMOVABLE, Safety.UNSAFE),
  BYTE_ORDER_MARK(0xFEFF, 0xFEFF, Removal.REMOVABLE, Safety.UNSAFE),
  OBJECT_REPLACEMENT_CHARACTER(0xFFFC, 0xFFFC, Removal.REMOVABLE, Safety.UNSAFE),
  PRIVATE_USE_AREA(0xE000, 0xF8FF, Removal.KEPT, Safety.UNSAFE),
  PRIVATE_USE_AREA_A(0xF0000, 0xFFFFD, Removal.KEPT, Safety.UNSAFE),
  PRIVATE_USE_AREA_B(0x100000, 0x10FFFD, Removal.KEPT, Safety.UNSAFE);

  private static final int CHECK_MARK_SYMBOL = 0xF0FC;
  private static final List<String> CHECK_MARK_ALTERNATIVES = List.of("*", "✓");

  private final int first;
  private final int last;
  private final Removal removal;
  private final Safety safety;

  ControlCharacter(final int first, final int last, final Removal removal, final Safety safety) {
    this.first = first;
    this.last = last;
    this.removal = removal;
    this.safety = safety;
  }

  static Optional<ControlCharacter> of(final int codePoint) {
    return Arrays.stream(values())
        .filter(control -> control.first <= codePoint && codePoint <= control.last)
        .findFirst();
  }

  boolean isRemovable() {
    return removal == Removal.REMOVABLE;
  }

  boolean isReplacedNearPunctuation() {
    return removal == Removal.KEPT_EXCEPT_NEAR_PUNCTUATION;
  }

  boolean isSafe() {
    return safety == Safety.SAFE;
  }

  boolean isSafeInRedirect() {
    return safety == Safety.SAFE_IN_REDIRECT;
  }

  List<String> alternatives(final int codePoint) {
    if (codePoint == CHECK_MARK_SYMBOL) {
      return CHECK_MARK_ALTERNATIVES;
    }
    return switch (this) {
      case NON_BREAKING_SPACE -> List.of(" ", "&nbsp;");
      case SOFT_HYPHEN -> List.of(entity(HtmlCharacter.SYMBOL_SOFT_HYPHEN), "-", "");
      case THREE_PER_EM_SPACE,
          FOUR_PER_EM_SPACE,
          SIX_PER_EM_SPACE,
          FIGURE_SPACE,
          PUNCTUATION_SPACE ->
          List.of(" ", "");
      default -> List.of();
    };
  }

  private static String entity(final HtmlCharacter character) {
    return "&" + character.getEntityName() + ";";
  }

  private enum Removal {
    REMOVABLE,
    KEPT_EXCEPT_NEAR_PUNCTUATION,
    KEPT
  }

  private enum Safety {
    SAFE,
    SAFE_IN_REDIRECT,
    UNSAFE
  }
}
