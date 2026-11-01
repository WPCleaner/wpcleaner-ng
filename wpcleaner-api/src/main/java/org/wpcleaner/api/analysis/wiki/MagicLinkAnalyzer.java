package org.wpcleaner.api.analysis.wiki;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.wpcleaner.api.analysis.Element;
import org.wpcleaner.api.analysis.TextBrowser;
import org.wpcleaner.api.analysis.magiclink.MagicLinkElement;
import org.wpcleaner.api.repository.magiclink.MagicLinkType;

final class MagicLinkAnalyzer {

  private static final String SPACE = "(?:[\\x20\\t]|&nbsp;|&#0*160;|&#[Xx]0*[Aa]0;|\\p{Zs})";
  private static final String SPACES = "(?>" + SPACE + "+)";
  private static final String SPACE_OR_DASH = "(?:-|" + SPACE + ")";
  private static final Pattern MAGIC_LINK =
      Pattern.compile(
          "\\b(?:(RFC|PMID)"
              + SPACES
              + "([0-9]+)\\b|(ISBN)"
              + SPACES
              + "((?:97[89]"
              + SPACE_OR_DASH
              + "?)?(?:[0-9]"
              + SPACE_OR_DASH
              + "?){9}[0-9Xx]\\b))");
  private static final int NUMBERED_TYPE_GROUP = 1;
  private static final int NUMBERED_NUMBER_GROUP = 2;
  private static final int ISBN_TYPE_GROUP = 3;
  private static final int ISBN_NUMBER_GROUP = 4;

  private final String text;
  private final TextBrowser textBrowser;
  private final Set<MagicLinkType> enabledMagicLinks;
  private final List<MagicLinkElement> magicLinks;

  MagicLinkAnalyzer(
      final String text,
      final TextBrowser textBrowser,
      final Set<MagicLinkType> enabledMagicLinks) {
    this.text = text;
    this.textBrowser = textBrowser;
    this.enabledMagicLinks = enabledMagicLinks;
    this.magicLinks = new ArrayList<>();
  }

  List<MagicLinkElement> getMagicLinks() {
    return magicLinks;
  }

  void analyze(final List<? extends Element> links) {
    if (enabledMagicLinks.isEmpty()) {
      return;
    }
    final Matcher matcher = MAGIC_LINK.matcher(text);
    while (matcher.find()) {
      createMagicLink(matcher)
          .filter(magicLink -> enabledMagicLinks.contains(magicLink.type()))
          .filter(magicLink -> !textBrowser.isInsideExclusion(magicLink.begin()))
          .filter(
              magicLink ->
                  textBrowser.isContinuous(magicLink.begin(), magicLink.end() - magicLink.begin()))
          .filter(
              magicLink ->
                  links.stream()
                      .noneMatch(
                          link -> link.begin() < magicLink.end() && magicLink.begin() < link.end()))
          .ifPresent(magicLinks::add);
    }
  }

  private static Optional<MagicLinkElement> createMagicLink(final Matcher matcher) {
    final String isbnType = matcher.group(ISBN_TYPE_GROUP);
    final String type = isbnType == null ? matcher.group(NUMBERED_TYPE_GROUP) : isbnType;
    final String number =
        isbnType == null ? matcher.group(NUMBERED_NUMBER_GROUP) : matcher.group(ISBN_NUMBER_GROUP);
    return MagicLinkType.findByName(type)
        .map(
            magicLinkType ->
                new MagicLinkElement(matcher.start(), matcher.end(), magicLinkType, number));
  }
}
