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

public final class CheckWikiDetectionsButton extends Button {

  public static final String CHECK_WIKI_DETECTIONS_ID = "check-wiki-detections";

  public CheckWikiDetectionsButton(
      final JavaFxImageLoader imageLoader, final boolean available, final Runnable action) {
    super();
    setId(CHECK_WIKI_DETECTIONS_ID);
    setStyle(DefaultStyles.TOOLBAR_ELEMENT);
    imageLoader
        .getImageView(ImageCollection.LOGO_WPCLEANER, ImageSize.BUTTON)
        .ifPresent(this::setGraphic);
    setTooltip(new Tooltip(GT._T("Retrieve the errors still detected by Check Wiki")));
    setDisable(!available);
    setOnAction(_ -> action.run());
  }
}
