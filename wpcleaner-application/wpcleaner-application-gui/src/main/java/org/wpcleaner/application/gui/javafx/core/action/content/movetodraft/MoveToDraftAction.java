package org.wpcleaner.application.gui.javafx.core.action.content.movetodraft;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.Objects;
import javafx.scene.control.Control;
import javafx.scene.layout.Pane;
import org.wpcleaner.api.analysis.PageAnalysisFactory;
import org.wpcleaner.api.api.edit.ApiEditWrapper;
import org.wpcleaner.api.api.move.MoveQueryByTitle;
import org.wpcleaner.api.api.move.MoveQueryCommon;
import org.wpcleaner.api.api.query.prop.revisions.ApiRevisions;
import org.wpcleaner.api.utils.GT;
import org.wpcleaner.application.gui.javafx.core.action.content.PageContentDisplay;
import org.wpcleaner.application.gui.javafx.core.action.content.RetrievePageContentTask;
import org.wpcleaner.application.gui.javafx.core.pageanalysis.coloration.PageSyntaxColorizer;
import org.wpcleaner.application.gui.javafx.core.window.JavaFxWindow;
import org.wpcleaner.lib.image.ImageLoader;

public class MoveToDraftAction {

  private final JavaFxWindow<?> owner;
  private final ApiEditWrapper apiEditWrapper;
  private final ApiRevisions apiRevisions;
  private final PageAnalysisFactory analysisFactory;
  private final MoveToDraftContext context;
  private final MoveToDraftActionControl configurationControl;
  private final PageContentDisplay displayPane;

  public MoveToDraftAction(
      final JavaFxWindow<?> owner,
      final ImageLoader imageLoader,
      final PageSyntaxColorizer colorizer,
      final PageAnalysisFactory analysisFactory,
      final ApiEditWrapper apiEditWrapper,
      final ApiRevisions apiRevisions,
      final MoveToDraftConfiguration configuration,
      final MoveToDraftContext context) {
    this.owner = owner;
    this.apiEditWrapper = apiEditWrapper;
    this.apiRevisions = apiRevisions;
    this.analysisFactory = analysisFactory;
    this.context = context;
    this.configurationControl =
        new MoveToDraftActionControl(
            imageLoader, context.blockTitle(), configuration, this::refreshTargetContent);
    this.displayPane =
        new PageContentDisplay(
            colorizer, analysisFactory, context.subPageFullTitle(), context.pageContent());
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

  public String getSubPageName() {
    return configurationControl.getSubPageName().trim();
  }

  public void execute() {
    final String targetSubPageTitle =
        context.subPageFullTitle(configurationControl.getSubPageName());
    apiEditWrapper.move(
        context.wiki(),
        new MoveQueryByTitle(
            context.basePageTitle(),
            MoveQueryCommon.emptyBuilder()
                .to(targetSubPageTitle)
                .reason(configurationControl.getSummary())
                .moveTalk(false)
                .moveSubpages(false)
                .noRedirect(true)
                .ignoreWarnings(true)
                .build()));
  }

  private void refreshTargetContent() {
    final String subPageName = configurationControl.getSubPageName();
    if (subPageName.isBlank()) {
      owner.showError(GT._T("Please enter a sub-page title"));
      return;
    }
    final String targetSubPageTitle = context.subPageFullTitle(subPageName);
    owner.executeAsync(
        new RetrievePageContentTask(context.wiki(), targetSubPageTitle, apiRevisions),
        content -> displayPane.updateText(targetSubPageTitle, content, analysisFactory),
        e ->
            owner.showError(
                GT._T(
                    "Error loading page content: %s",
                    Objects.requireNonNullElseGet(
                        e.getMessage(), () -> e.getClass().getSimpleName()))));
  }
}
