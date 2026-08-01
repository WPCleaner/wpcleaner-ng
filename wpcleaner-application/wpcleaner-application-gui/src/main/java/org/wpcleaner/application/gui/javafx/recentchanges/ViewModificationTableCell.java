package org.wpcleaner.application.gui.javafx.recentchanges;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.function.Consumer;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.Tooltip;
import org.wpcleaner.api.utils.GT;
import org.wpcleaner.application.gui.javafx.JavaFxImageLoader;
import org.wpcleaner.lib.image.ImageCollection;
import org.wpcleaner.lib.image.ImageSize;

public final class ViewModificationTableCell
    extends TableCell<FilteredRecentChange, FilteredRecentChange> {

  private final Button button = new Button();
  private final Consumer<FilteredRecentChange> viewAction;

  public ViewModificationTableCell(
      final JavaFxImageLoader imageLoader, final Consumer<FilteredRecentChange> viewAction) {
    this.viewAction = viewAction;
    button.setStyle("-fx-background-color: transparent; -fx-padding: 1px;");
    imageLoader.getImageView(ImageCollection.PAGE, ImageSize.BUTTON).ifPresent(button::setGraphic);
    button.setTooltip(new Tooltip(GT._T("View modifications")));
    setAlignment(Pos.CENTER);
  }

  @Override
  protected void updateItem(final FilteredRecentChange item, final boolean empty) {
    super.updateItem(item, empty);
    if (empty) {
      setGraphic(null);
    } else {
      button.setOnAction(_ -> viewAction.accept(item));
      setGraphic(button);
    }
  }
}
