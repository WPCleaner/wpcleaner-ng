package org.wpcleaner.application.gui.javafx.core;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.jspecify.annotations.Nullable;

public class ImageToggleButton extends ToggleButton {

  public ImageToggleButton(
      final String tooltipText,
      @Nullable final Image unselectedImage,
      @Nullable final Image selectedImage) {
    super();
    setTooltip(new Tooltip(tooltipText));
    setStyle("-fx-background-color: transparent; -fx-padding: 1px;");

    final ImageView imageView = new ImageView();
    if (unselectedImage != null) {
      imageView.setImage(unselectedImage);
    }
    setGraphic(imageView);

    selectedProperty()
        .addListener(
            (_, _, isSelected) -> {
              if (isSelected) {
                if (selectedImage != null) {
                  imageView.setImage(selectedImage);
                }
              } else {
                if (unselectedImage != null) {
                  imageView.setImage(unselectedImage);
                }
              }
            });
  }
}
