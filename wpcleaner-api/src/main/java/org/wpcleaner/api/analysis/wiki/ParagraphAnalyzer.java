package org.wpcleaner.api.analysis.wiki;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.wpcleaner.api.analysis.paragraph.ParagraphElement;
import org.wpcleaner.api.analysis.table.TableElement;
import org.wpcleaner.api.analysis.title.TitleElement;

final class ParagraphAnalyzer {

  private static final String NOT_TEXT_FIRST_CHARACTERS = " \t*#:;";
  private static final String HORIZONTAL_RULE = "----";

  private final String text;
  private final LineAnalyzer lineAnalyzer;
  private final TextContainers containers;
  private final Set<Integer> titleBegins;
  private final List<ParagraphElement> paragraphs;
  private int paragraphBegin;
  private int paragraphEnd;

  ParagraphAnalyzer(
      final String text,
      final LineAnalyzer lineAnalyzer,
      final TextContainers containers,
      final List<TitleElement> titles) {
    this.text = text;
    this.lineAnalyzer = lineAnalyzer;
    this.containers = containers;
    this.titleBegins = titles.stream().map(TitleElement::begin).collect(Collectors.toSet());
    this.paragraphs = new ArrayList<>();
    this.paragraphBegin = -1;
    this.paragraphEnd = -1;
  }

  List<ParagraphElement> getParagraphs() {
    return paragraphs;
  }

  void analyze(final List<TableElement> tables) {
    for (final LineAnalyzer.Line line : lineAnalyzer.getLines()) {
      if (containers.isWholeText(line.container())) {
        analyzeLine(line, tables);
      }
    }
    endParagraph();
  }

  private void analyzeLine(final LineAnalyzer.Line line, final List<TableElement> tables) {
    final String content = text.substring(line.contentBegin(), line.end());
    if (content.isBlank() && line.contentBegin() > line.begin()) {
      return;
    }
    if (isTextLine(line, content, tables)) {
      if (paragraphBegin < 0) {
        paragraphBegin = line.contentBegin();
      }
      paragraphEnd = line.end();
    } else {
      endParagraph();
    }
  }

  private boolean isTextLine(
      final LineAnalyzer.Line line, final String content, final List<TableElement> tables) {
    return !content.isBlank()
        && NOT_TEXT_FIRST_CHARACTERS.indexOf(content.charAt(0)) < 0
        && !content.startsWith(HORIZONTAL_RULE)
        && !titleBegins.contains(line.contentBegin())
        && tables.stream()
            .noneMatch(table -> table.begin() < line.end() && line.contentBegin() < table.end());
  }

  private void endParagraph() {
    if (paragraphBegin >= 0) {
      paragraphs.add(new ParagraphElement(paragraphBegin, paragraphEnd));
    }
    paragraphBegin = -1;
  }
}
