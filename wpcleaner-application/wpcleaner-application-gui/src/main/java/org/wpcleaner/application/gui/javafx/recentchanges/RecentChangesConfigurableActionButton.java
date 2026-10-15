package org.wpcleaner.application.gui.javafx.recentchanges;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import javafx.beans.binding.Bindings;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseButton;
import org.jspecify.annotations.Nullable;
import org.wpcleaner.application.gui.javafx.JavaFxImageLoader;
import org.wpcleaner.application.gui.javafx.core.control.DefaultStyles;
import org.wpcleaner.lib.image.ImageCollection;
import org.wpcleaner.lib.image.ImageLoader;
import org.wpcleaner.lib.image.ImageSize;

public final class RecentChangesConfigurableActionButton extends Button {

  public RecentChangesConfigurableActionButton(
      final ImageLoader imageLoader,
      final ImageCollection image,
      final String tooltipText,
      final RecentChangesConfigurableAction action,
      final ObservableValue<@Nullable FilteredRecentChange> currentRecentChange) {
    super();
    final JavaFxImageLoader fxImageLoader = new JavaFxImageLoader(imageLoader);
    setStyle(DefaultStyles.TOOLBAR_ELEMENT);
    fxImageLoader.getImageView(image, ImageSize.BUTTON).ifPresent(this::setGraphic);
    setTooltip(new Tooltip(tooltipText));
    disableProperty()
        .bind(
            Bindings.createBooleanBinding(
                () -> {
                  final FilteredRecentChange rc = currentRecentChange.getValue();
                  return rc == null || !action.canApply(rc);
                },
                currentRecentChange));
    setOnAction(
        _ -> {
          final FilteredRecentChange rc = currentRecentChange.getValue();
          if (rc != null && action.canApply(rc)) {
            action.apply(rc);
          }
        });
    setOnMouseClicked(
        event -> {
          if (event.getButton() == MouseButton.SECONDARY) {
            action.configure();
          }
        });
  }
}
