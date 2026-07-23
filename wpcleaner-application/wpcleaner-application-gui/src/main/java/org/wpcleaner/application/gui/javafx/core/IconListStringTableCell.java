package org.wpcleaner.application.gui.javafx.core;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import java.util.stream.Collectors;
import javafx.geometry.Pos;
import javafx.scene.control.TableCell;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.jspecify.annotations.Nullable;

public class IconListStringTableCell<S> extends TableCell<S, @Nullable List<String>> {

  private final String tooltipPrefix;
  @Nullable private final Image image;

  public IconListStringTableCell(final String tooltipPrefix, @Nullable final Image image) {
    super();
    this.tooltipPrefix = tooltipPrefix;
    this.image = image;
    setAlignment(Pos.CENTER);
  }

  @Override
  protected void updateItem(@Nullable final List<String> item, final boolean empty) {
    super.updateItem(item, empty);
    if (empty || item == null || item.isEmpty()) {
      setGraphic(null);
      setTooltip(null);
    } else {
      if (image != null) {
        setGraphic(new ImageView(image));
      } else {
        setGraphic(null);
      }
      final String tooltipText =
          tooltipPrefix
              + "\n"
              + item.stream().sorted().map(tag -> "* " + tag).collect(Collectors.joining("\n"));
      setTooltip(new Tooltip(tooltipText));
    }
  }
}
