package org.wpcleaner.application.gui.javafx.core.action.content.append;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.wpcleaner.application.gui.javafx.JavaFxTest;

class ContentAppendActionControlTest extends JavaFxTest {

  @DisplayName("ContentAppendActionControl initializes children properly with config")
  @Test
  void testInitializationWithConfiguration()
      throws InterruptedException, ExecutionException, TimeoutException {
    runOnJavaFx(
        () -> {
          final List<ContentAppendConfigurationText> texts =
              List.of(
                  new ContentAppendConfigurationText(true, "label1", "text1"),
                  new ContentAppendConfigurationText(false, "label2", "text2"));
          final ContentAppendActionControl pane =
              new ContentAppendActionControl(
                  "Talk Page Action", new ContentAppendConfiguration("mySummary", texts));

          Assertions.assertThat(pane.getText()).isEqualTo("Talk Page Action");
          Assertions.assertThat(pane.isCollapsible()).isFalse();
          Assertions.assertThat(pane.isComplete()).isTrue();
          Assertions.assertThat(pane.getSummary()).isEqualTo("mySummary");

          Assertions.assertThat(pane.getSelectedTexts()).containsExactly("text1");

          final TextField summaryField =
              lookup(pane, ContentAppendActionControl.SUMMARY_ID, TextField.class);
          Assertions.assertThat(summaryField.getText()).isEqualTo("mySummary");
          Assertions.assertThat(GridPane.getHgrow(summaryField)).isEqualTo(Priority.ALWAYS);

          final VBox checkboxesBox = lookup(pane, ContentAppendActionControl.TEXTS_ID, VBox.class);
          Assertions.assertThat(checkboxesBox.getChildren()).hasSize(2);

          final CheckBox cb1 = (CheckBox) checkboxesBox.getChildren().get(0);
          final CheckBox cb2 = (CheckBox) checkboxesBox.getChildren().get(1);
          Assertions.assertThat(cb1.isSelected()).isTrue();
          Assertions.assertThat(cb2.isSelected()).isFalse();

          cb2.setSelected(true);
          Assertions.assertThat(pane.getSelectedTexts()).containsExactly("text1", "text2");
        });
  }

  @DisplayName("ContentAppendActionControl initializes children empty when config has empty fields")
  @Test
  void testInitializationWithEmptyConfiguration()
      throws InterruptedException, ExecutionException, TimeoutException {
    runOnJavaFx(
        () -> {
          final ContentAppendActionControl pane =
              new ContentAppendActionControl(
                  "Talk Page Action", new ContentAppendConfiguration("", List.of()));

          Assertions.assertThat(pane.getText()).isEqualTo("Talk Page Action");
          Assertions.assertThat(pane.isComplete()).isFalse();
          Assertions.assertThat(pane.getSummary()).isEmpty();
          Assertions.assertThat(pane.getSelectedTexts()).isEmpty();

          final TextField summaryField =
              lookup(pane, ContentAppendActionControl.SUMMARY_ID, TextField.class);

          summaryField.setText("enteredSummary");
          Assertions.assertThat(pane.isComplete()).isTrue();
          Assertions.assertThat(pane.getSummary()).isEqualTo("enteredSummary");
        });
  }

  @DisplayName("ContentAppendActionControl throws NullPointerException when initialConfig is null")
  @Test
  void testConstructorThrowsNullPointerExceptionWhenInitialConfigIsNull()
      throws InterruptedException, ExecutionException, TimeoutException {
    runOnJavaFx(
        () ->
            Assertions.assertThatThrownBy(
                    () -> new ContentAppendActionControl("Talk Page Action", null))
                .isInstanceOf(NullPointerException.class));
  }
}
