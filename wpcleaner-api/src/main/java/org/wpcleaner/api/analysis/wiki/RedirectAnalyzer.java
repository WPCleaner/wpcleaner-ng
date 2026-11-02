package org.wpcleaner.api.analysis.wiki;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.jspecify.annotations.Nullable;
import org.wpcleaner.api.analysis.Element;
import org.wpcleaner.api.analysis.redirect.RedirectElement;
import org.wpcleaner.api.repository.magicword.MagicWord;
import org.wpcleaner.api.repository.namespace.CommonNamespaces;

final class RedirectAnalyzer {

  private static final String REDIRECT_MAGIC_WORD = "redirect";
  private static final String LEADING_WHITESPACE = " \t\n\r\0\u000B";
  private static final Pattern LINK =
      Pattern.compile("\\s*:?\\s*(\\[{2})(.*?)(?:\\|(.*?))?(\\]{2})\\s*", Pattern.UNIX_LINES);
  private static final int LINK_BEGIN_GROUP = 1;
  private static final int TARGET_GROUP = 2;
  private static final int TEXT_GROUP = 3;
  private static final int LINK_END_GROUP = 4;
  private static final String LEADING_COLONS = "^:+";

  private final String text;
  private final List<MagicWord> magicWords;
  private final TitleDecoder titleDecoder;

  RedirectAnalyzer(
      final String text, final List<MagicWord> magicWords, final TitleDecoder titleDecoder) {
    this.text = text;
    this.magicWords = magicWords;
    this.titleDecoder = titleDecoder;
  }

  Optional<Redirect> analyze() {
    int begin = 0;
    while (begin < text.length() && LEADING_WHITESPACE.indexOf(text.charAt(begin)) >= 0) {
      begin++;
    }
    final Optional<Pattern> magicWordPattern = createMagicWordPattern();
    if (magicWordPattern.isEmpty()) {
      return Optional.empty();
    }
    final Matcher magicWordMatcher =
        magicWordPattern.get().matcher(text).region(begin, text.length());
    if (!magicWordMatcher.lookingAt()) {
      return Optional.empty();
    }
    final Matcher linkMatcher = LINK.matcher(text).region(magicWordMatcher.end(), text.length());
    if (!linkMatcher.lookingAt() || !isValidTarget(linkMatcher.group(TARGET_GROUP))) {
      return Optional.empty();
    }
    return Optional.of(
        new Redirect(
            begin,
            linkMatcher.start(LINK_BEGIN_GROUP),
            linkMatcher.end(LINK_END_GROUP),
            linkMatcher.end(),
            magicWordMatcher.group(),
            linkMatcher.group(TARGET_GROUP),
            linkMatcher.group(TEXT_GROUP)));
  }

  private Optional<Pattern> createMagicWordPattern() {
    return magicWords.stream()
        .filter(magicWord -> REDIRECT_MAGIC_WORD.equals(magicWord.name()))
        .findFirst()
        .filter(magicWord -> !magicWord.aliases().isEmpty())
        .map(
            magicWord ->
                Pattern.compile(
                    magicWord.aliases().stream()
                        .sorted(Comparator.comparingInt(String::length).reversed())
                        .map(Pattern::quote)
                        .collect(Collectors.joining("|")),
                    magicWord.caseSensitive()
                        ? 0
                        : Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE));
  }

  private boolean isValidTarget(final String target) {
    final String decodedTarget =
        target.indexOf('%') < 0 ? target : target.replaceFirst(LEADING_COLONS, "");
    return titleDecoder.decode(decodedTarget, CommonNamespaces.MAIN.id).isPresent();
  }

  record Redirect(
      int begin,
      int linkBegin,
      int end,
      int contentBegin,
      String name,
      String target,
      @Nullable String linkText) {

    RedirectElement toElement(final Element link) {
      return new RedirectElement(begin, end, name, link);
    }
  }
}
