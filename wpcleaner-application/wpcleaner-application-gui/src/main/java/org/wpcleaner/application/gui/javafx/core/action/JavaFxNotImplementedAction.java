package org.wpcleaner.application.gui.javafx.core.action;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import javafx.scene.control.Alert;
import org.springframework.stereotype.Component;
import org.wpcleaner.api.utils.GT;
import org.wpcleaner.application.gui.core.action.NotImplementedAction;

@Component
public class JavaFxNotImplementedAction implements NotImplementedAction {

  @Override
  public void run() {
    final Alert alert = new Alert(Alert.AlertType.INFORMATION);
    alert.setTitle(GT._T("Not implemented"));
    alert.setHeaderText(null);
    alert.setContentText(GT._T("This feature is not implemented yet. Try again later!"));
    alert.showAndWait();
  }
}
