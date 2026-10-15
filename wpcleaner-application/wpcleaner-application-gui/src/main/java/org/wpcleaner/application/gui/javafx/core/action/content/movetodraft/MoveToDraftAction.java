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
import org.wpcleaner.api.wiki.definition.WikiDefinition;
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
  private final WikiDefinition wiki;
  private final String basePageTitle;
  private final MoveToDraftActionControl configurationControl;
  private final PageContentDisplay displayPane;

  @SuppressWarnings("PMD.ExcessiveParameterList")
  public MoveToDraftAction(
      final JavaFxWindow<?> owner,
      final ImageLoader imageLoader,
      final PageSyntaxColorizer colorizer,
      final PageAnalysisFactory analysisFactory,
      final ApiEditWrapper apiEditWrapper,
      final ApiRevisions apiRevisions,
      final WikiDefinition wiki,
      final String blockTitle,
      final MoveToDraftConfiguration configuration,
      final String basePageTitle,
      final String subPageTitle,
      final String pageContent) {
    this.owner = owner;
    this.apiEditWrapper = apiEditWrapper;
    this.apiRevisions = apiRevisions;
    this.analysisFactory = analysisFactory;
    this.wiki = wiki;
    this.basePageTitle = Objects.requireNonNull(basePageTitle, "basePageTitle");
    this.configurationControl =
        new MoveToDraftActionControl(
            imageLoader, blockTitle, configuration, this::refreshTargetContent);
    this.displayPane =
        new PageContentDisplay(
            colorizer, analysisFactory, basePageTitle + "/" + subPageTitle, pageContent);
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
        basePageTitle + "/" + configurationControl.getSubPageName().trim();
    apiEditWrapper.move(
        wiki,
        new MoveQueryByTitle(
            basePageTitle,
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
    final String subPageName = configurationControl.getSubPageName().trim();
    if (subPageName.isBlank()) {
      owner.showError(GT._T("Please enter a sub-page title"));
      return;
    }
    final String targetSubPageTitle = basePageTitle + "/" + subPageName;
    owner.executeAsync(
        new RetrievePageContentTask(wiki, targetSubPageTitle, apiRevisions),
        content -> displayPane.updateText(targetSubPageTitle, content, analysisFactory),
        e ->
            owner.showError(
                GT._T(
                    "Error loading page content: %s",
                    Objects.requireNonNullElseGet(
                        e.getMessage(), () -> e.getClass().getSimpleName()))));
  }
}
