package org.wpcleaner.lib.check.formatting;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import org.springframework.stereotype.Component;
import org.wpcleaner.api.analysis.PageAnalysis;

@Component
public class EndOfArticleFix implements FormattingFix {

  @Override
  public String fix(final PageAnalysis analysis) {
    final String text = analysis.getText();
    final int contentEnd = FormattingText.skipWhitespaceBefore(text, text.length());
    if (contentEnd == 0 || contentEnd >= text.length() - 1) {
      return text;
    }
    return text.substring(0, contentEnd)
        + (FormattingText.countLineEnds(text, contentEnd, text.length()) > 0
            ? FormattingText.LINE_END
            : "");
  }
}
