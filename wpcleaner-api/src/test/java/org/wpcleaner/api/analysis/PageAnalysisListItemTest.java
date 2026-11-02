package org.wpcleaner.api.analysis;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.wpcleaner.api.analysis.listitem.ListItemDefinition;
import org.wpcleaner.api.analysis.listitem.ListItemElement;

class PageAnalysisListItemTest {

  @DisplayName("should analyze list items with their prefix")
  @Test
  void analyzeListItems() {
    // GIVEN
    final String text = "* a\n** b\n#: c\n; d";

    // WHEN
    final PageAnalysis analysis = PageAnalysisTestFactory.createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getListItems())
        .containsExactly(
            new ListItemElement(0, 3, "*", "a"),
            new ListItemElement(4, 8, "**", "b"),
            new ListItemElement(9, 13, "#:", "c"),
            new ListItemElement(14, 17, ";", "d"));
  }

  @DisplayName("should not analyze list items not at line start")
  @Test
  void analyzeNotListItems() {
    // GIVEN
    final String text = "x * a\n * b\n<nowiki>\n* c\n</nowiki>\n<!--\n* d\n-->";

    // WHEN
    final PageAnalysis analysis = PageAnalysisTestFactory.createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getListItems()).isEmpty();
  }

  @DisplayName("should continue list item over new lines inside templates, tags and comments")
  @Test
  void analyzeListItemWithNewLinesInsideElements() {
    // GIVEN
    final String text = "* {{T\n|a}} b<ref>c\nd</ref><!--\n-->e\n* f";

    // WHEN
    final PageAnalysis analysis = PageAnalysisTestFactory.createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getListItems())
        .containsExactly(
            new ListItemElement(0, 35, "*", "{{T\n|a}} b<ref>c\nd</ref><!--\n-->e"),
            new ListItemElement(36, 39, "*", "f"));
  }

  @DisplayName("should end list item at the end of the template parameter")
  @Test
  void analyzeListItemsInsideTemplateParameter() {
    // GIVEN
    final String text = "{{T|\n* a\n* b|c}}";

    // WHEN
    final PageAnalysis analysis = PageAnalysisTestFactory.createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getListItems())
        .containsExactly(new ListItemElement(5, 8, "*", "a"), new ListItemElement(9, 12, "*", "b"));
  }

  @DisplayName("should end list item at the end of the tag content")
  @Test
  void analyzeListItemInsideTag() {
    // GIVEN
    final String text = "<ref>\n* a</ref>";

    // WHEN
    final PageAnalysis analysis = PageAnalysisTestFactory.createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getListItems())
        .containsExactly(new ListItemElement(6, 9, "*", "a"));
  }

  @DisplayName("should analyze list item after a comment at line start")
  @Test
  void analyzeListItemAfterComment() {
    // GIVEN
    final String text = "<!-- c -->* item";

    // WHEN
    final PageAnalysis analysis = PageAnalysisTestFactory.createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getListItems())
        .containsExactly(new ListItemElement(10, 16, "*", "item"));
  }

  @DisplayName("should split a term and its definition")
  @Test
  void analyzeDefinition() {
    // GIVEN
    final String text = "; a : b\n; c";

    // WHEN
    final PageAnalysis analysis = PageAnalysisTestFactory.createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getListItems())
        .containsExactly(
            new ListItemElement(0, 7, ";", "a", new ListItemDefinition(4, 7, "b")),
            new ListItemElement(8, 11, ";", "c"));
  }

  @DisplayName("should not split a term on colons inside other elements")
  @Test
  void analyzeDefinitionAfterElementsWithColons() {
    // GIVEN
    final String text =
        "; [[a:b]] [http://x.org c:d] http://y.org/ <span>e:f</span>"
            + "<!-- g:h --><nowiki>:</nowiki>{{T|i:j}}<ref>k:l</ref> : m";

    // WHEN
    final PageAnalysis analysis = PageAnalysisTestFactory.createPageAnalysis(text);

    // THEN
    final int colon = text.lastIndexOf(':');
    Assertions.assertThat(analysis.getListItems())
        .containsExactly(
            new ListItemElement(
                0,
                text.length(),
                ";",
                text.substring(2, colon).trim(),
                new ListItemDefinition(colon, text.length(), "m")));
  }

  @DisplayName("should split a term opened anywhere in the prefix, like MediaWiki")
  @Test
  void analyzeDefinitionWithLongerPrefixes() {
    // GIVEN
    final String text = ";: a : b\n;: c : d\n*; e : f\n* g : h";

    // WHEN
    final PageAnalysis analysis = PageAnalysisTestFactory.createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getListItems())
        .containsExactly(
            new ListItemElement(0, 8, ";:", "a", new ListItemDefinition(5, 8, "b")),
            new ListItemElement(9, 17, ";:", "c : d"),
            new ListItemElement(18, 26, "*;", "e", new ListItemDefinition(23, 26, "f")),
            new ListItemElement(27, 34, "*", "g : h"));
  }
}
