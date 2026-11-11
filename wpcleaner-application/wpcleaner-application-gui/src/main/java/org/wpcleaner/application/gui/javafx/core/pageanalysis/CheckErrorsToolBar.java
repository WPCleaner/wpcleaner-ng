package org.wpcleaner.application.gui.javafx.core.pageanalysis;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import javafx.scene.control.Button;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import org.wpcleaner.api.utils.GT;
import org.wpcleaner.application.gui.javafx.JavaFxImageLoader;
import org.wpcleaner.application.gui.javafx.core.control.DefaultStyles;
import org.wpcleaner.lib.check.CheckError;
import org.wpcleaner.lib.image.ImageCollection;
import org.wpcleaner.lib.image.ImageSize;

public final class CheckErrorsToolBar extends ToolBar {

  public static final String GO_FIRST_ID = "go-first-error";
  public static final String GO_PREVIOUS_ID = "go-previous-error";
  public static final String GO_NEXT_ID = "go-next-error";
  public static final String GO_LAST_ID = "go-last-error";

  private final PageAnalysisArea area;
  private final List<Button> navigationButtons;
  private List<CheckError> errors = List.of();

  public CheckErrorsToolBar(final JavaFxImageLoader imageLoader, final PageAnalysisArea area) {
    super();
    this.area = area;
    this.navigationButtons =
        List.of(
            createButton(
                imageLoader,
                GO_FIRST_ID,
                ImageCollection.GO_FIRST,
                GT._T("Go to first error"),
                _ -> Optional.ofNullable(errors.isEmpty() ? null : errors.getFirst())),
            createButton(
                imageLoader,
                GO_PREVIOUS_ID,
                ImageCollection.GO_PREVIOUS,
                GT._T("Go to previous error"),
                position ->
                    errors.reversed().stream()
                        .filter(error -> error.begin() < position)
                        .findFirst()),
            createButton(
                imageLoader,
                GO_NEXT_ID,
                ImageCollection.GO_NEXT,
                GT._T("Go to next error"),
                position -> errors.stream().filter(error -> error.begin() > position).findFirst()),
            createButton(
                imageLoader,
                GO_LAST_ID,
                ImageCollection.GO_LAST,
                GT._T("Go to last error"),
                _ -> Optional.ofNullable(errors.isEmpty() ? null : errors.getLast())));
    getItems().addAll(navigationButtons);
  }

  private Button createButton(
      final JavaFxImageLoader imageLoader,
      final String id,
      final ImageCollection image,
      final String tooltip,
      final Function<Integer, Optional<CheckError>> errorFinder) {
    final Button button = new Button();
    button.setId(id);
    button.setStyle(DefaultStyles.TOOLBAR_ELEMENT);
    imageLoader.getImageView(image, ImageSize.BUTTON).ifPresent(button::setGraphic);
    button.setTooltip(new Tooltip(tooltip));
    button.setDisable(true);
    button.setOnAction(
        _ -> errorFinder.apply(area.getSelection().getStart()).ifPresent(this::select));
    return button;
  }

  public void updateErrors(final List<CheckError> newErrors) {
    errors = newErrors.stream().sorted(Comparator.comparingInt(CheckError::begin)).toList();
    navigationButtons.forEach(button -> button.setDisable(errors.isEmpty()));
  }

  private void select(final CheckError error) {
    area.selectRange(error.begin(), error.end());
    area.requestFollowCaret();
  }
}
