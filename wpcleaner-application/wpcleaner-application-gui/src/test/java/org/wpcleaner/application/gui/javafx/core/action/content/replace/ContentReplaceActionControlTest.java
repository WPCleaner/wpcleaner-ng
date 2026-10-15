package org.wpcleaner.application.gui.javafx.core.action.content.replace;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.wpcleaner.application.gui.javafx.JavaFxTest;

class ContentReplaceActionControlTest extends JavaFxTest {

  @DisplayName("ContentReplaceActionControl initializes children properly with config")
  @Test
  void testInitializationWithConfiguration()
      throws InterruptedException, ExecutionException, TimeoutException {
    runOnJavaFx(
        () -> {
          final ContentReplaceConfiguration initial =
              new ContentReplaceConfiguration("mySummary", "myText");
          final ContentReplaceActionControl pane =
              new ContentReplaceActionControl("Action Title", initial);

          Assertions.assertThat(pane.getText()).isEqualTo("Action Title");
          Assertions.assertThat(pane.isCollapsible()).isFalse();
          Assertions.assertThat(pane.isComplete()).isTrue();
          Assertions.assertThat(pane.getSummary()).isEqualTo("mySummary");

          final ContentReplaceConfiguration retrieved = pane.getContentReplaceConfiguration();
          Assertions.assertThat(retrieved.summary()).isEqualTo("mySummary");
          Assertions.assertThat(retrieved.text()).isEqualTo("myText");

          final GridPane grid = (GridPane) pane.getContent();
          final TextField summaryField = (TextField) grid.getChildren().get(1);
          final TextArea textArea = (TextArea) grid.getChildren().get(3);

          Assertions.assertThat(summaryField.getText()).isEqualTo("mySummary");
          Assertions.assertThat(textArea.getText()).isEqualTo("myText");
          Assertions.assertThat(textArea.isEditable()).isFalse();
          Assertions.assertThat(GridPane.getHgrow(summaryField)).isEqualTo(Priority.ALWAYS);
          Assertions.assertThat(GridPane.getHgrow(textArea)).isEqualTo(Priority.ALWAYS);
        });
  }

  @DisplayName(
      "ContentReplaceActionControl initializes children empty when config has empty fields")
  @Test
  void testInitializationWithEmptyConfiguration()
      throws InterruptedException, ExecutionException, TimeoutException {
    runOnJavaFx(
        () -> {
          final ContentReplaceActionControl pane =
              new ContentReplaceActionControl(
                  "Action Title", new ContentReplaceConfiguration("", ""));

          Assertions.assertThat(pane.getText()).isEqualTo("Action Title");
          Assertions.assertThat(pane.isComplete()).isFalse();
          Assertions.assertThat(pane.getSummary()).isEmpty();

          final ContentReplaceConfiguration retrieved = pane.getContentReplaceConfiguration();
          Assertions.assertThat(retrieved.summary()).isEmpty();
          Assertions.assertThat(retrieved.text()).isEmpty();

          final GridPane grid = (GridPane) pane.getContent();
          final TextField summaryField = (TextField) grid.getChildren().get(1);

          summaryField.setText("enteredSummary");
          Assertions.assertThat(pane.isComplete()).isTrue();
          Assertions.assertThat(pane.getSummary()).isEqualTo("enteredSummary");
        });
  }

  @DisplayName("ContentReplaceActionControl throws NullPointerException when initialConfig is null")
  @Test
  void testConstructorThrowsNullPointerExceptionWhenInitialConfigIsNull()
      throws InterruptedException, ExecutionException, TimeoutException {
    runOnJavaFx(
        () ->
            Assertions.assertThatThrownBy(
                    () -> new ContentReplaceActionControl("Action Title", null))
                .isInstanceOf(NullPointerException.class));
  }
}
