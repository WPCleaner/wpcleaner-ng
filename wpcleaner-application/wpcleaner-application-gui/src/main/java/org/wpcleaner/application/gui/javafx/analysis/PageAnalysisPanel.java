package org.wpcleaner.application.gui.javafx.analysis;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import java.util.Set;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.wpcleaner.api.api.query.prop.revisions.Page;
import org.wpcleaner.api.api.query.prop.revisions.Revision;
import org.wpcleaner.api.api.query.prop.revisions.RevisionsParameters;
import org.wpcleaner.api.api.query.prop.revisions.RevisionsQuery;
import org.wpcleaner.api.utils.GT;
import org.wpcleaner.application.base.processor.ProgressStep;
import org.wpcleaner.application.gui.javafx.JavaFxProgressTracker;

public final class PageAnalysisPanel extends StackPane {

  private final JavaFxAnalysisWindowServices services;
  private final String pageName;
  private final PageAnalysisArea analysisArea;
  private final BooleanProperty loading = new SimpleBooleanProperty(true);

  public PageAnalysisPanel(final JavaFxAnalysisWindowServices services, final String pageName) {
    this.services = services;
    this.pageName = pageName;
    this.analysisArea = new PageAnalysisArea(services.colorizer());
    initialize();
  }

  private void initialize() {
    final VBox mainContainer = new VBox();
    final VirtualizedScrollPane<PageAnalysisArea> scrollPane =
        new VirtualizedScrollPane<>(analysisArea);
    VBox.setVgrow(scrollPane, Priority.ALWAYS);
    mainContainer.getChildren().add(scrollPane);
    getChildren().add(mainContainer);

    loadPageContent();
  }

  private void loadPageContent() {
    final JavaFxProgressTracker progressTracker = JavaFxProgressTracker.forObservable(loading);
    getChildren().add(progressTracker.getProgressOverlay());

    final Thread thread = new Thread(() -> doLoadPageContent(progressTracker));
    thread.setDaemon(true);
    thread.start();
  }

  @SuppressWarnings("PMD.AvoidCatchingGenericException")
  private void doLoadPageContent(final JavaFxProgressTracker progressTracker) {
    try (ProgressStep _ = progressTracker.start(GT._T("Retrieving page content"))) {
      final RevisionsQuery query =
          RevisionsQuery.emptyBuilder()
              .properties(Set.of(RevisionsParameters.Properties.CONTENT))
              .slots(Set.of("main"))
              .build();

      final List<Page> pages =
          services
              .apiRevisions()
              .retrieveRevisionsByTitle(
                  services.user().getCurrentUser().wiki(), List.of(pageName), query);

      Platform.runLater(
          () -> {
            updateAnalysisArea(pages);
            finishLoading(progressTracker);
          });
    } catch (final Exception e) {
      Platform.runLater(
          () -> {
            updateAnalysisAreaWithError(e);
            finishLoading(progressTracker);
          });
    }
  }

  private void updateAnalysisArea(final List<Page> pages) {
    if (pages.isEmpty()) {
      analysisArea.replaceText(GT._T("Page not found."));
      return;
    }

    final Page page = pages.getFirst();
    if (page.revisions().isEmpty()) {
      analysisArea.replaceText(GT._T("No revisions found for this page."));
      return;
    }

    final Revision revision = page.revisions().getFirst();
    if (!revision.slots().containsKey("main")) {
      analysisArea.replaceText(GT._T("No content found for this page."));
      return;
    }

    analysisArea.replaceText(revision.slots().get("main").content());
  }

  private void updateAnalysisAreaWithError(final Exception e) {
    analysisArea.replaceText(
        GT._T("Error retrieving page content: %s", String.valueOf(e.getMessage())));
  }

  private void finishLoading(final JavaFxProgressTracker progressTracker) {
    getChildren().remove(progressTracker.getProgressOverlay());
    loading.set(false);
  }
}
