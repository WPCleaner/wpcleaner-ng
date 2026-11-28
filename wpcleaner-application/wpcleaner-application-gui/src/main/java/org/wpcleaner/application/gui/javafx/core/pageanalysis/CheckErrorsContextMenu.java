package org.wpcleaner.application.gui.javafx.core.pageanalysis;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import org.wpcleaner.api.utils.GT;
import org.wpcleaner.lib.check.CheckError;
import org.wpcleaner.lib.check.ExpandedReplacement;
import org.wpcleaner.lib.check.ExternalView;
import org.wpcleaner.lib.check.Information;
import org.wpcleaner.lib.check.Replacement;
import org.wpcleaner.lib.check.Suggestion;
import org.wpcleaner.lib.check.TextInput;

public final class CheckErrorsContextMenu extends ContextMenu {

  private static final String AUTOMATIC_STYLE = "-fx-font-weight: bold;";
  private static final int MAX_DISPLAYED_LENGTH = 60;
  private static final int DISPLAYED_EDGE_LENGTH = 25;
  private static final String NEW_LINE = "\n";
  private static final String DISPLAYED_NEW_LINE = "↵";
  private static final String ELLIPSIS = "…";

  public CheckErrorsContextMenu(
      final List<CheckAlgorithmErrors> algorithmsErrors,
      final BiConsumer<CheckError, String> replacementAction,
      final ExpansionAction expansionAction,
      final BiConsumer<CheckError, TextInput> textInputAction,
      final Consumer<String> browseAction) {
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
                    createSuggestionItem(
                        error,
                        suggestion,
                        replacementAction,
                        expansionAction,
                        textInputAction,
                        browseAction))
            .forEach(getItems()::add);
      }
    }
  }

  private static MenuItem createSuggestionItem(
      final CheckError error,
      final Suggestion suggestion,
      final BiConsumer<CheckError, String> replacementAction,
      final ExpansionAction expansionAction,
      final BiConsumer<CheckError, TextInput> textInputAction,
      final Consumer<String> browseAction) {
    return switch (suggestion) {
      case Replacement replacement -> createReplacementItem(error, replacement, replacementAction);
      case ExpandedReplacement expandedReplacement ->
          createExpansionItem(error, expandedReplacement, expansionAction);
      case ExternalView externalView -> createExternalViewItem(externalView, browseAction);
      case Information information -> createInformationItem(information);
      case TextInput textInput -> createTextInputItem(error, textInput, textInputAction);
    };
  }

  private static MenuItem createTextInputItem(
      final CheckError error,
      final TextInput textInput,
      final BiConsumer<CheckError, TextInput> textInputAction) {
    final MenuItem item = new MenuItem(textInput.label());
    item.setOnAction(_ -> textInputAction.accept(error, textInput));
    return item;
  }

  private static MenuItem createExternalViewItem(
      final ExternalView externalView, final Consumer<String> browseAction) {
    final MenuItem item = new MenuItem(externalView.label());
    item.setOnAction(_ -> browseAction.accept(externalView.url()));
    return item;
  }

  private static MenuItem createInformationItem(final Information information) {
    final MenuItem item = new MenuItem(information.label());
    item.setDisable(true);
    return item;
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
    if (!replacement.label().isEmpty() && !replacement.label().equals(replacement.text())) {
      return replacement.label();
    }
    return replacement.text().isEmpty()
        ? GT._T("Delete")
        : GT._T("Replace with %s", getDisplayedText(replacement.text()));
  }

  private static String getDisplayedText(final String text) {
    final String displayedText = text.replace(NEW_LINE, DISPLAYED_NEW_LINE);
    if (displayedText.length() <= MAX_DISPLAYED_LENGTH) {
      return displayedText;
    }
    return displayedText.substring(0, DISPLAYED_EDGE_LENGTH)
        + ELLIPSIS
        + displayedText.substring(displayedText.length() - DISPLAYED_EDGE_LENGTH);
  }
}
