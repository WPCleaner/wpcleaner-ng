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

class ContentReplaceConfigurationPaneTest extends JavaFxTest {

  @DisplayName("ContentReplaceConfigurationPane initializes children properly with config")
  @Test
  void testInitializationWithConfiguration()
      throws InterruptedException, ExecutionException, TimeoutException {
    runOnJavaFx(
        () -> {
          final ContentReplaceConfiguration initial =
              new ContentReplaceConfiguration("mySummary", "myText");
          final ContentReplaceConfigurationPane pane =
              new ContentReplaceConfigurationPane("Test Title", initial);

          Assertions.assertThat(pane.getText()).isEqualTo("Test Title");
          Assertions.assertThat(pane.isCollapsible()).isFalse();
          Assertions.assertThat(pane.isComplete()).isTrue();

          final ContentReplaceConfiguration retrieved = pane.getContentReplaceConfiguration();
          Assertions.assertThat(retrieved.summary()).isEqualTo("mySummary");
          Assertions.assertThat(retrieved.text()).isEqualTo("myText");

          final GridPane grid = (GridPane) pane.getContent();
          final TextField summaryField = (TextField) grid.getChildren().get(1);
          final TextArea textArea = (TextArea) grid.getChildren().get(3);

          Assertions.assertThat(summaryField.getText()).isEqualTo("mySummary");
          Assertions.assertThat(textArea.getText()).isEqualTo("myText");
          Assertions.assertThat(GridPane.getHgrow(summaryField)).isEqualTo(Priority.ALWAYS);
          Assertions.assertThat(GridPane.getHgrow(textArea)).isEqualTo(Priority.ALWAYS);
          Assertions.assertThat(GridPane.getVgrow(textArea)).isEqualTo(Priority.ALWAYS);
        });
  }

  @DisplayName("ContentReplaceConfigurationPane initializes children empty when config is null")
  @Test
  void testInitializationWithoutConfiguration()
      throws InterruptedException, ExecutionException, TimeoutException {
    runOnJavaFx(
        () -> {
          final ContentReplaceConfigurationPane pane =
              new ContentReplaceConfigurationPane("Test Title", null);

          Assertions.assertThat(pane.getText()).isEqualTo("Test Title");
          Assertions.assertThat(pane.isComplete()).isFalse();

          final ContentReplaceConfiguration retrieved = pane.getContentReplaceConfiguration();
          Assertions.assertThat(retrieved.summary()).isEmpty();
          Assertions.assertThat(retrieved.text()).isEmpty();

          final GridPane grid = (GridPane) pane.getContent();
          final TextField summaryField = (TextField) grid.getChildren().get(1);
          final TextArea textArea = (TextArea) grid.getChildren().get(3);

          summaryField.setText("newSummary");
          Assertions.assertThat(pane.isComplete()).isFalse();

          textArea.setText("newText");
          Assertions.assertThat(pane.isComplete()).isTrue();

          final ContentReplaceConfiguration updated = pane.getContentReplaceConfiguration();
          Assertions.assertThat(updated.summary()).isEqualTo("newSummary");
          Assertions.assertThat(updated.text()).isEqualTo("newText");
        });
  }
}
