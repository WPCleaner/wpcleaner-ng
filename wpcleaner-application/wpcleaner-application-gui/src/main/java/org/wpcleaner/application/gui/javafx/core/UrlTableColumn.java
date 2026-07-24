package org.wpcleaner.application.gui.javafx.core;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.net.URI;
import java.util.function.Function;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.TableColumn;
import org.jspecify.annotations.Nullable;
import org.wpcleaner.application.gui.core.desktop.DesktopService;
import org.wpcleaner.application.gui.javafx.JavaFxImageLoader;
import org.wpcleaner.lib.image.ImageCollection;

public class UrlTableColumn<S> extends TableColumn<S, @Nullable URI> {

  public UrlTableColumn(
      final String title,
      final JavaFxImageLoader imageLoader,
      final DesktopService desktopService,
      final ImageCollection icon,
      final Function<S, @Nullable URI> mapper) {
    super(title);
    setCellValueFactory(cellData -> new SimpleObjectProperty<>(mapper.apply(cellData.getValue())));
    setCellFactory(_ -> new UrlTableCell<>(imageLoader, desktopService, icon));
    setPrefWidth(30);
    setResizable(false);
  }
}
