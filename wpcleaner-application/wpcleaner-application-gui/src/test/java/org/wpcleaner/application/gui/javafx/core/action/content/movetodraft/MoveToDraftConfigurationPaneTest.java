package org.wpcleaner.application.gui.javafx.core.action.content.movetodraft;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.wpcleaner.application.gui.javafx.JavaFxTest;

class MoveToDraftConfigurationPaneTest extends JavaFxTest {

  @DisplayName("MoveToDraftConfigurationPane initializes children properly with config")
  @Test
  void testInitializationWithConfiguration()
      throws InterruptedException, ExecutionException, TimeoutException {
    runOnJavaFx(
        () -> {
          final MoveToDraftConfiguration initial =
              new MoveToDraftConfiguration("DraftSubPage", "Moving page");
          final MoveToDraftConfigurationPane pane =
              new MoveToDraftConfigurationPane("Move Title", initial);

          Assertions.assertThat(pane.getText()).isEqualTo("Move Title");
          Assertions.assertThat(pane.isCollapsible()).isFalse();
          Assertions.assertThat(pane.isComplete()).isTrue();
          Assertions.assertThat(pane.getSummary()).isEqualTo("Moving page");
          Assertions.assertThat(pane.getSubPageName()).isEqualTo("DraftSubPage");

          final MoveToDraftConfiguration retrieved = pane.getMoveToDraftConfiguration();
          Assertions.assertThat(retrieved.summary()).isEqualTo("Moving page");
          Assertions.assertThat(retrieved.subPageName()).isEqualTo("DraftSubPage");

          final GridPane grid = (GridPane) pane.getContent();
          final TextField summaryField = (TextField) grid.getChildren().get(1);
          final TextField subPageNameField = (TextField) grid.getChildren().get(3);

          Assertions.assertThat(summaryField.getText()).isEqualTo("Moving page");
          Assertions.assertThat(subPageNameField.getText()).isEqualTo("DraftSubPage");
          Assertions.assertThat(GridPane.getHgrow(summaryField)).isEqualTo(Priority.ALWAYS);
          Assertions.assertThat(GridPane.getHgrow(subPageNameField)).isEqualTo(Priority.ALWAYS);
        });
  }

  @DisplayName("MoveToDraftConfigurationPane initializes empty when config is null")
  @Test
  void testInitializationWithoutConfiguration()
      throws InterruptedException, ExecutionException, TimeoutException {
    runOnJavaFx(
        () -> {
          final MoveToDraftConfigurationPane pane =
              new MoveToDraftConfigurationPane("Move Title", null);

          Assertions.assertThat(pane.getText()).isEqualTo("Move Title");
          Assertions.assertThat(pane.isComplete()).isFalse();
          Assertions.assertThat(pane.getSummary()).isEmpty();
          Assertions.assertThat(pane.getSubPageName()).isEmpty();

          final GridPane grid = (GridPane) pane.getContent();
          final TextField summaryField = (TextField) grid.getChildren().get(1);
          final TextField subPageNameField = (TextField) grid.getChildren().get(3);

          summaryField.setText("summary");
          Assertions.assertThat(pane.isComplete()).isFalse();

          subPageNameField.setText("subPage");
          Assertions.assertThat(pane.isComplete()).isTrue();
        });
  }
}
