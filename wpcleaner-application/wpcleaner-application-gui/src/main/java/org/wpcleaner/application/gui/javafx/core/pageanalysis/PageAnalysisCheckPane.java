package org.wpcleaner.application.gui.javafx.core.pageanalysis;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TitledPane;
import org.jspecify.annotations.Nullable;
import org.wpcleaner.api.analysis.PageAnalysis;
import org.wpcleaner.api.utils.GT;
import org.wpcleaner.application.gui.javafx.core.pageanalysis.coloration.PageSyntaxColorizer;
import org.wpcleaner.lib.check.CheckAlgorithmRegistry;
import org.wpcleaner.lib.check.CheckError;

public final class PageAnalysisCheckPane extends SplitPane {

  private final CheckAlgorithmRegistry algorithmRegistry;
  private final CheckErrorsListView errorsListView;
  private final PageAnalysisScrollPane scrollPane;

  public PageAnalysisCheckPane(
      final PageSyntaxColorizer colorizer, final CheckAlgorithmRegistry algorithmRegistry) {
    super();
    this.algorithmRegistry = algorithmRegistry;
    this.errorsListView = new CheckErrorsListView(this::showErrors);
    this.scrollPane = new PageAnalysisScrollPane(colorizer);
    initialize();
  }

  private void initialize() {
    final TitledPane errorsPane = new TitledPane(GT._T("Check Wiki errors"), errorsListView);
    errorsPane.setCollapsible(false);
    errorsPane.setMaxHeight(Double.MAX_VALUE);
    getItems().addAll(errorsPane, scrollPane);
    setDividerPositions(0.2);
    setResizableWithParent(errorsPane, false);
    errorsListView
        .getSelectionModel()
        .selectedItemProperty()
        .addListener((_, _, selectedErrors) -> showErrors(selectedErrors));
  }

  public CheckErrorsListView getErrorsListView() {
    return errorsListView;
  }

  public PageAnalysisArea getArea() {
    return scrollPane.getArea();
  }

  public void updateAnalysis(final PageAnalysis analysis) {
    getArea().updateAnalysis(analysis);
    errorsListView
        .getItems()
        .setAll(
            algorithmRegistry.getAlgorithms().stream()
                .map(algorithm -> new CheckAlgorithmErrors(algorithm, algorithm.analyze(analysis)))
                .filter(algorithmErrors -> !algorithmErrors.errors().isEmpty())
                .toList());
  }

  private void showErrors(@Nullable final CheckAlgorithmErrors selectedErrors) {
    final List<CheckError> errors = selectedErrors == null ? List.of() : selectedErrors.errors();
    final PageAnalysisArea area = getArea();
    area.highlight(errors);
    if (!errors.isEmpty()) {
      final CheckError firstError = errors.getFirst();
      area.selectRange(firstError.begin(), firstError.end());
      area.requestFollowCaret();
    }
  }
}
