package org.wpcleaner.api.analysis;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.wpcleaner.api.analysis.table.TableCaption;
import org.wpcleaner.api.analysis.table.TableCell;
import org.wpcleaner.api.analysis.table.TableElement;
import org.wpcleaner.api.analysis.table.TableRow;

class PageAnalysisTableTest {

  @DisplayName("should analyze table with caption, header cells, rows and cell attributes")
  @Test
  void analyzeTable() {
    // GIVEN
    final String text =
        "{| class=\"wikitable\"\n|+ Caption\n! A !! B\n|-\n| a || style=\"x\" | b\n|}";

    // WHEN
    final PageAnalysis analysis = PageAnalysisTestFactory.createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getTables())
        .containsExactly(
            new TableElement(
                0,
                text.length(),
                "class=\"wikitable\"",
                new TableCaption(21, 31, 23, null, "Caption"),
                List.of(
                    new TableRow(
                        32,
                        40,
                        "",
                        List.of(
                            new TableCell(32, 36, 33, true, null, "A"),
                            new TableCell(36, 40, 38, true, null, "B"))),
                    new TableRow(
                        41,
                        64,
                        "",
                        List.of(
                            new TableCell(44, 48, 45, false, null, "a"),
                            new TableCell(48, 64, 62, false, "style=\"x\"", "b"))))));
  }

  @DisplayName("should analyze nested table inside a multi-line cell")
  @Test
  void analyzeNestedTable() {
    // GIVEN
    final String text = "{|\n| a\nb\n{|\n| c\n|}\n| d\n|}";

    // WHEN
    final PageAnalysis analysis = PageAnalysisTestFactory.createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getTables())
        .containsExactly(
            new TableElement(
                0,
                text.length(),
                "",
                null,
                List.of(
                    new TableRow(
                        3,
                        22,
                        "",
                        List.of(
                            new TableCell(3, 18, 4, false, null, "a\nb\n{|\n| c\n|}"),
                            new TableCell(19, 22, 20, false, null, "d"))))),
            new TableElement(
                9,
                18,
                "",
                null,
                List.of(
                    new TableRow(
                        12, 15, "", List.of(new TableCell(12, 15, 13, false, null, "c"))))));
  }

  @DisplayName("should not split cells at pipes inside templates and links")
  @Test
  void analyzeTableWithTemplateAndLink() {
    // GIVEN
    final String text = "{|\n| {{T|a||b}} || [[x|y]]\n|}";

    // WHEN
    final PageAnalysis analysis = PageAnalysisTestFactory.createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getTables().getFirst().rows())
        .containsExactly(
            new TableRow(
                3,
                26,
                "",
                List.of(
                    new TableCell(3, 16, 4, false, null, "{{T|a||b}}"),
                    new TableCell(16, 26, 18, false, null, "[[x|y]]"))));
  }

  @DisplayName("should analyze unclosed table inside a tag")
  @Test
  void analyzeUnclosedTableInsideTag() {
    // GIVEN
    final String text = "<ref>\n{|\n| a\n</ref>";

    // WHEN
    final PageAnalysis analysis = PageAnalysisTestFactory.createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getTables())
        .containsExactly(
            new TableElement(
                6,
                12,
                "",
                null,
                List.of(
                    new TableRow(9, 12, "", List.of(new TableCell(9, 12, 10, false, null, "a"))))));
  }

  @DisplayName("should analyze indented table and ignore table syntax outside tables")
  @Test
  void analyzeIndentedTable() {
    // GIVEN
    final String text = "| a\n|}\n: {|\n|}";

    // WHEN
    final PageAnalysis analysis = PageAnalysisTestFactory.createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getTables())
        .containsExactly(new TableElement(9, text.length(), "", null, List.of()));
  }
}
