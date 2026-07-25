package org.wpcleaner.application.gui.javafx.core;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.net.URI;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.Tooltip;
import org.jspecify.annotations.Nullable;
import org.wpcleaner.api.utils.GT;
import org.wpcleaner.application.gui.core.desktop.DesktopService;
import org.wpcleaner.application.gui.javafx.JavaFxImageLoader;
import org.wpcleaner.application.gui.javafx.JavaFxInitializer;
import org.wpcleaner.lib.image.ImageCollection;
import org.wpcleaner.lib.image.ImageSize;

public class UrlTableCell<S> extends TableCell<S, @Nullable URI> {

  private final Button button = new Button();
  private final DesktopService desktopService;

  public UrlTableCell(
      final JavaFxImageLoader imageLoader,
      final DesktopService desktopService,
      final ImageCollection icon) {
    this.desktopService = desktopService;
    button.setStyle("-fx-background-color: transparent; -fx-padding: 1px;");
    imageLoader.getImageView(icon, ImageSize.BUTTON).ifPresent(button::setGraphic);
    button.setTooltip(new Tooltip(GT._T("Open URL")));
    setAlignment(Pos.CENTER);
  }

  @Override
  public void updateItem(@Nullable final URI item, final boolean empty) {
    super.updateItem(item, empty);
    if (empty || item == null) {
      setGraphic(null);
    } else {
      button.setOnAction(_ -> JavaFxInitializer.browse(desktopService, item.toString()));
      setGraphic(button);
    }
  }
}
