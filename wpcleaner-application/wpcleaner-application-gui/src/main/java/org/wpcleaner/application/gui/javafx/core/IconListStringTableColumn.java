package org.wpcleaner.application.gui.javafx.core;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import java.util.function.Function;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.jspecify.annotations.Nullable;

public class IconListStringTableColumn<S> extends TableColumn<S, @Nullable List<String>> {

  public IconListStringTableColumn(
      final String tooltipText,
      @Nullable final Image image,
      final Function<S, @Nullable List<String>> mapper) {
    super();

    final Label headerLabel = new Label();
    if (image != null) {
      headerLabel.setGraphic(new ImageView(image));
    }
    headerLabel.setTooltip(new Tooltip(tooltipText));
    setGraphic(headerLabel);

    setCellValueFactory(cellData -> new SimpleObjectProperty<>(mapper.apply(cellData.getValue())));
    setCellFactory(_ -> new IconListStringTableCell<>(tooltipText + ":", image));
    setPrefWidth(50);
    setResizable(false);
  }
}
