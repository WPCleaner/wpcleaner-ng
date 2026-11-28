package org.wpcleaner.application.gui.javafx.core.pageanalysis;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import javafx.scene.Scene;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.wpcleaner.application.gui.GuiSpringBootTest;
import org.wpcleaner.application.gui.javafx.JavaFxTest;
import org.wpcleaner.lib.check.CheckError;
import org.wpcleaner.lib.check.TextInput;

@GuiSpringBootTest
class TextInputSuggestionDialogTest extends JavaFxTest {

  private static final String QUESTION = "Description?";

  @DisplayName("should refuse a blank text or unauthorized characters and apply a valid text")
  @Test
  void refuseUnauthorizedText() throws ExecutionException, InterruptedException, TimeoutException {
    // GIVEN
    final TextInput textInput =
        new TextInput("Add a description...", QUESTION, "[[File:A.jpg|", "]]", "[]|=");
    final CheckError error = new CheckError(30, 0, 14, List.of(textInput));
    final List<String> replacements = new ArrayList<>();
    final List<String> headers = new ArrayList<>();
    final List<Boolean> disabledButtons = new ArrayList<>();
    final Stage stage = callOnJavaFx(TextInputSuggestionDialogTest::showStage);
    try {
      // WHEN
      runLaterOnJavaFx(
          () ->
              TextInputSuggestionDialog.input(
                  stage, error, textInput, (_, replacement) -> replacements.add(replacement)));
      answerDialog(
          stage,
          dialogPane -> {
            for (final String value : List.of(" ", "a[b", "Une ville")) {
              typeText(dialogPane, TextInputSuggestionDialog.VALUE_ID, value);
              headers.add(dialogPane.getHeaderText());
              disabledButtons.add(dialogPane.lookupButton(ButtonType.OK).isDisable());
            }
          });

      // THEN
      Assertions.assertThat(headers)
          .containsExactly(
              QUESTION,
              QUESTION + "\nThe value cannot contain any of these characters: []|=",
              QUESTION);
      Assertions.assertThat(disabledButtons).containsExactly(true, true, false);
      Assertions.assertThat(callOnJavaFx(() -> replacements))
          .containsExactly("[[File:A.jpg|Une ville]]");
    } finally {
      runOnJavaFx(stage::close);
    }
  }

  private static Stage showStage() {
    final Stage stage = new Stage();
    stage.setScene(new Scene(new Pane(), 200, 100));
    stage.show();
    return stage;
  }
}
