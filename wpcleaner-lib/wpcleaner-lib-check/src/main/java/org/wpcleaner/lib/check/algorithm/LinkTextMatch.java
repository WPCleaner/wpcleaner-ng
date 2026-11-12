package org.wpcleaner.lib.check.algorithm;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.wpcleaner.api.analysis.PageAnalysis;
import org.wpcleaner.api.analysis.comment.CommentedText;
import org.wpcleaner.api.analysis.internallink.InternalLinkElement;
import org.wpcleaner.api.repository.namespace.CommonNamespaces;

record LinkTextMatch(String before, String text, String after, boolean automatic, boolean risk) {

  private static final String CHARACTERS_BEFORE = "«'`‘\"„“” .,()–־—";
  private static final String CHARACTERS_AFTER = "»'`‘\"„“” .,();:–־—";
  private static final String UNSAFE_CHARACTERS = "[]{}|<>#";
  private static final Pattern LINE_BREAK_AT_END =
      Pattern.compile("(.*?)\\s*(<br\\s*/?\\s*>)\\s*", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
  private static final char APOSTROPHE = '\'';
  private static final char OPENING_QUOTE = '‘';
  private static final char UNDERSCORE = '_';
  private static final String ONLY_BEFORE_IGNORED = ".";
  private static final String ONLY_AFTER_IGNORED = "'";
  private static final String LINK_END = "]]";
  private static final char ANCHOR = '#';

  static Optional<LinkTextMatch> find(final PageAnalysis analysis, final InternalLinkElement link) {
    final String linkText = link.text();
    final String anchor = link.anchor();
    final boolean hasAnchor = anchor != null && !anchor.isBlank();
    if (linkText == null) {
      return Optional.empty();
    }
    final boolean commented =
        analysis.getComments().stream()
            .anyMatch(comment -> comment.begin() >= link.begin() && comment.end() <= link.end());
    final String text = withoutComments(analysis, link, linkText);
    final Optional<LinkTextMatch> match;
    if (hasAnchor && link.target().isBlank()) {
      final String anchorTarget = ANCHOR + anchor;
      match = find(analysis, link, text, anchorTarget, anchorTarget::equals);
    } else if (hasAnchor || link.title() == null) {
      match = Optional.empty();
    } else {
      match =
          find(
              analysis,
              link,
              text,
              link.target().strip(),
              candidate -> isSameTitle(analysis, link, candidate));
    }
    return match.map(found -> commented ? found.withoutAutomatic() : found);
  }

  private static String withoutComments(
      final PageAnalysis analysis, final InternalLinkElement link, final String linkText) {
    final int textEnd = link.end() - LINK_END.length();
    return new CommentedText(analysis.getText(), analysis.getComments())
        .withoutComments(textEnd - linkText.length(), textEnd);
  }

  private LinkTextMatch withoutAutomatic() {
    return new LinkTextMatch(before, text, after, false, risk);
  }

  private static Optional<LinkTextMatch> find(
      final PageAnalysis analysis,
      final InternalLinkElement link,
      final String text,
      final String target,
      final Predicate<String> sameTarget) {
    if (sameTarget.test(text)) {
      return Optional.of(new LinkTextMatch("", text, "", text.indexOf(UNDERSCORE) < 0, false));
    }
    final Matcher lineBreak = LINE_BREAK_AT_END.matcher(text);
    if (lineBreak.matches() && sameTarget.test(lineBreak.group(1))) {
      return Optional.of(
          new LinkTextMatch("", lineBreak.group(1), lineBreak.group(2), false, false));
    }
    return findPaddedMatch(analysis, link, text, target, sameTarget);
  }

  private static Optional<LinkTextMatch> findPaddedMatch(
      final PageAnalysis analysis,
      final InternalLinkElement link,
      final String text,
      final String target,
      final Predicate<String> sameTarget) {
    final int before = countPaddingBefore(text, target);
    final int after = countPaddingAfter(text, target, before);
    if (before + after == 0 || text.length() <= before + after) {
      return Optional.empty();
    }
    final String paddingBefore = text.substring(0, before);
    final String paddingAfter = text.substring(text.length() - after);
    final String linkText = text.substring(before, text.length() - after);
    if (isIgnoredPadding(paddingBefore, paddingAfter) || !sameTarget.test(linkText)) {
      return Optional.empty();
    }
    final boolean risk = isApostropheRisk(analysis.getText(), link, paddingBefore, paddingAfter);
    final boolean automatic =
        !risk
            && linkText.indexOf(UNDERSCORE) < 0
            && analysis.getPageTitle().namespace() == CommonNamespaces.MAIN.id;
    return Optional.of(new LinkTextMatch(paddingBefore, linkText, paddingAfter, automatic, risk));
  }

  private static int countPaddingBefore(final String text, final String target) {
    int before = 0;
    while (before < text.length()
        && CHARACTERS_BEFORE.indexOf(text.charAt(before)) >= 0
        && target.charAt(0) != text.charAt(before)) {
      before++;
    }
    return before;
  }

  private static int countPaddingAfter(final String text, final String target, final int before) {
    int after = 0;
    while (after < text.length()
        && CHARACTERS_AFTER.indexOf(text.charAt(text.length() - after - 1)) >= 0
        && (target.charAt(target.length() - 1) != text.charAt(text.length() - after - 1)
            || text.length() - before - after > target.length())) {
      after++;
    }
    return after;
  }

  private static boolean isIgnoredPadding(final String paddingBefore, final String paddingAfter) {
    return (ONLY_BEFORE_IGNORED.equals(paddingBefore) && paddingAfter.isEmpty())
        || (paddingBefore.isEmpty() && ONLY_AFTER_IGNORED.equals(paddingAfter));
  }

  private static boolean isApostropheRisk(
      final String pageText,
      final InternalLinkElement link,
      final String paddingBefore,
      final String paddingAfter) {
    final boolean riskBefore =
        link.begin() > 0
            && pageText.charAt(link.begin() - 1) == APOSTROPHE
            && paddingBefore.indexOf(APOSTROPHE) == 0;
    final boolean riskAfter =
        link.end() < pageText.length()
            && pageText.charAt(link.end()) == APOSTROPHE
            && !paddingAfter.isEmpty()
            && paddingAfter.charAt(paddingAfter.length() - 1) == APOSTROPHE;
    return riskBefore || riskAfter || paddingAfter.indexOf(OPENING_QUOTE) == 0;
  }

  private static boolean isSameTitle(
      final PageAnalysis analysis, final InternalLinkElement link, final String text) {
    return !text.isBlank()
        && text.charAt(0) != ':'
        && text.charAt(0) != '/'
        && text.chars().noneMatch(character -> UNSAFE_CHARACTERS.indexOf(character) >= 0)
        && analysis
            .normalizeTitle(text, CommonNamespaces.MAIN.id)
            .filter(title -> title.equals(link.title()))
            .isPresent();
  }
}
