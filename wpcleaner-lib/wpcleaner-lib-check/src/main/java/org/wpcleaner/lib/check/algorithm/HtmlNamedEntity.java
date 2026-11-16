package org.wpcleaner.lib.check.algorithm;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.wpcleaner.api.analysis.PageAnalysis;
import org.wpcleaner.api.analysis.externallink.ExternalLinkElement;
import org.wpcleaner.api.analysis.tag.CompleteTagElement;
import org.wpcleaner.api.analysis.tag.WikiTagType;

public record HtmlNamedEntity(int begin, int end, HtmlCharacter character) {

  private static final char AMPERSAND = '&';
  private static final char SEMICOLON = ';';

  public static List<HtmlNamedEntity> findWithSemicolon(
      final PageAnalysis analysis, final Collection<HtmlCharacter> characters) {
    final String text = analysis.getText();
    return find(
        analysis,
        ampersand ->
            characters.stream()
                .map(character -> of(text, ampersand, character))
                .filter(entity -> isAt(text, entity.end(), SEMICOLON))
                .map(entity -> new HtmlNamedEntity(ampersand, entity.end() + 1, entity.character()))
                .findFirst());
  }

  public static List<HtmlNamedEntity> findWithoutSemicolon(
      final PageAnalysis analysis, final Collection<HtmlCharacter> characters) {
    final String text = analysis.getText();
    final List<HtmlCharacter> longestFirst =
        characters.stream()
            .sorted(
                Comparator.comparingInt(
                        (HtmlCharacter character) -> character.getEntityName().length())
                    .reversed())
            .toList();
    return find(
        analysis,
        ampersand ->
            longestFirst.stream()
                .map(character -> of(text, ampersand, character))
                .filter(entity -> entity.end() <= text.length())
                .findFirst()
                .filter(entity -> !isAt(text, entity.end(), SEMICOLON)));
  }

  private static List<HtmlNamedEntity> find(
      final PageAnalysis analysis, final EntityFinder finder) {
    final String text = analysis.getText();
    final TextExclusions exclusions = createExclusions(analysis);
    return IntStream.range(0, text.length())
        .filter(index -> text.charAt(index) == AMPERSAND)
        .filter(index -> !exclusions.contains(index))
        .boxed()
        .flatMap(index -> finder.findAt(index).stream())
        .toList();
  }

  private static HtmlNamedEntity of(
      final String text, final int ampersand, final HtmlCharacter character) {
    final String name = character.getEntityName();
    final int nameBegin = ampersand + 1;
    return new HtmlNamedEntity(
        ampersand,
        text.startsWith(name, nameBegin) ? nameBegin + name.length() : Integer.MAX_VALUE,
        character);
  }

  private static boolean isAt(final String text, final int index, final char character) {
    return index < text.length() && text.charAt(index) == character;
  }

  private static TextExclusions createExclusions(final PageAnalysis analysis) {
    return new TextExclusions(
        Stream.of(
                analysis.getComments().stream()
                    .map(comment -> new TextExclusions.Area(comment.begin(), comment.end())),
                analysis.getCompleteTags().stream()
                    .filter(HtmlNamedEntity::isSourceCode)
                    .map(TextExclusions.Area::inside),
                analysis.getExternalLinks().stream().map(HtmlNamedEntity::urlArea))
            .flatMap(areas -> areas)
            .toList());
  }

  private static boolean isSourceCode(final CompleteTagElement tag) {
    return tag.startTag()
        .type()
        .filter(type -> type == WikiTagType.SOURCE || type == WikiTagType.SYNTAXHIGHLIGHT)
        .isPresent();
  }

  private static TextExclusions.Area urlArea(final ExternalLinkElement link) {
    final String linkText = link.text();
    return new TextExclusions.Area(
        link.begin(),
        link.bracketed() && linkText != null ? link.end() - 1 - linkText.length() : link.end());
  }

  @FunctionalInterface
  private interface EntityFinder {

    Optional<HtmlNamedEntity> findAt(int ampersand);
  }
}
