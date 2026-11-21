package org.wpcleaner.lib.check.algorithm.a0xx.a02x.a027;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import org.wpcleaner.api.analysis.PageAnalysis;
import org.wpcleaner.lib.check.CheckAlgorithm;
import org.wpcleaner.lib.check.CheckError;
import org.wpcleaner.lib.check.Replacement;
import org.wpcleaner.lib.check.algorithm.HtmlCharacter;

@Component
public class CheckAlgorithm027 implements CheckAlgorithm {

  private static final int ALGORITHM_NUMBER = 27;
  private static final int FIRST_NON_ASCII = 0x80;
  private static final int FIRST_WINDOWS_1252 = 0x80;
  private static final int LAST_WINDOWS_1252 = 0x9F;
  private static final Charset WINDOWS_1252 = Charset.forName("windows-1252");
  private static final int UNMAPPED = 0xFFFD;
  private static final String KEPT_CHARACTERS = "&':;<=>[]{|}~\u2011";
  private static final Set<Integer> INVISIBLE_TYPES =
      Set.of(
          (int) Character.CONTROL,
          (int) Character.FORMAT,
          (int) Character.PRIVATE_USE,
          (int) Character.SURROGATE,
          (int) Character.UNASSIGNED,
          (int) Character.SPACE_SEPARATOR,
          (int) Character.LINE_SEPARATOR,
          (int) Character.PARAGRAPH_SEPARATOR);
  private static final Set<Integer> HTML_CHARACTERS =
      Arrays.stream(HtmlCharacter.values())
          .map(character -> (int) character.getValue())
          .collect(Collectors.toUnmodifiableSet());

  @Override
  public int number() {
    return ALGORITHM_NUMBER;
  }

  @Override
  public List<CheckError> analyze(final PageAnalysis analysis) {
    return NumericEntity.find(analysis).stream()
        .flatMap(entity -> createError(entity).stream())
        .toList();
  }

  private static Optional<CheckError> createError(final NumericEntity entity) {
    final int codePoint = displayedCodePoint(entity.codePoint());
    if (KEPT_CHARACTERS.indexOf(codePoint) >= 0
        || INVISIBLE_TYPES.contains(Character.getType(codePoint))) {
      return Optional.empty();
    }
    return Optional.of(
        new CheckError(
            ALGORITHM_NUMBER,
            entity.begin(),
            entity.end(),
            List.of(new Replacement(Character.toString(codePoint), isAutomatic(codePoint)))));
  }

  private static int displayedCodePoint(final int codePoint) {
    if (codePoint < FIRST_WINDOWS_1252 || codePoint > LAST_WINDOWS_1252) {
      return codePoint;
    }
    final String decoded = new String(new byte[] {(byte) codePoint}, WINDOWS_1252);
    return decoded.codePointAt(0) == UNMAPPED ? codePoint : decoded.codePointAt(0);
  }

  private static boolean isAutomatic(final int codePoint) {
    if (codePoint < FIRST_NON_ASCII) {
      return Character.isLetterOrDigit(codePoint);
    }
    return HTML_CHARACTERS.contains(codePoint);
  }
}
