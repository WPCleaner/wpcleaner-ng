package org.wpcleaner.application.gui.javafx.analysis;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.time.Instant;
import java.util.List;
import java.util.Set;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.jspecify.annotations.Nullable;
import org.wpcleaner.api.analysis.PageAnalysis;
import org.wpcleaner.api.api.ApiException;
import org.wpcleaner.api.api.edit.Edit;
import org.wpcleaner.api.api.edit.EditQueryByTitle;
import org.wpcleaner.api.api.edit.EditQueryCommon;
import org.wpcleaner.api.api.query.Slot;
import org.wpcleaner.api.api.query.prop.revisions.Page;
import org.wpcleaner.api.api.query.prop.revisions.Revision;
import org.wpcleaner.api.api.query.prop.revisions.RevisionSlot;
import org.wpcleaner.api.api.query.prop.revisions.RevisionsParameters;
import org.wpcleaner.api.api.query.prop.revisions.RevisionsQuery;
import org.wpcleaner.api.progress.LongRunningTask;
import org.wpcleaner.api.progress.ProgressStep;
import org.wpcleaner.api.progress.ProgressTracker;
import org.wpcleaner.api.utils.GT;
import org.wpcleaner.api.wiki.definition.WikiDefinition;
import org.wpcleaner.application.gui.javafx.JavaFxImageLoader;
import org.wpcleaner.application.gui.javafx.core.pageanalysis.PageAnalysisCheckPane;
import org.wpcleaner.application.gui.javafx.core.window.JavaFxWindow;
import org.wpcleaner.lib.check.CheckAlgorithm;

public final class PageAnalysisPanel extends StackPane {

  private final JavaFxWindow<?> owner;
  private final JavaFxAnalysisWindowServices services;
  private final String pageName;
  @Nullable private final Integer algorithmNumber;
  private final PageAnalysisCheckPane checkPane;
  private final PageSavePane savePane;
  @Nullable private Instant baseTimestamp;

  public PageAnalysisPanel(
      final JavaFxWindow<?> owner,
      final JavaFxAnalysisWindowServices services,
      final String pageName,
      @Nullable final Integer algorithmNumber) {
    this.owner = owner;
    this.services = services;
    this.pageName = pageName;
    this.algorithmNumber = algorithmNumber;
    final JavaFxImageLoader imageLoader = new JavaFxImageLoader(services.imageLoader());
    this.checkPane =
        new PageAnalysisCheckPane(
            services.colorizer(),
            imageLoader,
            services.checkAlgorithmRegistry(),
            services.pageAnalysisFactory(),
            services.actionServices()::browse);
    this.savePane =
        new PageSavePane(imageLoader, services.user().getCurrentUser().demo(), this::savePage);
    initialize();
  }

  private void initialize() {
    checkPane.getArea().setEditable(true);
    checkPane.setAnalysisListener(this::updateSummary);
    final VBox mainContainer = new VBox();
    VBox.setVgrow(checkPane, Priority.ALWAYS);
    mainContainer.getChildren().addAll(savePane, checkPane);
    getChildren().add(mainContainer);

    loadPageContent();
  }

  private WikiDefinition getWiki() {
    return services.user().getCurrentUser().wiki();
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
                .properties(
                    Set.of(
                        RevisionsParameters.Properties.CONTENT,
                        RevisionsParameters.Properties.TIMESTAMP))
                .slots(Set.of(Slot.MAIN))
                .build();
        return services
            .apiRevisions()
            .retrieveRevisionsByTitle(getWiki(), List.of(pageName), query);
      }
    }
  }

  private void updateAnalysisArea(final List<Page> pages) {
    if (pages.isEmpty()) {
      owner.showWarning(GT._T("Error loading the page"), GT._T("Page not found."));
      return;
    }

    final Page page = pages.getFirst();
    if (page.revisions().isEmpty()) {
      owner.showWarning(
          GT._T("Error loading the page"), GT._T("No revisions found for this page."));
      return;
    }

    final Revision revision = page.revisions().getFirst();
    final RevisionSlot slot = revision.mainSlot().orElse(null);
    if (slot == null || slot.content() == null) {
      owner.showWarning(GT._T("Error loading the page"), GT._T("No content found for this page."));
      return;
    }

    baseTimestamp = revision.timestamp();
    checkPane.updateAnalysis(
        services.pageAnalysisFactory().analysis(getWiki(), pageName, slot.content()));
    resetInitialErrors();
    if (algorithmNumber != null) {
      checkPane.selectAlgorithm(algorithmNumber);
    }
  }

  private void resetInitialErrors() {
    checkPane.resetInitialErrors();
    savePane.resetSummary();
    updateSummary();
  }

  private void updateSummary() {
    savePane.updateAutomaticSummary(
        services.checkWikiSummary().create(getWiki(), checkPane.getFixedAlgorithms()));
  }

  private void savePage() {
    checkPane.refreshAnalysis();
    final PageAnalysis analysis = checkPane.getAnalysis();
    final List<CheckAlgorithm> fixedAlgorithms = checkPane.getFullyFixedAlgorithms();
    owner.executeAsync(
        new SavePageTask(checkPane.getArea().getText(), savePane.getSummary()),
        edit -> pageSaved(edit, analysis, fixedAlgorithms),
        this::pageSavedWithError);
  }

  private final class SavePageTask implements LongRunningTask<Edit> {

    private final String text;
    private final String summary;

    private SavePageTask(final String text, final String summary) {
      this.text = text;
      this.summary = summary;
    }

    @Override
    public Edit call(final ProgressTracker tracker) {
      try (ProgressStep _ = tracker.start(GT._T("Saving page"))) {
        return services
            .apiEdit()
            .edit(
                getWiki(),
                new EditQueryByTitle(
                    pageName,
                    EditQueryCommon.emptyBuilder()
                        .text(text)
                        .summary(summary)
                        .baseTimestamp(baseTimestamp)
                        .noCreate(true)
                        .build()));
      }
    }
  }

  private void pageSaved(
      final Edit edit,
      @Nullable final PageAnalysis analysis,
      final List<CheckAlgorithm> fixedAlgorithms) {
    if (!Edit.RESULT_SUCCESS.equals(edit.result())) {
      owner.showWarning(
          GT._T("Error saving the page"), GT._T("Result of the edit: %s", edit.result()));
      return;
    }
    if (edit.newTimestamp() != null) {
      baseTimestamp = edit.newTimestamp();
    }
    resetInitialErrors();
    if (analysis != null && !fixedAlgorithms.isEmpty()) {
      owner.executeAsync(
          new MarkAsFixedTask(analysis, fixedAlgorithms), _ -> {}, this::markedAsFixedWithError);
    }
  }

  private final class MarkAsFixedTask implements LongRunningTask<List<CheckAlgorithm>> {

    private final PageAnalysis analysis;
    private final List<CheckAlgorithm> algorithms;

    private MarkAsFixedTask(final PageAnalysis analysis, final List<CheckAlgorithm> algorithms) {
      this.analysis = analysis;
      this.algorithms = List.copyOf(algorithms);
    }

    @Override
    public List<CheckAlgorithm> call(final ProgressTracker tracker) {
      try (ProgressStep _ = tracker.start(GT._T("Marking errors as fixed"))) {
        return services.checkWikiServer().markAsFixed(analysis, algorithms);
      }
    }
  }

  private void markedAsFixedWithError(final Exception e) {
    owner.showWarning(
        GT._T("Error marking errors as fixed"),
        GT._T("Error marking errors as fixed: %s", String.valueOf(e.getMessage())));
  }

  private void pageSavedWithError(final Exception e) {
    final String message = String.valueOf(e.getMessage());
    owner.showWarning(
        GT._T("Error saving the page"),
        GT._T(
            "Error saving the page: %s",
            e instanceof ApiException apiException
                ? "%s%n%s".formatted(message, apiException.getDetails())
                : message));
  }

  private void updateAnalysisAreaWithError(final Exception e) {
    owner.showWarning(
        GT._T("Error loading the page"),
        GT._T("Error retrieving page content: %s", String.valueOf(e.getMessage())));
  }
}
