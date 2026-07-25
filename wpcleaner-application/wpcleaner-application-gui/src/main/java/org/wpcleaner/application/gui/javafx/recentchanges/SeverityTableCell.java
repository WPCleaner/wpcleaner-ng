package org.wpcleaner.application.gui.javafx.recentchanges;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import javafx.geometry.Pos;
import javafx.scene.control.TableCell;
import javafx.scene.control.Tooltip;
import org.jspecify.annotations.Nullable;
import org.wpcleaner.application.gui.javafx.JavaFxImageLoader;
import org.wpcleaner.lib.image.ImageSize;

public class SeverityTableCell<S> extends TableCell<S, @Nullable Severity> {

  private final JavaFxImageLoader imageLoader;

  public SeverityTableCell(final JavaFxImageLoader imageLoader) {
    this.imageLoader = imageLoader;
    setAlignment(Pos.CENTER);
  }

  @Override
  public void updateItem(@Nullable final Severity severity, final boolean empty) {
    super.updateItem(severity, empty);
    if (empty || severity == null) {
      setGraphic(null);
      setTooltip(null);
    } else {
      imageLoader
          .getImageView(severity.getImage(), ImageSize.BUTTON)
          .ifPresentOrElse(
              imageView -> {
                setGraphic(imageView);
                setTooltip(new Tooltip(severity.getName()));
              },
              () -> {
                setGraphic(null);
                setTooltip(null);
              });
    }
  }
}
