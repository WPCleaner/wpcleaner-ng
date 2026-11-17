package org.wpcleaner.lib.check.algorithm.a0xx.a01x.a016;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

record ControlCharacterRange(
    String original,
    String replacement,
    List<String> alternatives,
    Set<Integer> controls,
    boolean automatic,
    boolean safeInRedirect) {

  private static final String AUTOMATIC_CHARACTERS =
      "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
          + "abcdefghijklmnopqrstuvwxyz"
          + "0123456789"
          + "áàâäåãÀéèêëÉíìîïĩóôöōŌúùûüýćčńňšźØ"
          + " []|(){}<>,.!¡?;:-–—=+*#/%'\"«»\n\t→‘’°@&﻿​";
  private static final String ZERO_WIDTH_SPACE_NEIGHBORS = " |]";
  private static final String PUNCTUATION_AFTER_SPACE = "»:?;";
  private static final int OPENING_GUILLEMET = '«';

  ControlCharacterRange {
    alternatives = List.copyOf(alternatives);
    controls = Set.copyOf(controls);
  }

  static ControlCharacterRange of(
      final String text, final int begin, final int end, final EnabledControlCharacters enabled) {
    final String original = text.substring(begin, end);
    final StringBuilder replacement = new StringBuilder();
    final Set<String> alternatives = new LinkedHashSet<>();
    final Set<Integer> controls = new LinkedHashSet<>();
    boolean unsafeCharacter = false;
    boolean unsafeControl = false;
    boolean safeInRedirect = true;
    int index = begin;
    while (index < end) {
      final int codePoint = text.codePointAt(index);
      final Optional<ControlCharacter> optionalControl = enabled.find(codePoint);
      if (optionalControl.isEmpty()) {
        replacement.appendCodePoint(codePoint);
        unsafeCharacter |= AUTOMATIC_CHARACTERS.indexOf(codePoint) < 0;
      } else {
        final ControlCharacter control = optionalControl.get();
        controls.add(codePoint);
        safeInRedirect &= control.isSafeInRedirect();
        final String controlText = Character.toString(codePoint);
        final String controlReplacement = replace(text, index, end, control, controlText);
        replacement.append(controlReplacement);
        unsafeControl |=
            controlReplacement.equals(controlText)
                || (!control.isSafe() && !isHarmlessZeroWidthSpace(text, index, control));
        control
            .alternatives(codePoint)
            .forEach(alternative -> alternatives.add(original.replace(controlText, alternative)));
      }
      index += Character.charCount(codePoint);
    }
    return new ControlCharacterRange(
        original,
        replacement.toString(),
        List.copyOf(alternatives),
        controls,
        !unsafeCharacter || !unsafeControl,
        safeInRedirect);
  }

  private static String replace(
      final String text,
      final int index,
      final int end,
      final ControlCharacter control,
      final String controlText) {
    if (control.isRemovable()) {
      return "";
    }
    if (control.isReplacedNearPunctuation() && isNearPunctuation(text, index, end)) {
      return " ";
    }
    return controlText;
  }

  private static boolean isNearPunctuation(final String text, final int index, final int end) {
    final int next = index + Character.charCount(text.codePointAt(index));
    return (index > 0 && text.codePointBefore(index) == OPENING_GUILLEMET)
        || (next < end && PUNCTUATION_AFTER_SPACE.indexOf(text.codePointAt(next)) >= 0);
  }

  private static boolean isHarmlessZeroWidthSpace(
      final String text, final int index, final ControlCharacter control) {
    if (control != ControlCharacter.ZERO_WIDTH_SPACE) {
      return false;
    }
    final int next = index + Character.charCount(text.codePointAt(index));
    return index == 0
        || ZERO_WIDTH_SPACE_NEIGHBORS.indexOf(text.charAt(index - 1)) >= 0
        || ControlCharacter.of(text.codePointBefore(index))
            .filter(ControlCharacter.ZERO_WIDTH_SPACE::equals)
            .isPresent()
        || next >= text.length()
        || ZERO_WIDTH_SPACE_NEIGHBORS.indexOf(text.charAt(next)) >= 0;
  }
}
