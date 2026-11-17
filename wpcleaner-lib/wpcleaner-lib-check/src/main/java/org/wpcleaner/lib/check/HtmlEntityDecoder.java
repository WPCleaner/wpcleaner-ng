package org.wpcleaner.lib.check;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.wpcleaner.lib.check.algorithm.HtmlCharacter;

final class HtmlEntityDecoder {

  private static final Pattern ENTITY =
      Pattern.compile("&(?:#([0-9]{1,7})|#[xX]([0-9a-fA-F]{1,6})|([a-zA-Z][a-zA-Z0-9]*));");
  private static final int DECIMAL = 10;
  private static final int HEXADECIMAL = 16;
  private static final Map<String, String> NAMED_ENTITIES = createNamedEntities();

  private HtmlEntityDecoder() {}

  static String decode(final String text) {
    if (text.indexOf('&') < 0) {
      return text;
    }
    return ENTITY
        .matcher(text)
        .replaceAll(entity -> Matcher.quoteReplacement(decodeEntity(entity)));
  }

  private static String decodeEntity(final MatchResult entity) {
    final Optional<String> decoded;
    if (entity.group(1) != null) {
      decoded = decodeCodePoint(entity.group(1), DECIMAL);
    } else if (entity.group(2) != null) {
      decoded = decodeCodePoint(entity.group(2), HEXADECIMAL);
    } else {
      decoded = Optional.ofNullable(NAMED_ENTITIES.get(entity.group(3)));
    }
    return decoded.orElse(entity.group());
  }

  private static Optional<String> decodeCodePoint(final String digits, final int radix) {
    final int codePoint = Integer.parseInt(digits, radix);
    return Character.isValidCodePoint(codePoint)
        ? Optional.of(Character.toString(codePoint))
        : Optional.empty();
  }

  private static Map<String, String> createNamedEntities() {
    final Map<String, String> entities = new HashMap<>();
    Arrays.stream(HtmlCharacter.values())
        .forEach(
            character ->
                entities.put(character.getEntityName(), String.valueOf(character.getValue())));
    entities.put("amp", "&");
    entities.put("apos", "'");
    entities.put("gt", ">");
    entities.put("lt", "<");
    return Map.copyOf(entities);
  }
}
