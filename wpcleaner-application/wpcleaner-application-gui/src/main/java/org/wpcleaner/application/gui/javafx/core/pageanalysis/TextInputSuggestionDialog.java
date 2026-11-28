package org.wpcleaner.application.gui.javafx.core.pageanalysis;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.function.BiConsumer;
import javafx.beans.binding.Bindings;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Window;
import org.wpcleaner.api.utils.GT;
import org.wpcleaner.lib.check.CheckError;
import org.wpcleaner.lib.check.TextInput;

final class TextInputSuggestionDialog extends TextInputDialog {

  static final String VALUE_ID = "text-input-value";

  private TextInputSuggestionDialog(final Window owner, final TextInput textInput) {
    super();
    initOwner(owner);
    setTitle(textInput.label());
    getEditor().setId(VALUE_ID);
    headerTextProperty()
        .bind(
            Bindings.createStringBinding(
                () -> createHeader(textInput, getEditor().getText()), getEditor().textProperty()));
    getDialogPane()
        .lookupButton(ButtonType.OK)
        .disableProperty()
        .bind(
            Bindings.createBooleanBinding(
                () -> !textInput.isAuthorized(getEditor().getText()), getEditor().textProperty()));
  }

  static void input(
      final Window owner,
      final CheckError error,
      final TextInput textInput,
      final BiConsumer<CheckError, String> replacementAction) {
    new TextInputSuggestionDialog(owner, textInput)
        .showAndWait()
        .ifPresent(value -> replacementAction.accept(error, textInput.replacement(value)));
  }

  private static String createHeader(final TextInput textInput, final String value) {
    if (value.isBlank() || textInput.isAuthorized(value)) {
      return textInput.question();
    }
    return textInput.question()
        + "\n"
        + GT._T(
            "The value cannot contain any of these characters: %s",
            textInput.unauthorizedCharacters());
  }
}
