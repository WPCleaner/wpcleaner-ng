package org.wpcleaner.application.gui.javafx;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import javafx.scene.control.Alert;
import org.springframework.stereotype.Component;
import org.wpcleaner.application.gui.core.action.NotImplementedAction;

@Component
public class NotImplementedJavaFxAction implements NotImplementedAction {

  @Override
  public void run() {
    final Alert alert = new Alert(Alert.AlertType.INFORMATION);
    alert.setTitle("Not implemented");
    alert.setHeaderText(null);
    alert.setContentText("This feature is not implemented yet. Try again later!");
    alert.showAndWait();
  }
}
