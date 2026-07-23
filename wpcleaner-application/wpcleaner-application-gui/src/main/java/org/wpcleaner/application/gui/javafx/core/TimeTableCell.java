package org.wpcleaner.application.gui.javafx.core;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import javafx.scene.control.TableCell;
import org.jspecify.annotations.Nullable;

public class TimeTableCell<S> extends TableCell<S, @Nullable Instant> {

  @Override
  protected void updateItem(@Nullable final Instant item, final boolean empty) {
    super.updateItem(item, empty);
    if (empty || item == null) {
      setText(null);
    } else {
      setText(
          DateTimeFormatter.ISO_LOCAL_TIME.format(
              item.atZone(ZoneId.systemDefault()).toLocalTime()));
    }
  }
}
