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

public final class LinterErrorsButton extends Button {

  public static final String LINTER_ERRORS_ID = "linter-errors";

  public LinterErrorsButton(final JavaFxImageLoader imageLoader, final Runnable action) {
    super();
    setId(LINTER_ERRORS_ID);
    setStyle(DefaultStyles.TOOLBAR_ELEMENT);
    imageLoader.getImageView(ImageCollection.LINTER, ImageSize.BUTTON).ifPresent(this::setGraphic);
    setTooltip(new Tooltip(GT._T("Retrieve the errors detected by Linter in the current text")));
    setOnAction(_ -> action.run());
  }
}
