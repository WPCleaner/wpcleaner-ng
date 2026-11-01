package org.wpcleaner.api.analysis.wiki;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import org.wpcleaner.api.analysis.TextBrowser;
import org.wpcleaner.api.analysis.behaviorswitch.BehaviorSwitchElement;
import org.wpcleaner.api.repository.magicword.MagicWord;

final class BehaviorSwitchAnalyzer {

  private static final String PREFIX = "__";

  private final String text;
  private final TextBrowser textBrowser;
  private final List<MagicWord> behaviorSwitchMagicWords;
  private final List<BehaviorSwitchElement> behaviorSwitches;

  BehaviorSwitchAnalyzer(
      final String text, final TextBrowser textBrowser, final List<MagicWord> magicWords) {
    this.text = text;
    this.textBrowser = textBrowser;
    this.behaviorSwitchMagicWords =
        magicWords.stream().filter(MagicWord::doubleUnderscore).toList();
    this.behaviorSwitches = new ArrayList<>();
  }

  List<BehaviorSwitchElement> getBehaviorSwitches() {
    return behaviorSwitches;
  }

  void analyze() {
    if (behaviorSwitchMagicWords.isEmpty()) {
      return;
    }
    TextBrowser.Cursor cursor = textBrowser.cursor();
    while (cursor.getIndex() < text.length()) {
      final Optional<BehaviorSwitchElement> behaviorSwitch = findBehaviorSwitch(cursor.getIndex());
      if (behaviorSwitch.isPresent()) {
        behaviorSwitches.add(behaviorSwitch.get());
        cursor = textBrowser.cursor(behaviorSwitch.get().end());
      } else {
        cursor.moveNext();
      }
    }
  }

  private Optional<BehaviorSwitchElement> findBehaviorSwitch(final int begin) {
    if (!text.startsWith(PREFIX, begin)) {
      return Optional.empty();
    }
    return behaviorSwitchMagicWords.stream()
        .flatMap(
            magicWord ->
                magicWord.aliases().stream()
                    .filter(alias -> matchesAlias(begin, alias, magicWord.caseSensitive()))
                    .map(
                        alias ->
                            new BehaviorSwitchElement(
                                begin,
                                begin + alias.length(),
                                text.substring(begin, begin + alias.length()),
                                magicWord.name())))
        .max(Comparator.comparingInt(BehaviorSwitchElement::end));
  }

  private boolean matchesAlias(final int begin, final String alias, final boolean caseSensitive) {
    return text.regionMatches(!caseSensitive, begin, alias, 0, alias.length())
        && isContinuous(begin, alias.length());
  }

  private boolean isContinuous(final int begin, final int length) {
    final TextBrowser.Cursor cursor = textBrowser.cursor(begin);
    for (int offset = 1; offset < length; offset++) {
      cursor.moveNext();
      if (!cursor.samePart() || cursor.getIndex() != begin + offset) {
        return false;
      }
    }
    return true;
  }
}
