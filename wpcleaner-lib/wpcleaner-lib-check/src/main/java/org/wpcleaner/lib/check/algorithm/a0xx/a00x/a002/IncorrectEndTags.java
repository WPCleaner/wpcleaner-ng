package org.wpcleaner.lib.check.algorithm.a0xx.a00x.a002;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.wpcleaner.api.analysis.PageAnalysis;
import org.wpcleaner.api.analysis.tag.HtmlTagType;
import org.wpcleaner.api.analysis.tag.TagBuilder;
import org.wpcleaner.api.analysis.tag.TagElement;
import org.wpcleaner.api.analysis.tag.TagKind;
import org.wpcleaner.lib.check.CheckError;
import org.wpcleaner.lib.check.Replacement;

record IncorrectEndTags(PageAnalysis analysis, TextExclusions exclusions) {

  private static final List<String> TAG_NAMES =
      Arrays.stream(HtmlTagType.values())
          .filter(type -> !type.isVoid())
          .map(HtmlTagType::getNormalizedName)
          .toList();

  List<CheckError> analyze(final int algorithm) {
    final List<CheckError> errors = new ArrayList<>();
    final String text = analysis.getText();
    int index = text.indexOf(TagElement.TOKEN_START);
    while (index >= 0) {
      final Optional<CheckError> error =
          exclusions.contains(index) ? Optional.empty() : analyzeTag(algorithm, index);
      error.ifPresent(errors::add);
      index = text.indexOf(TagElement.TOKEN_START, error.map(CheckError::end).orElse(index + 1));
    }
    return errors;
  }

  private Optional<CheckError> analyzeTag(final int algorithm, final int begin) {
    final String text = analysis.getText();
    final int slash = IncorrectVoidTags.skipWhitespace(text, begin + 1);
    if (!isAt(text, slash, TagElement.TOKEN_SLASH)) {
      return Optional.empty();
    }
    final int nameBegin = IncorrectVoidTags.skipWhitespace(text, slash + 1);
    return TAG_NAMES.stream()
        .filter(name -> isName(text, nameBegin, name))
        .findFirst()
        .flatMap(name -> createError(algorithm, begin, nameBegin + name.length(), name));
  }

  private Optional<CheckError> createError(
      final int algorithm, final int begin, final int nameEnd, final String name) {
    final String text = analysis.getText();
    final int slash = IncorrectVoidTags.skipWhitespace(text, nameEnd);
    if (!isAt(text, slash, TagElement.TOKEN_SLASH)) {
      return Optional.empty();
    }
    final int tokenEnd = IncorrectVoidTags.skipWhitespace(text, slash + 1);
    if (!isAt(text, tokenEnd, TagElement.TOKEN_END)) {
      return Optional.empty();
    }
    return Optional.of(
        new CheckError(
            algorithm,
            begin,
            tokenEnd + 1,
            List.of(new Replacement(TagBuilder.from(name).withKind(TagKind.END).build(), false))));
  }

  private static boolean isName(final String text, final int index, final String name) {
    final int end = index + name.length();
    return end < text.length()
        && text.regionMatches(true, index, name, 0, name.length())
        && !Character.isLetterOrDigit(text.charAt(end));
  }

  private static boolean isAt(final String text, final int index, final char character) {
    return index < text.length() && text.charAt(index) == character;
  }
}
