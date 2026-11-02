package org.wpcleaner.api.analysis.wiki;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.ArrayList;
import java.util.List;
import java.util.NavigableSet;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.wpcleaner.api.analysis.Element;
import org.wpcleaner.api.analysis.TextBrowser;
import org.wpcleaner.api.analysis.externallink.ExternalLinkElement;
import org.wpcleaner.api.analysis.image.ImageElement;
import org.wpcleaner.api.analysis.image.ImageParameter;
import org.wpcleaner.api.analysis.magiclink.MagicLinkElement;
import org.wpcleaner.api.repository.magiclink.MagicLinkType;
import org.wpcleaner.api.repository.protocol.Protocol;

final class MagicLinkAnalyzer {

  private static final String RELATIVE_PROTOCOL = "//";
  private static final String NO_MATCH = "(?!)";
  private static final String URL_CHARACTER = "[^\\]\\[<>\"\\x00-\\x20\\x7F\\p{Zs}\\x{FFFD}]";
  private static final String ADDRESS = "(?:[0-9.]+|\\[(?i:[0-9a-f:.]+)\\]|" + URL_CHARACTER + ")";
  private static final String SPACE = "(?:[\\x20\\t]|&nbsp;|&#0*160;|&#[Xx]0*[Aa]0;|\\p{Zs})";
  private static final String SPACES = "(?>" + SPACE + "+)";
  private static final String SPACE_OR_DASH = "(?:-|" + SPACE + ")";
  private static final String MAGIC_LINK =
      "(?:(RFC|PMID)"
          + SPACES
          + "([0-9]+)\\b|(ISBN)"
          + SPACES
          + "((?:97[89]"
          + SPACE_OR_DASH
          + "?)?(?:[0-9]"
          + SPACE_OR_DASH
          + "?){9}[0-9Xx]\\b))";
  private static final String MAGIC_LINK_FIRST_LETTERS = "RPI";
  private static final Pattern URL_END_ENTITY =
      Pattern.compile("&(lt|gt|nbsp|#x0*(3[CcEe]|[Aa]0)|#0*(60|62|160));");
  private static final Pattern TRAILING_ENTITY =
      Pattern.compile("&([a-z]+|#x[\\da-f]+|#\\d+)$", Pattern.CASE_INSENSITIVE);
  private static final String TRAILING_SEPARATORS = ",;.:!?";
  private static final char TRAILING_PARENTHESIS = ')';
  private static final char OPENING_PARENTHESIS = '(';
  private static final char ENTITY_END = ';';
  private static final int URL_GROUP = 1;
  private static final int NUMBERED_TYPE_GROUP = 2;
  private static final int NUMBERED_NUMBER_GROUP = 3;
  private static final int ISBN_TYPE_GROUP = 4;
  private static final int ISBN_NUMBER_GROUP = 5;

  private final String text;
  private final TextBrowser textBrowser;
  private final Set<MagicLinkType> enabledMagicLinks;
  private final List<Protocol> protocols;
  private final TextContainers containers;
  private final NavigableSet<Integer> curlyBracketElementBegins;
  private final Pattern pattern;
  private final List<MagicLinkElement> magicLinks;
  private final List<ExternalLinkElement> freeExternalLinks;

  MagicLinkAnalyzer(
      final String text,
      final TextBrowser textBrowser,
      final Set<MagicLinkType> enabledMagicLinks,
      final List<Protocol> protocols,
      final TextContainers containers,
      final List<? extends Element> curlyBracketElements) {
    this.text = text;
    this.textBrowser = textBrowser;
    this.enabledMagicLinks = enabledMagicLinks;
    this.protocols =
        protocols.stream().filter(protocol -> !RELATIVE_PROTOCOL.equals(protocol.value())).toList();
    this.containers = containers;
    this.curlyBracketElementBegins =
        curlyBracketElements.stream()
            .map(Element::begin)
            .collect(Collectors.toCollection(TreeSet::new));
    this.pattern =
        Pattern.compile(
            "(?=["
                + createFirstLetters()
                + "])\\b(?:("
                + createUrlPattern()
                + ")|"
                + MAGIC_LINK
                + ")");
    this.magicLinks = new ArrayList<>();
    this.freeExternalLinks = new ArrayList<>();
  }

  List<MagicLinkElement> getMagicLinks() {
    return magicLinks;
  }

  List<ExternalLinkElement> getFreeExternalLinks() {
    return freeExternalLinks;
  }

  void analyze(final List<? extends Element> links, final List<ImageElement> images) {
    final SortedAreas linkAreas =
        new SortedAreas(
            Stream.concat(
                    links.stream().map(link -> new Area(link.begin(), link.end())),
                    images.stream().flatMap(MagicLinkAnalyzer::computeImageAreasWithoutCaption))
                .toList());
    final Matcher matcher = pattern.matcher(text);
    while (matcher.find()) {
      final int begin = matcher.start();
      if (isOutsideOtherElements(begin, matcher.end(), linkAreas)) {
        if (matcher.group(URL_GROUP) == null) {
          createMagicLink(matcher)
              .filter(magicLink -> enabledMagicLinks.contains(magicLink.type()))
              .ifPresent(magicLinks::add);
        } else {
          createFreeExternalLink(begin, matcher.end()).ifPresent(freeExternalLinks::add);
        }
      }
    }
  }

  private boolean isOutsideOtherElements(
      final int begin, final int end, final SortedAreas linkAreas) {
    return !textBrowser.isInsideExclusion(begin)
        && textBrowser.isContinuous(begin, end - begin)
        && !linkAreas.overlaps(begin, end);
  }

  private Optional<ExternalLinkElement> createFreeExternalLink(final int begin, final int end) {
    final Integer curlyBracketElementBegin = curlyBracketElementBegins.higher(begin);
    int urlEnd = Math.min(end, containers.findContainer(begin).end());
    if (curlyBracketElementBegin != null) {
      urlEnd = Math.min(urlEnd, curlyBracketElementBegin);
    }
    String url = text.substring(begin, urlEnd);
    final Matcher entityMatcher = URL_END_ENTITY.matcher(url);
    if (entityMatcher.find()) {
      url = url.substring(0, entityMatcher.start());
    }
    url = url.substring(0, url.length() - countTrailingSeparators(url));
    final int protocolLength = findProtocolLength(url);
    if (protocolLength == 0 || url.length() <= protocolLength) {
      return Optional.empty();
    }
    return Optional.of(new ExternalLinkElement(begin, begin + url.length(), url, null, false));
  }

  private static int countTrailingSeparators(final String url) {
    final String separators =
        url.indexOf(OPENING_PARENTHESIS) < 0
            ? TRAILING_SEPARATORS + TRAILING_PARENTHESIS
            : TRAILING_SEPARATORS;
    int count = 0;
    while (count < url.length() && separators.indexOf(url.charAt(url.length() - 1 - count)) >= 0) {
      count++;
    }
    if (count > 0
        && url.charAt(url.length() - count) == ENTITY_END
        && TRAILING_ENTITY.matcher(url.substring(0, url.length() - count)).find()) {
      count--;
    }
    return count;
  }

  private int findProtocolLength(final String url) {
    return protocols.stream()
        .map(Protocol::value)
        .filter(protocol -> url.regionMatches(true, 0, protocol, 0, protocol.length()))
        .mapToInt(String::length)
        .max()
        .orElse(0);
  }

  private String createFirstLetters() {
    return Stream.concat(
            protocols.stream().map(Protocol::value), Stream.of(MAGIC_LINK_FIRST_LETTERS.split("")))
        .mapToInt(value -> value.codePointAt(0))
        .flatMap(
            firstLetter ->
                IntStream.of(
                    firstLetter,
                    Character.toLowerCase(firstLetter),
                    Character.toUpperCase(firstLetter)))
        .distinct()
        .mapToObj(firstLetter -> "\\x{" + Integer.toHexString(firstLetter) + "}")
        .collect(Collectors.joining());
  }

  private String createUrlPattern() {
    if (protocols.isEmpty()) {
      return NO_MATCH;
    }
    return "(?i:"
        + protocols.stream()
            .map(protocol -> Pattern.quote(protocol.value()))
            .collect(Collectors.joining("|"))
        + ")"
        + ADDRESS
        + URL_CHARACTER
        + "*";
  }

  private static Stream<Area> computeImageAreasWithoutCaption(final ImageElement image) {
    final Optional<ImageParameter> caption = image.findCaption();
    if (caption.isEmpty()) {
      return Stream.of(new Area(image.begin(), image.end()));
    }
    return Stream.of(
        new Area(image.begin(), caption.get().begin()), new Area(caption.get().end(), image.end()));
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
