package org.wpcleaner.application.gui.javafx.analysis;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import java.util.Set;
import javafx.scene.control.Alert;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.wpcleaner.api.api.query.prop.revisions.Page;
import org.wpcleaner.api.api.query.prop.revisions.Revision;
import org.wpcleaner.api.api.query.prop.revisions.RevisionSlot;
import org.wpcleaner.api.api.query.prop.revisions.RevisionsParameters;
import org.wpcleaner.api.api.query.prop.revisions.RevisionsQuery;
import org.wpcleaner.api.progress.LongRunningTask;
import org.wpcleaner.api.progress.ProgressStep;
import org.wpcleaner.api.progress.ProgressTracker;
import org.wpcleaner.api.utils.GT;
import org.wpcleaner.application.gui.javafx.core.pageanalysis.PageAnalysisArea;
import org.wpcleaner.application.gui.javafx.core.pageanalysis.PageAnalysisScrollPane;
import org.wpcleaner.application.gui.javafx.core.window.JavaFxWindow;

public final class PageAnalysisPanel extends StackPane {

  private final JavaFxWindow<?> owner;
  private final JavaFxAnalysisWindowServices services;
  private final String pageName;
  private final PageAnalysisScrollPane scrollPane;
  private final PageAnalysisArea analysisArea;

  public PageAnalysisPanel(
      final JavaFxWindow<?> owner,
      final JavaFxAnalysisWindowServices services,
      final String pageName) {
    this.owner = owner;
    this.services = services;
    this.pageName = pageName;
    this.scrollPane = new PageAnalysisScrollPane(services.colorizer());
    this.analysisArea = scrollPane.getArea();
    this.analysisArea.setEditable(true);
    initialize();
  }

  private void initialize() {
    final VBox mainContainer = new VBox();
    VBox.setVgrow(scrollPane, Priority.ALWAYS);
    mainContainer.getChildren().add(scrollPane);
    getChildren().add(mainContainer);

    loadPageContent();
  }

  private void loadPageContent() {
    owner.executeAsync(
        new LoadPageContentTask(), this::updateAnalysisArea, this::updateAnalysisAreaWithError);
  }

  private final class LoadPageContentTask implements LongRunningTask<List<Page>> {

    @Override
    public List<Page> call(final ProgressTracker tracker) {
      try (ProgressStep _ = tracker.start(GT._T("Retrieving page content"))) {
        final RevisionsQuery query =
            RevisionsQuery.emptyBuilder()
                .properties(Set.of(RevisionsParameters.Properties.CONTENT))
                .slots(Set.of("main"))
                .build();
        return services
            .apiRevisions()
            .retrieveRevisionsByTitle(
                services.user().getCurrentUser().wiki(), List.of(pageName), query);
      }
    }
  }

  private void updateAnalysisArea(final List<Page> pages) {
    if (pages.isEmpty()) {
      showWarning(GT._T("Error loading the page"), GT._T("Page not found."));
      return;
    }

    final Page page = pages.getFirst();
    if (page.revisions().isEmpty()) {
      showWarning(GT._T("Error loading the page"), GT._T("No revisions found for this page."));
      return;
    }

    final Revision revision = page.revisions().getFirst();
    final RevisionSlot slot = revision.slots().get("main");
    if (slot == null || slot.content() == null) {
      showWarning(GT._T("Error loading the page"), GT._T("No content found for this page."));
      return;
    }

    analysisArea.updateAnalysis(services.pageAnalysisFactory().analysis(pageName, slot.content()));
  }

  private void updateAnalysisAreaWithError(final Exception e) {
    showWarning(
        GT._T("Error loading the page"),
        GT._T("Error retrieving page content: %s", String.valueOf(e.getMessage())));
  }

  private void showWarning(final String title, final String content) {
    final Alert alert = new Alert(Alert.AlertType.WARNING);
    alert.setTitle(title);
    alert.setHeaderText(null);
    alert.setContentText(content);
    alert.showAndWait();
  }
}
