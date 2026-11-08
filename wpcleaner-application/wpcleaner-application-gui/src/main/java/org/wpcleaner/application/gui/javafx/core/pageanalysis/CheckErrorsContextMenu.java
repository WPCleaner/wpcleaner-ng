package org.wpcleaner.application.gui.javafx.core.pageanalysis;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import java.util.function.BiConsumer;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import org.wpcleaner.api.utils.GT;
import org.wpcleaner.lib.check.CheckError;
import org.wpcleaner.lib.check.Replacement;

public final class CheckErrorsContextMenu extends ContextMenu {

  private static final String AUTOMATIC_STYLE = "-fx-font-weight: bold;";

  public CheckErrorsContextMenu(
      final List<CheckAlgorithmErrors> algorithmsErrors,
      final BiConsumer<CheckError, Replacement> replacementAction) {
    super();
    for (final CheckAlgorithmErrors algorithmErrors : algorithmsErrors) {
      for (final CheckError error : algorithmErrors.errors()) {
        if (!getItems().isEmpty()) {
          getItems().add(new SeparatorMenuItem());
        }
        final MenuItem algorithmItem = new MenuItem(algorithmErrors.name());
        algorithmItem.setDisable(true);
        getItems().add(algorithmItem);
        error.replacements().stream()
            .map(replacement -> createReplacementItem(error, replacement, replacementAction))
            .forEach(getItems()::add);
      }
    }
  }

  private static MenuItem createReplacementItem(
      final CheckError error,
      final Replacement replacement,
      final BiConsumer<CheckError, Replacement> replacementAction) {
    final MenuItem item =
        new MenuItem(replacement.text().isEmpty() ? GT._T("Delete") : replacement.text());
    if (replacement.automatic()) {
      item.setStyle(AUTOMATIC_STYLE);
    }
    item.setOnAction(_ -> replacementAction.accept(error, replacement));
    return item;
  }
}
