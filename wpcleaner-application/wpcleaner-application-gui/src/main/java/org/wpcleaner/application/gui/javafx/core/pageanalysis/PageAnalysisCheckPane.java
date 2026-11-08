package org.wpcleaner.application.gui.javafx.core.pageanalysis;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import java.util.function.Consumer;
import javafx.animation.PauseTransition;
import javafx.scene.control.SplitPane;
import javafx.scene.input.ContextMenuEvent;
import javafx.util.Duration;
import org.jspecify.annotations.Nullable;
import org.wpcleaner.api.analysis.PageAnalysis;
import org.wpcleaner.api.analysis.PageAnalysisFactory;
import org.wpcleaner.application.gui.javafx.JavaFxImageLoader;
import org.wpcleaner.application.gui.javafx.core.pageanalysis.coloration.PageSyntaxColorizer;
import org.wpcleaner.lib.check.CheckAlgorithmRegistry;
import org.wpcleaner.lib.check.CheckError;
import org.wpcleaner.lib.check.Replacement;

public final class PageAnalysisCheckPane extends SplitPane {

  private static final Duration ANALYSIS_DELAY = Duration.millis(500);

  private final CheckAlgorithmRegistry algorithmRegistry;
  private final PageAnalysisFactory analysisFactory;
  private final PauseTransition analysisDelay;
  private final CheckErrorsPane errorsPane;
  private final PageAnalysisScrollPane scrollPane;
  @Nullable private PageAnalysis analysis;

  public PageAnalysisCheckPane(
      final PageSyntaxColorizer colorizer,
      final JavaFxImageLoader imageLoader,
      final CheckAlgorithmRegistry algorithmRegistry,
      final PageAnalysisFactory analysisFactory,
      final Consumer<String> browseAction) {
    super();
    this.algorithmRegistry = algorithmRegistry;
    this.analysisFactory = analysisFactory;
    this.analysisDelay = new PauseTransition(ANALYSIS_DELAY);
    this.errorsPane =
        new CheckErrorsPane(imageLoader, this::showErrors, this::applyAutomaticFixes, browseAction);
    this.scrollPane = new PageAnalysisScrollPane(colorizer);
    initialize();
  }

  private void initialize() {
    getItems().addAll(errorsPane, scrollPane);
    setDividerPositions(0.2);
    setResizableWithParent(errorsPane, false);
    final PageAnalysisArea area = getArea();
    analysisDelay.setOnFinished(_ -> refreshAnalysis());
    area.textProperty().addListener((_, _, _) -> analysisDelay.playFromStart());
    area.addEventFilter(ContextMenuEvent.CONTEXT_MENU_REQUESTED, this::prepareContextMenu);
  }

  public CheckErrorsListView getErrorsListView() {
    return errorsPane.getErrorsListView();
  }

  public PageAnalysisArea getArea() {
    return scrollPane.getArea();
  }

  public void updateAnalysis(final PageAnalysis newAnalysis) {
    analysis = newAnalysis;
    getArea().updateAnalysis(newAnalysis);
    errorsPane.updateErrors(
        algorithmRegistry.getActiveAlgorithms(newAnalysis).stream()
            .map(algorithm -> CheckAlgorithmErrors.of(algorithmRegistry, algorithm, newAnalysis))
            .toList());
    highlightErrors(errorsPane.getSelectedErrors());
  }

  CheckErrorsContextMenu createContextMenu(final int position) {
    refreshAnalysis();
    return new CheckErrorsContextMenu(
        errorsPane.getErrors().stream()
            .map(algorithmErrors -> algorithmErrors.atPosition(position))
            .filter(algorithmErrors -> !algorithmErrors.errors().isEmpty())
            .toList(),
        this::applyReplacement);
  }

  private void showErrors(@Nullable final CheckAlgorithmErrors selectedErrors) {
    final List<CheckError> errors = highlightErrors(selectedErrors);
    if (!errors.isEmpty()) {
      final CheckError firstError = errors.getFirst();
      final PageAnalysisArea area = getArea();
      area.selectRange(firstError.begin(), firstError.end());
      area.requestFollowCaret();
    }
  }

  private List<CheckError> highlightErrors(@Nullable final CheckAlgorithmErrors selectedErrors) {
    final List<CheckError> errors = selectedErrors == null ? List.of() : selectedErrors.errors();
    getArea().highlight(errors);
    return errors;
  }

  private void refreshAnalysis() {
    analysisDelay.stop();
    final PageAnalysis currentAnalysis = analysis;
    final String text = getArea().getText();
    if (currentAnalysis != null && !currentAnalysis.getText().equals(text)) {
      updateAnalysis(
          analysisFactory.analysis(currentAnalysis.getWiki(), currentAnalysis.getTitle(), text));
    }
  }

  private void prepareContextMenu(final ContextMenuEvent event) {
    final PageAnalysisArea area = getArea();
    area.setContextMenu(
        area.hit(event.getX(), event.getY()).getCharacterIndex().stream()
            .mapToObj(this::createContextMenu)
            .filter(contextMenu -> !contextMenu.getItems().isEmpty())
            .findFirst()
            .orElse(null));
  }

  private void applyReplacement(final CheckError error, final Replacement replacement) {
    getArea().replaceText(error.begin(), error.end(), replacement.text());
    refreshAnalysis();
  }

  private void applyAutomaticFixes() {
    refreshAnalysis();
    int limit = getArea().getLength();
    for (final AutomaticFix fix : errorsPane.findAutomaticFixes()) {
      if (fix.error().end() <= limit) {
        getArea().replaceText(fix.error().begin(), fix.error().end(), fix.replacement().text());
        limit = fix.error().begin();
      }
    }
    refreshAnalysis();
  }
}
