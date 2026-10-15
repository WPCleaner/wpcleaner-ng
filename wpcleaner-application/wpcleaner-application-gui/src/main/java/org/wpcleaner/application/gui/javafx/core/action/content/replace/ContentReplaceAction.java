package org.wpcleaner.application.gui.javafx.core.action.content.replace;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.Objects;
import javafx.scene.control.Control;
import javafx.scene.layout.Pane;
import org.wpcleaner.api.analysis.PageAnalysisFactory;
import org.wpcleaner.api.api.edit.ApiEditWrapper;
import org.wpcleaner.api.api.edit.EditQueryByTitle;
import org.wpcleaner.api.api.edit.EditQueryCommon;
import org.wpcleaner.api.wiki.definition.WikiDefinition;
import org.wpcleaner.application.gui.javafx.core.action.content.PageContentDisplay;
import org.wpcleaner.application.gui.javafx.core.pageanalysis.coloration.PageSyntaxColorizer;

public class ContentReplaceAction {

  private final ApiEditWrapper apiEditWrapper;
  private final WikiDefinition wiki;
  private final ContentReplaceConfiguration configuration;
  private final String pageTitle;
  private final ContentReplaceActionControl configurationControl;
  private final PageContentDisplay displayPane;

  public ContentReplaceAction(
      final PageSyntaxColorizer colorizer,
      final PageAnalysisFactory analysisFactory,
      final ApiEditWrapper apiEditWrapper,
      final WikiDefinition wiki,
      final String blockTitle,
      final ContentReplaceConfiguration configuration,
      final String pageTitle,
      final String pageContent) {
    this.apiEditWrapper = apiEditWrapper;
    this.wiki = wiki;
    this.configuration = configuration;
    this.pageTitle = Objects.requireNonNull(pageTitle, "pageTitle");
    this.configurationControl = new ContentReplaceActionControl(blockTitle, configuration);
    this.displayPane = new PageContentDisplay(colorizer, analysisFactory, pageTitle, pageContent);
  }

  public Control getConfigurationControl() {
    return configurationControl;
  }

  public Pane getDisplayPane() {
    return displayPane;
  }

  public boolean isComplete() {
    return configurationControl.isComplete();
  }

  public void execute() {
    apiEditWrapper.edit(
        wiki,
        new EditQueryByTitle(
            pageTitle,
            EditQueryCommon.emptyBuilder()
                .text(configuration.text())
                .summary(configurationControl.getSummary())
                .build()));
  }
}
