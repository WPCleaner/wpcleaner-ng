package org.wpcleaner.application.gui.javafx.core.pageanalysis;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import javafx.beans.value.ChangeListener;
import javafx.scene.control.Button;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.jspecify.annotations.Nullable;
import org.wpcleaner.api.utils.GT;
import org.wpcleaner.application.gui.javafx.JavaFxImageLoader;
import org.wpcleaner.application.gui.javafx.core.control.DefaultStyles;
import org.wpcleaner.lib.check.CheckAlgorithm;
import org.wpcleaner.lib.check.Replacement;
import org.wpcleaner.lib.image.ImageCollection;
import org.wpcleaner.lib.image.ImageSize;

public final class CheckErrorsPane extends TitledPane {

  public static final String AUTOMATIC_FIXES_ID = "automatic-fixes";

  private final Button automaticFixesButton;
  private final CheckErrorsListView errorsListView;
  private final ChangeListener<CheckAlgorithmErrors> selectionListener;

  public CheckErrorsPane(
      final JavaFxImageLoader imageLoader,
      final Consumer<@Nullable CheckAlgorithmErrors> showAction,
      final Runnable automaticFixesAction) {
    super();
    this.automaticFixesButton = new Button();
    this.errorsListView = new CheckErrorsListView(showAction);
    this.selectionListener = (_, _, selectedErrors) -> showAction.accept(selectedErrors);
    initialize(imageLoader, automaticFixesAction);
  }

  private void initialize(
      final JavaFxImageLoader imageLoader, final Runnable automaticFixesAction) {
    setText(GT._T("Check Wiki errors"));
    setCollapsible(false);
    setMaxHeight(Double.MAX_VALUE);
    automaticFixesButton.setId(AUTOMATIC_FIXES_ID);
    automaticFixesButton.setStyle(DefaultStyles.TOOLBAR_ELEMENT);
    imageLoader
        .getImageView(ImageCollection.AUTOMATIC_FIXES, ImageSize.BUTTON)
        .ifPresent(automaticFixesButton::setGraphic);
    automaticFixesButton.setTooltip(new Tooltip(GT._T("Automatic fixes")));
    automaticFixesButton.setDisable(true);
    automaticFixesButton.setOnAction(_ -> automaticFixesAction.run());
    VBox.setVgrow(errorsListView, Priority.ALWAYS);
    setContent(new VBox(new ToolBar(automaticFixesButton), errorsListView));
    errorsListView.getSelectionModel().selectedItemProperty().addListener(selectionListener);
  }

  public CheckErrorsListView getErrorsListView() {
    return errorsListView;
  }

  public List<CheckAlgorithmErrors> getErrors() {
    return List.copyOf(errorsListView.getItems());
  }

  public @Nullable CheckAlgorithmErrors getSelectedErrors() {
    return errorsListView.getSelectionModel().getSelectedItem();
  }

  public void updateErrors(final List<CheckAlgorithmErrors> algorithmsErrors) {
    final MultipleSelectionModel<CheckAlgorithmErrors> selectionModel =
        errorsListView.getSelectionModel();
    final Optional<CheckAlgorithmErrors> previousErrors =
        Optional.ofNullable(selectionModel.getSelectedItem());
    final Set<CheckAlgorithm> displayedAlgorithms =
        errorsListView.getItems().stream()
            .map(CheckAlgorithmErrors::algorithm)
            .collect(Collectors.toSet());
    final List<CheckAlgorithmErrors> displayedErrors =
        algorithmsErrors.stream()
            .filter(
                algorithmErrors ->
                    !algorithmErrors.errors().isEmpty()
                        || displayedAlgorithms.contains(algorithmErrors.algorithm()))
            .toList();
    selectionModel.selectedItemProperty().removeListener(selectionListener);
    try {
      errorsListView.getItems().setAll(displayedErrors);
      previousErrors
          .flatMap(
              previous ->
                  displayedErrors.stream()
                      .filter(errors -> errors.algorithm().equals(previous.algorithm()))
                      .findFirst())
          .ifPresent(selectionModel::select);
    } finally {
      selectionModel.selectedItemProperty().addListener(selectionListener);
    }
    automaticFixesButton.setDisable(findAutomaticFixes().isEmpty());
  }

  public List<AutomaticFix> findAutomaticFixes() {
    return errorsListView.getItems().stream()
        .flatMap(algorithmErrors -> algorithmErrors.errors().stream())
        .flatMap(
            error ->
                error.replacements().stream()
                    .filter(Replacement::automatic)
                    .findFirst()
                    .map(replacement -> new AutomaticFix(error, replacement))
                    .stream())
        .sorted(Comparator.comparingInt((AutomaticFix fix) -> fix.error().begin()).reversed())
        .toList();
  }
}
