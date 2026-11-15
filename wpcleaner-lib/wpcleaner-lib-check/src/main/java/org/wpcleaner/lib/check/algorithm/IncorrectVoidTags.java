package org.wpcleaner.lib.check.algorithm;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.wpcleaner.api.analysis.PageAnalysis;
import org.wpcleaner.api.analysis.tag.TagBuilder;
import org.wpcleaner.api.analysis.tag.TagElement;
import org.wpcleaner.api.analysis.tag.TagKind;
import org.wpcleaner.api.repository.namespace.CommonNamespaces;
import org.wpcleaner.lib.check.CheckError;
import org.wpcleaner.lib.check.Replacement;

record IncorrectVoidTags(PageAnalysis analysis, TextExclusions exclusions, TagKind kind) {

  private static final List<String> TAG_NAMES = List.of("br", "hr");
  private static final String INCORRECT_BEFORE_NAME = " \\.,:?/\n|+&)(";
  private static final String INCORRECT_AFTER_NAME = " \\.,:?\n|+&)(`";
  private static final String INCORRECT_AFTER_SLASH = " \\.,:?/\n|+&)(`";
  private static final String SPACES = " \t";
  private static final String ALWAYS_AUTOMATIC = "</br>";

  List<CheckError> analyze(final int algorithm) {
    final List<CheckError> errors = new ArrayList<>();
    for (final String name : TAG_NAMES) {
      final String text = analysis.getText();
      int index = text.indexOf(TagElement.TOKEN_START);
      while (index >= 0) {
        final Optional<CheckError> error =
            exclusions.contains(index) ? Optional.empty() : analyzeTag(algorithm, name, index);
        error.ifPresent(errors::add);
        index = text.indexOf(TagElement.TOKEN_START, error.map(CheckError::end).orElse(index + 1));
      }
    }
    return errors;
  }

  private Optional<CheckError> analyzeTag(final int algorithm, final String name, final int begin) {
    final String text = analysis.getText();
    final TagScanner scan = new TagScanner(text, begin + 1);
    scan.skip(SPACES, false);
    scan.skip(INCORRECT_BEFORE_NAME, true);
    if (!text.regionMatches(true, scan.index, name, 0, name.length())) {
      return Optional.empty();
    }
    scan.index += name.length();
    if (scan.index >= text.length() || Character.isLetter(text.charAt(scan.index))) {
      return Optional.empty();
    }
    scan.skip(SPACES, false);
    scan.skip(INCORRECT_AFTER_NAME, true);
    scan.skip(SPACES, false);
    if (scan.index < text.length() && text.charAt(scan.index) == TagElement.TOKEN_SLASH) {
      scan.index++;
    }
    scan.skip(SPACES, false);
    scan.skip(INCORRECT_AFTER_SLASH, true);
    if (scan.index >= text.length() || !shouldReport(scan, name, begin)) {
      return Optional.empty();
    }
    final boolean endsWithToken = text.charAt(scan.index) == TagElement.TOKEN_END;
    final int end = endsWithToken ? scan.index + 1 : scan.index;
    final boolean automatic =
        (endsWithToken
                && scan.incorrect
                && analysis.getPageTitle().namespace() == CommonNamespaces.MAIN.id)
            || ALWAYS_AUTOMATIC.equals(text.substring(begin, end));
    return Optional.of(
        new CheckError(
            algorithm,
            begin,
            end,
            List.of(new Replacement(TagBuilder.from(name).withKind(kind).build(), automatic))));
  }

  private boolean shouldReport(final TagScanner scan, final String name, final int begin) {
    return scan.incorrect
        || (scan.text.charAt(scan.index) != TagElement.TOKEN_END
            && analysis.getTags().stream()
                .noneMatch(
                    tag ->
                        tag.normalizedName().equals(name)
                            && tag.begin() <= begin
                            && begin < tag.end()));
  }

  static int skipWhitespace(final String text, final int index) {
    int current = index;
    while (current < text.length() && Character.isWhitespace(text.charAt(current))) {
      current++;
    }
    return current;
  }

  private static final class TagScanner {

    private final String text;
    private int index;
    private boolean incorrect;

    private TagScanner(final String text, final int index) {
      this.text = text;
      this.index = index;
    }

    private void skip(final String characters, final boolean incorrectCharacters) {
      while (index < text.length() && characters.indexOf(text.charAt(index)) >= 0) {
        index++;
        incorrect |= incorrectCharacters;
      }
    }
  }
}
