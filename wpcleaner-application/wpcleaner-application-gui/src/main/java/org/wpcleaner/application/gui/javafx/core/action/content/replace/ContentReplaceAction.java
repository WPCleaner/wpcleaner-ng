package org.wpcleaner.application.gui.javafx.core.action.content.replace;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import javafx.scene.control.Control;
import javafx.scene.layout.Pane;
import org.wpcleaner.api.analysis.PageAnalysisFactory;
import org.wpcleaner.api.api.edit.ApiEditWrapper;
import org.wpcleaner.api.api.edit.EditQueryByTitle;
import org.wpcleaner.api.api.edit.EditQueryCommon;
import org.wpcleaner.application.gui.javafx.core.action.content.PageContentDisplay;
import org.wpcleaner.application.gui.javafx.core.pageanalysis.coloration.PageSyntaxColorizer;

public class ContentReplaceAction {

  private final ApiEditWrapper apiEditWrapper;
  private final ContentReplaceConfiguration configuration;
  private final ContentReplaceContext context;
  private final ContentReplaceActionControl configurationControl;
  private final PageContentDisplay displayPane;

  public ContentReplaceAction(
      final PageSyntaxColorizer colorizer,
      final PageAnalysisFactory analysisFactory,
      final ApiEditWrapper apiEditWrapper,
      final ContentReplaceConfiguration configuration,
      final ContentReplaceContext context) {
    this.apiEditWrapper = apiEditWrapper;
    this.configuration = configuration;
    this.context = context;
    this.configurationControl =
        new ContentReplaceActionControl(context.blockTitle(), configuration);
    this.displayPane =
        new PageContentDisplay(
            colorizer, analysisFactory, context.pageTitle(), context.pageContent());
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
        context.wiki(),
        new EditQueryByTitle(
            context.pageTitle(),
            EditQueryCommon.emptyBuilder()
                .text(configuration.text())
                .summary(configurationControl.getSummary())
                .build()));
  }
}
