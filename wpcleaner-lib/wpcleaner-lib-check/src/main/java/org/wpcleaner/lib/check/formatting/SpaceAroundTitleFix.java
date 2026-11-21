package org.wpcleaner.lib.check.formatting;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.Optional;
import org.springframework.stereotype.Component;
import org.wpcleaner.api.analysis.PageAnalysis;
import org.wpcleaner.api.analysis.title.TitleElement;
import org.wpcleaner.api.repository.configuration.WPCleanerConfigurationRepository;
import org.wpcleaner.api.repository.configuration.WPCleanerParameter;

@Component
public class SpaceAroundTitleFix implements FormattingFix {

  private static final char EQUAL = '=';
  private static final String SPACES = "  ";
  private static final String SPACE = " ";
  private static final String TRAILING_WHITESPACE = " \t";

  private final WPCleanerConfigurationRepository wpcleanerRepository;

  public SpaceAroundTitleFix(final WPCleanerConfigurationRepository wpcleanerRepository) {
    this.wpcleanerRepository = wpcleanerRepository;
  }

  @Override
  public String fix(final PageAnalysis analysis) {
    return CountOption.from(
            wpcleanerRepository.getConfiguration(analysis.getWiki()),
            WPCleanerParameter.AUTO_SPACE_AROUND_TITLE)
        .map(option -> fix(analysis, option))
        .orElseGet(analysis::getText);
  }

  private static String fix(final PageAnalysis analysis, final CountOption option) {
    final String text = analysis.getText();
    return TextChange.apply(
        text,
        analysis.getTitles().stream()
            .filter(title -> isSimpleTitle(text, title))
            .flatMap(title -> fix(text, title, option).stream())
            .toList());
  }

  private static boolean isSimpleTitle(final String text, final TitleElement title) {
    final int level = title.level();
    return title.begin() + 2 * level < title.end()
        && text.charAt(title.begin() + level) != EQUAL
        && text.charAt(title.end() - level - 1) != EQUAL
        && isNothingAfter(text, title.end());
  }

  private static boolean isNothingAfter(final String text, final int begin) {
    int index = begin;
    while (index < text.length() && TRAILING_WHITESPACE.indexOf(text.charAt(index)) >= 0) {
      index++;
    }
    return index >= text.length() || FormattingText.isLineEnd(text.charAt(index));
  }

  private static Optional<TextChange> fix(
      final String text, final TitleElement title, final CountOption option) {
    final int level = title.level();
    final String value = text.substring(title.begin() + level, title.end() - level);
    final int spacesBefore = countSpacesBefore(value);
    if (spacesBefore == value.length()) {
      return Optional.empty();
    }
    final int spacesAfter = countSpacesAfter(value);
    if (option.accepts(spacesBefore) && option.accepts(spacesAfter)) {
      return Optional.empty();
    }
    final String equals = String.valueOf(EQUAL).repeat(level);
    return Optional.of(
        new TextChange(
            title.begin(),
            title.end(),
            equals
                + SPACE.repeat(option.normalize(spacesBefore))
                + value.substring(spacesBefore, value.length() - spacesAfter)
                + SPACE.repeat(option.normalize(spacesAfter))
                + equals));
  }

  private static int countSpacesBefore(final String value) {
    int count = 0;
    while (count < value.length() && SPACES.indexOf(value.charAt(count)) >= 0) {
      count++;
    }
    return count;
  }

  private static int countSpacesAfter(final String value) {
    int count = 0;
    while (count < value.length()
        && SPACES.indexOf(value.charAt(value.length() - count - 1)) >= 0) {
      count++;
    }
    return count;
  }
}
