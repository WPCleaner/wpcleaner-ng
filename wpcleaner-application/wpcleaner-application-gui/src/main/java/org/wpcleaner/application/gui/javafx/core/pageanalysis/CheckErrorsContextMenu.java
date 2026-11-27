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
import org.wpcleaner.lib.check.ExpandedReplacement;
import org.wpcleaner.lib.check.Replacement;
import org.wpcleaner.lib.check.Suggestion;

public final class CheckErrorsContextMenu extends ContextMenu {

  private static final String AUTOMATIC_STYLE = "-fx-font-weight: bold;";

  public CheckErrorsContextMenu(
      final List<CheckAlgorithmErrors> algorithmsErrors,
      final BiConsumer<CheckError, String> replacementAction,
      final ExpansionAction expansionAction) {
    super();
    for (final CheckAlgorithmErrors algorithmErrors : algorithmsErrors) {
      for (final CheckError error : algorithmErrors.errors()) {
        if (!getItems().isEmpty()) {
          getItems().add(new SeparatorMenuItem());
        }
        final MenuItem algorithmItem = new MenuItem(algorithmErrors.name());
        algorithmItem.setDisable(true);
        getItems().add(algorithmItem);
        error.suggestions().stream()
            .map(
                suggestion ->
                    createSuggestionItem(error, suggestion, replacementAction, expansionAction))
            .forEach(getItems()::add);
      }
    }
  }

  private static MenuItem createSuggestionItem(
      final CheckError error,
      final Suggestion suggestion,
      final BiConsumer<CheckError, String> replacementAction,
      final ExpansionAction expansionAction) {
    return switch (suggestion) {
      case Replacement replacement -> createReplacementItem(error, replacement, replacementAction);
      case ExpandedReplacement expandedReplacement ->
          createExpansionItem(error, expandedReplacement, expansionAction);
    };
  }

  private static MenuItem createExpansionItem(
      final CheckError error,
      final ExpandedReplacement expandedReplacement,
      final ExpansionAction expansionAction) {
    final MenuItem item = new MenuItem(expandedReplacement.label());
    item.setOnAction(_ -> expansionAction.expand(error, expandedReplacement));
    return item;
  }

  private static MenuItem createReplacementItem(
      final CheckError error,
      final Replacement replacement,
      final BiConsumer<CheckError, String> replacementAction) {
    final MenuItem item = new MenuItem(getDisplayedLabel(replacement));
    if (replacement.automatic()) {
      item.setStyle(AUTOMATIC_STYLE);
    }
    item.setOnAction(_ -> replacementAction.accept(error, replacement.text()));
    return item;
  }

  private static String getDisplayedLabel(final Replacement replacement) {
    if (!replacement.label().isEmpty()) {
      return replacement.label();
    }
    return replacement.text().isEmpty() ? GT._T("Delete") : replacement.text();
  }
}
