package org.wpcleaner.application.gui.javafx.core.pageanalysis.coloration;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;
import org.wpcleaner.api.analysis.PageAnalysis;
import org.wpcleaner.api.analysis.table.TableCaption;
import org.wpcleaner.api.analysis.table.TableCell;
import org.wpcleaner.api.analysis.table.TableElement;
import org.wpcleaner.api.analysis.table.TableRow;
import org.wpcleaner.application.gui.core.style.PageAnalysisStylePropertiesInitializer;

@Component
public class TableSyntaxRule implements PageSyntaxRule {

  private static final String ROW = "|-";
  private static final String TABLE_END = "|}";
  private static final char NEW_LINE = '\n';

  @Override
  public String getStyleName() {
    return PageAnalysisStylePropertiesInitializer.TABLE;
  }

  @Override
  public List<RuleRange> getRanges(final PageAnalysis pageAnalysis) {
    final String text = pageAnalysis.getText();
    final List<RuleRange> ranges = new ArrayList<>();
    for (final TableElement table : pageAnalysis.getTables()) {
      ranges.add(new RuleRange(table.begin(), findLineEnd(text, table.begin(), table.end())));
      final TableCaption caption = table.caption();
      if (caption != null) {
        ranges.add(new RuleRange(caption.begin(), caption.contentBegin()));
      }
      for (final TableRow row : table.rows()) {
        if (text.startsWith(ROW, row.begin())) {
          ranges.add(new RuleRange(row.begin(), findLineEnd(text, row.begin(), row.end())));
        }
        for (final TableCell cell : row.cells()) {
          ranges.add(new RuleRange(cell.begin(), cell.contentBegin()));
        }
      }
      final int tableEndBegin = table.end() - TABLE_END.length();
      if (tableEndBegin >= table.begin() && text.startsWith(TABLE_END, tableEndBegin)) {
        ranges.add(new RuleRange(tableEndBegin, table.end()));
      }
    }
    return ranges;
  }

  private static int findLineEnd(final String text, final int begin, final int end) {
    final int newLine = text.indexOf(NEW_LINE, begin);
    return newLine < 0 ? end : Math.min(newLine, end);
  }
}
