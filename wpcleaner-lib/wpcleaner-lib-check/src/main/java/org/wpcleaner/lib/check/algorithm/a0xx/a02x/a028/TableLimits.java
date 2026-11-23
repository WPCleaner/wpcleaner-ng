package org.wpcleaner.lib.check.algorithm.a0xx.a02x.a028;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.wpcleaner.api.analysis.PageAnalysis;
import org.wpcleaner.api.analysis.pagetitle.PageTitle;
import org.wpcleaner.api.analysis.tag.CompleteTagElement;
import org.wpcleaner.api.analysis.tag.HtmlTagType;
import org.wpcleaner.api.analysis.tag.TagKind;
import org.wpcleaner.lib.check.algorithm.TextExclusions;

record TableLimits(List<TableLimit> starts, List<TableLimit> ends) {

  private static final String WIKI_START = "{|";
  private static final String WIKI_END = "|}";
  private static final char OPENING_BRACE = '{';
  private static final int TEMPLATE_CLOSING_LENGTH = 3;
  private static final int PARAMETER_CLOSING_LENGTH = 4;
  private static final Comparator<TableLimit> ORDER = Comparator.comparingInt(TableLimit::begin);

  TableLimits {
    starts = List.copyOf(starts);
    ends = List.copyOf(ends);
  }

  static TableLimits find(final PageAnalysis analysis, final Set<PageTitle> endTemplates) {
    final TextExclusions exclusions = TextExclusions.of(analysis);
    final List<CompleteTagElement> codeTags =
        analysis.getCompleteTags().stream()
            .filter(
                tag -> HtmlTagType.CODE.getNormalizedName().equals(tag.startTag().normalizedName()))
            .toList();
    final Area area = new Area(exclusions, codeTags);
    final String text = analysis.getText();
    final List<TableLimit> starts =
        Stream.concat(
                findWikiLimits(text, WIKI_START)
                    .filter(index -> index == 0 || text.charAt(index - 1) != OPENING_BRACE)
                    .filter(area::isAnalyzed)
                    .mapToObj(index -> new TableLimit(index, index + WIKI_START.length())),
                findTableTags(analysis, TagKind.START, area))
            .sorted(ORDER)
            .toList();
    final List<TableLimit> ends =
        Stream.of(
                findWikiLimits(text, WIKI_END)
                    .filter(area::isAnalyzed)
                    .filter(index -> !isTemplateClosing(analysis, index))
                    .mapToObj(index -> new TableLimit(index, index + WIKI_END.length())),
                findTableTags(analysis, TagKind.END, area),
                analysis.getTemplates().stream()
                    .filter(template -> endTemplates.contains(template.title()))
                    .map(template -> new TableLimit(template.begin(), template.end())))
            .flatMap(limits -> limits)
            .toList();
    return new TableLimits(starts, ends);
  }

  private static IntStream findWikiLimits(final String text, final String limit) {
    return IntStream.iterate(
        text.indexOf(limit), index -> index >= 0, index -> text.indexOf(limit, index + 1));
  }

  private static Stream<TableLimit> findTableTags(
      final PageAnalysis analysis, final TagKind kind, final Area area) {
    return analysis.getTags().stream()
        .filter(tag -> tag.kind() == kind)
        .filter(tag -> HtmlTagType.TABLE.getNormalizedName().equals(tag.normalizedName()))
        .filter(tag -> area.isAnalyzed(tag.begin()))
        .map(tag -> new TableLimit(tag.begin(), tag.end()));
  }

  private static boolean isTemplateClosing(final PageAnalysis analysis, final int index) {
    return analysis.getTemplates().stream()
            .anyMatch(template -> template.end() == index + TEMPLATE_CLOSING_LENGTH)
        || analysis.getFunctions().stream()
            .anyMatch(function -> function.end() == index + TEMPLATE_CLOSING_LENGTH)
        || analysis.getParameters().stream()
            .anyMatch(parameter -> parameter.end() == index + PARAMETER_CLOSING_LENGTH);
  }

  private record Area(TextExclusions exclusions, List<CompleteTagElement> codeTags) {
    boolean isAnalyzed(final int index) {
      return !exclusions.contains(index)
          && codeTags.stream().noneMatch(tag -> tag.begin() <= index && index < tag.end());
    }
  }
}
