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
import org.wpcleaner.api.analysis.preformatted.PreformattedElement;
import org.wpcleaner.api.analysis.table.TableElement;
import org.wpcleaner.api.analysis.title.TitleElement;

final class ParagraphAnalyzer {

  private static final String LIST_PREFIX_CHARACTERS = "*#:;";
  private static final char PREFORMATTED_PREFIX = ' ';
  private static final String HORIZONTAL_RULE = "----";

  private final String text;
  private final LineAnalyzer lineAnalyzer;
  private final TextContainers containers;
  private final BlockTagMatcher blockTagMatcher;
  private final Set<Integer> titleBegins;
  private final List<ParagraphElement> paragraphs;
  private final List<PreformattedElement> preformattedBlocks;
  private Block block;
  private int blockBegin;
  private int blockEnd;
  private boolean insideBlockTag;

  ParagraphAnalyzer(
      final String text,
      final LineAnalyzer lineAnalyzer,
      final TextContainers containers,
      final BlockTagMatcher blockTagMatcher,
      final List<TitleElement> titles) {
    this.text = text;
    this.lineAnalyzer = lineAnalyzer;
    this.containers = containers;
    this.blockTagMatcher = blockTagMatcher;
    this.titleBegins = titles.stream().map(TitleElement::begin).collect(Collectors.toSet());
    this.paragraphs = new ArrayList<>();
    this.preformattedBlocks = new ArrayList<>();
    this.block = Block.NONE;
  }

  List<ParagraphElement> getParagraphs() {
    return paragraphs;
  }

  List<PreformattedElement> getPreformattedBlocks() {
    return preformattedBlocks;
  }

  void analyze(final List<TableElement> tables) {
    for (final LineAnalyzer.Line line : lineAnalyzer.getLines()) {
      if (containers.isWholeText(line.container())) {
        analyzeLine(line, tables);
      }
    }
    endBlock();
  }

  private void analyzeLine(final LineAnalyzer.Line line, final List<TableElement> tables) {
    final String content = text.substring(line.contentBegin(), line.end());
    if (content.isBlank() && line.contentBegin() > line.begin()) {
      return;
    }
    if (isOtherBlock(line, content, tables)) {
      endBlock();
      return;
    }
    final BlockTagMatcher.Match blockTagMatch =
        blockTagMatcher.match(line.contentBegin(), line.end());
    if (blockTagMatch == BlockTagMatcher.Match.NONE) {
      analyzeTextLine(line, content);
    } else {
      endBlock();
      insideBlockTag = blockTagMatch == BlockTagMatcher.Match.OPENING;
    }
  }

  private void analyzeTextLine(final LineAnalyzer.Line line, final String content) {
    if (insideBlockTag) {
      endBlock();
    } else if (isPreformatted(content)) {
      extendBlock(Block.PREFORMATTED, line);
    } else if (content.isBlank()) {
      endBlock();
    } else {
      extendBlock(Block.PARAGRAPH, line);
    }
  }

  private boolean isOtherBlock(
      final LineAnalyzer.Line line, final String content, final List<TableElement> tables) {
    return (!content.isEmpty() && LIST_PREFIX_CHARACTERS.indexOf(content.charAt(0)) >= 0)
        || content.startsWith(HORIZONTAL_RULE)
        || titleBegins.contains(line.contentBegin())
        || tables.stream()
            .anyMatch(table -> table.begin() < line.end() && line.contentBegin() < table.end());
  }

  private boolean isPreformatted(final String content) {
    return !content.isEmpty()
        && content.charAt(0) == PREFORMATTED_PREFIX
        && (block == Block.PREFORMATTED || !content.isBlank());
  }

  private void extendBlock(final Block lineBlock, final LineAnalyzer.Line line) {
    if (block != lineBlock) {
      endBlock();
      block = lineBlock;
      blockBegin = line.contentBegin();
    }
    blockEnd = line.end();
  }

  private void endBlock() {
    switch (block) {
      case PARAGRAPH -> paragraphs.add(new ParagraphElement(blockBegin, blockEnd));
      case PREFORMATTED -> preformattedBlocks.add(new PreformattedElement(blockBegin, blockEnd));
      case NONE -> {}
    }
    block = Block.NONE;
  }

  private enum Block {
    NONE,
    PARAGRAPH,
    PREFORMATTED
  }
}
