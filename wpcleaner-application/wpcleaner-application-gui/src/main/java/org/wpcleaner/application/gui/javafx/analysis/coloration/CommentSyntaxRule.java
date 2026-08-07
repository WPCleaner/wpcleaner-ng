package org.wpcleaner.application.gui.javafx.analysis.coloration;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.regex.Pattern;
import org.springframework.stereotype.Component;
import org.wpcleaner.application.gui.core.style.PageAnalysisStylePropertiesInitializer;

@Component
public class CommentSyntaxRule implements PageSyntaxRule {

  private static final Pattern PATTERN = Pattern.compile("(?s)<!--.*?-->");

  @Override
  public String getGroupName() {
    return "COMMENT";
  }

  @Override
  public Pattern getPattern() {
    return PATTERN;
  }

  @Override
  public String getStyleName() {
    return PageAnalysisStylePropertiesInitializer.COMMENT;
  }
}
