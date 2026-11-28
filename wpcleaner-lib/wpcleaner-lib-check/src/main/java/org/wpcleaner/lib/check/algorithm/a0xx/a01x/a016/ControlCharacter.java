package org.wpcleaner.lib.check.algorithm.a0xx.a01x.a016;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import org.wpcleaner.api.utils.GT;
import org.wpcleaner.lib.check.algorithm.HtmlCharacter;

@SuppressWarnings("ImmutableEnumChecker")
enum ControlCharacter {
  DELETE(0x007F, 0x007F, Removal.REMOVABLE, Safety.UNSAFE, () -> GT._T("Delete character")),
  NON_BREAKING_SPACE(
      0x00A0,
      0x00A0,
      Removal.KEPT_EXCEPT_NEAR_PUNCTUATION,
      Safety.SAFE,
      () -> GT._T("Non-breaking space")),
  SOFT_HYPHEN(0x00AD, 0x00AD, Removal.REMOVABLE, Safety.UNSAFE, () -> GT._T("Soft hyphen")),
  THREE_PER_EM_SPACE(
      0x2004,
      0x2004,
      Removal.KEPT_EXCEPT_NEAR_PUNCTUATION,
      Safety.UNSAFE,
      () -> GT._T("Three-per-em space")),
  FOUR_PER_EM_SPACE(
      0x2005,
      0x2005,
      Removal.KEPT_EXCEPT_NEAR_PUNCTUATION,
      Safety.UNSAFE,
      () -> GT._T("Four-per-em space")),
  SIX_PER_EM_SPACE(
      0x2006,
      0x2006,
      Removal.KEPT_EXCEPT_NEAR_PUNCTUATION,
      Safety.UNSAFE,
      () -> GT._T("Six-per-em space")),
  FIGURE_SPACE(
      0x2007,
      0x2007,
      Removal.KEPT_EXCEPT_NEAR_PUNCTUATION,
      Safety.UNSAFE,
      () -> GT._T("Figure space")),
  PUNCTUATION_SPACE(0x2008, 0x2008, Removal.KEPT, Safety.UNSAFE, () -> GT._T("Punctuation space")),
  ZERO_WIDTH_SPACE(
      0x200B, 0x200B, Removal.REMOVABLE, Safety.SAFE_IN_REDIRECT, () -> GT._T("Zero-width space")),
  LEFT_TO_RIGHT_MARK(
      0x200E,
      0x200E,
      Removal.REMOVABLE,
      Safety.SAFE_IN_REDIRECT,
      () -> GT._T("Left-to-right mark")),
  RIGHT_TO_LEFT_MARK(
      0x200F,
      0x200F,
      Removal.REMOVABLE,
      Safety.SAFE_IN_REDIRECT,
      () -> GT._T("Right-to-left mark")),
  LINE_SEPARATOR(0x2028, 0x2028, Removal.REMOVABLE, Safety.UNSAFE, () -> GT._T("Line separator")),
  LEFT_TO_RIGHT_EMBEDDING(
      0x202A, 0x202A, Removal.REMOVABLE, Safety.UNSAFE, () -> GT._T("Left-to-right embedding")),
  RIGHT_TO_LEFT_EMBEDDING(
      0x202B, 0x202B, Removal.REMOVABLE, Safety.UNSAFE, () -> GT._T("Right-to-left embedding")),
  POP_DIRECTIONAL_FORMATTING(
      0x202C, 0x202C, Removal.REMOVABLE, Safety.UNSAFE, () -> GT._T("Pop directional formatting")),
  LEFT_TO_RIGHT_OVERRIDE(
      0x202D, 0x202D, Removal.REMOVABLE, Safety.UNSAFE, () -> GT._T("Left-to-right override")),
  RIGHT_TO_LEFT_OVERRIDE(
      0x202E, 0x202E, Removal.REMOVABLE, Safety.UNSAFE, () -> GT._T("Right-to-left override")),
  BYTE_ORDER_MARK(0xFEFF, 0xFEFF, Removal.REMOVABLE, Safety.UNSAFE, () -> GT._T("Byte order mark")),
  OBJECT_REPLACEMENT_CHARACTER(
      0xFFFC,
      0xFFFC,
      Removal.REMOVABLE,
      Safety.UNSAFE,
      () -> GT._T("Object replacement character")),
  PRIVATE_USE_AREA(0xE000, 0xF8FF, Removal.KEPT, Safety.UNSAFE, () -> GT._T("Private use area")),
  PRIVATE_USE_AREA_A(
      0xF0000, 0xFFFFD, Removal.KEPT, Safety.UNSAFE, () -> GT._T("Private use area A")),
  PRIVATE_USE_AREA_B(
      0x100000, 0x10FFFD, Removal.KEPT, Safety.UNSAFE, () -> GT._T("Private use area B"));

  private static final int CHECK_MARK_SYMBOL = 0xF0FC;
  private static final List<String> CHECK_MARK_ALTERNATIVES = List.of("*", "✓");

  private final int first;
  private final int last;
  private final Removal removal;
  private final Safety safety;
  private final Supplier<String> descriptionSupplier;

  ControlCharacter(
      final int first,
      final int last,
      final Removal removal,
      final Safety safety,
      final Supplier<String> descriptionSupplier) {
    this.first = first;
    this.last = last;
    this.removal = removal;
    this.safety = safety;
    this.descriptionSupplier = descriptionSupplier;
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

  String description() {
    return descriptionSupplier.get();
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
