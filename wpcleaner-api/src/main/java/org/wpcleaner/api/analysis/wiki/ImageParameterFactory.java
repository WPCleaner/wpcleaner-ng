package org.wpcleaner.api.analysis.wiki;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import org.jspecify.annotations.Nullable;
import org.wpcleaner.api.analysis.image.ImageParameter;
import org.wpcleaner.api.repository.magicword.MagicWord;

final class ImageParameterFactory {

  private static final String MAGIC_WORD_PREFIX = "img_";
  private static final String ARGUMENT_PLACEHOLDER = "$1";
  private static final Pattern NUMERIC =
      Pattern.compile("[+-]?(\\d+\\.?\\d*|\\.\\d+)([eE][+-]?\\d+)?");
  private static final Pattern ANY = Pattern.compile(".*", Pattern.DOTALL);
  private static final Map<String, Pattern> ARGUMENT_PATTERNS =
      Map.of(
          "img_width", Pattern.compile("\\d*(x\\d*)?\\s*(px)?\\s*"),
          "img_page", NUMERIC,
          "img_upright", NUMERIC);

  private final List<MagicWord> imageMagicWords;

  ImageParameterFactory(final List<MagicWord> magicWords) {
    this.imageMagicWords =
        magicWords.stream()
            .filter(magicWord -> magicWord.name().startsWith(MAGIC_WORD_PREFIX))
            .toList();
  }

  ImageParameter create(final String text, final int begin, final int end) {
    final String value = text.substring(begin, end);
    final String trimmedValue = value.trim();
    for (final MagicWord magicWord : imageMagicWords) {
      for (final String alias : magicWord.aliases()) {
        final Optional<ImageParameter> parameter =
            matchAlias(magicWord, alias, trimmedValue)
                .map(argument -> createParameter(begin, end, value, magicWord, argument));
        if (parameter.isPresent()) {
          return parameter.get();
        }
      }
    }
    return new ImageParameter(begin, end, value, null, null);
  }

  private static ImageParameter createParameter(
      final int begin,
      final int end,
      final String value,
      final MagicWord magicWord,
      final AliasArgument aliasArgument) {
    final String argument = aliasArgument.argument();
    if (argument == null) {
      return new ImageParameter(begin, end, value, magicWord.name(), null);
    }
    if (!ARGUMENT_PATTERNS.getOrDefault(magicWord.name(), ANY).matcher(argument).matches()) {
      return new ImageParameter(begin, end, value, null, null);
    }
    return new ImageParameter(begin, end, value, magicWord.name(), argument);
  }

  private static Optional<AliasArgument> matchAlias(
      final MagicWord magicWord, final String alias, final String value) {
    final int placeholderIndex = alias.indexOf(ARGUMENT_PLACEHOLDER);
    if (placeholderIndex < 0) {
      return areEqual(magicWord, alias, value)
          ? Optional.of(new AliasArgument(null))
          : Optional.empty();
    }
    final String prefix = alias.substring(0, placeholderIndex);
    final String suffix = alias.substring(placeholderIndex + ARGUMENT_PLACEHOLDER.length());
    if (value.length() < prefix.length() + suffix.length()) {
      return Optional.empty();
    }
    final String valuePrefix = value.substring(0, prefix.length());
    final String valueSuffix = value.substring(value.length() - suffix.length());
    if (!areEqual(magicWord, prefix, valuePrefix) || !areEqual(magicWord, suffix, valueSuffix)) {
      return Optional.empty();
    }
    return Optional.of(
        new AliasArgument(value.substring(prefix.length(), value.length() - suffix.length())));
  }

  private static boolean areEqual(
      final MagicWord magicWord, final String alias, final String value) {
    return magicWord.caseSensitive()
        ? alias.equals(value)
        : alias.toLowerCase(Locale.ROOT).equals(value.toLowerCase(Locale.ROOT));
  }

  private record AliasArgument(@Nullable String argument) {}
}
