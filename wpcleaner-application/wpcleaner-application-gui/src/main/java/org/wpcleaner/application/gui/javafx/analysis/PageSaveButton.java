package org.wpcleaner.application.gui.javafx.analysis;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import org.wpcleaner.api.utils.GT;
import org.wpcleaner.application.gui.javafx.JavaFxImageLoader;
import org.wpcleaner.application.gui.javafx.core.control.DefaultStyles;
import org.wpcleaner.lib.image.ImageCollection;
import org.wpcleaner.lib.image.ImageSize;

public final class PageSaveButton extends Button {

  public static final String SAVE_ID = "save";

  public PageSaveButton(
      final JavaFxImageLoader imageLoader, final boolean demo, final Runnable saveAction) {
    super();
    setId(SAVE_ID);
    setStyle(DefaultStyles.TOOLBAR_ELEMENT);
    imageLoader
        .getImageView(ImageCollection.DOCUMENT_SEND, ImageSize.BUTTON)
        .ifPresent(this::setGraphic);
    setTooltip(new Tooltip(GT._T("Save the page")));
    setDisable(demo);
    setOnAction(_ -> saveAction.run());
  }
}
