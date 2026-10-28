package org.wpcleaner.application.gui.javafx.core.action.content.append;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.wpcleaner.application.gui.GuiSpringBootTest;
import org.wpcleaner.application.gui.javafx.JavaFxTest;
import org.wpcleaner.lib.image.ImageLoader;

@GuiSpringBootTest
class ContentAppendConfigurationPaneTest extends JavaFxTest {

  @Autowired private ImageLoader imageLoader;

  @DisplayName("ContentAppendConfigurationPane initializes children properly with config")
  @Test
  void initializationWithConfiguration()
      throws InterruptedException, ExecutionException, TimeoutException {
    runOnJavaFx(
        () -> {
          final List<ContentAppendConfigurationText> initialTexts =
              List.of(new ContentAppendConfigurationText(true, "myLabel", "myText"));
          final ContentAppendConfigurationPane pane =
              new ContentAppendConfigurationPane(
                  "Append Title",
                  imageLoader,
                  new ContentAppendConfiguration("mySummary", initialTexts));

          Assertions.assertThat(pane.getText()).isEqualTo("Append Title");
          Assertions.assertThat(pane.isCollapsible()).isFalse();
          Assertions.assertThat(pane.isComplete()).isTrue();
          Assertions.assertThat(pane.getSummary()).isEqualTo("mySummary");

          final ContentAppendConfiguration fullConfig = pane.getContentAppendConfiguration();
          Assertions.assertThat(fullConfig.summary()).isEqualTo("mySummary");
          Assertions.assertThat(fullConfig.texts()).hasSize(1);

          final List<ContentAppendConfigurationText> retrieved = pane.getTexts();
          Assertions.assertThat(retrieved).hasSize(1);
          Assertions.assertThat(retrieved.getFirst().addedByDefault()).isTrue();
          Assertions.assertThat(retrieved.getFirst().label()).isEqualTo("myLabel");
          Assertions.assertThat(retrieved.getFirst().text()).isEqualTo("myText");

          final TextField summaryField =
              lookup(pane, ContentAppendConfigurationPane.SUMMARY_ID, TextField.class);
          Assertions.assertThat(summaryField.getText()).isEqualTo("mySummary");
          Assertions.assertThat(GridPane.getHgrow(summaryField)).isEqualTo(Priority.ALWAYS);

          final TableView<?> tableView =
              lookup(pane, ContentAppendConfigurationPane.TEXTS_ID, TableView.class);
          Assertions.assertThat(tableView.getColumns()).hasSize(3);
          Assertions.assertThat(tableView.getColumns().get(0).isSortable()).isFalse();
          Assertions.assertThat(tableView.getColumns().get(1).isSortable()).isFalse();
          Assertions.assertThat(tableView.getColumns().get(2).isSortable()).isFalse();

          final Button addButton =
              lookup(pane, ContentAppendConfigurationPane.ADD_ID, Button.class);
          final Button removeButton =
              lookup(pane, ContentAppendConfigurationPane.REMOVE_ID, Button.class);

          Assertions.assertThat(addButton.getText()).isEmpty();
          Assertions.assertThat(removeButton.getText()).isEmpty();
          Assertions.assertThat(addButton.getGraphic()).isInstanceOf(ImageView.class);
          Assertions.assertThat(removeButton.getGraphic()).isInstanceOf(ImageView.class);

          Assertions.assertThat(removeButton.isDisabled()).isTrue();
          tableView.getSelectionModel().select(0);
          Assertions.assertThat(removeButton.isDisabled()).isFalse();

          Assertions.assertThat(GridPane.getVgrow(tableView.getParent()))
              .isEqualTo(Priority.ALWAYS);
          Assertions.assertThat(VBox.getVgrow(tableView)).isEqualTo(Priority.ALWAYS);
        });
  }

  @DisplayName("ContentAppendConfigurationPane initializes children empty when config is null")
  @Test
  void initializationWithoutConfiguration()
      throws InterruptedException, ExecutionException, TimeoutException {
    runOnJavaFx(
        () -> {
          final ContentAppendConfigurationPane pane =
              new ContentAppendConfigurationPane("Append Title", imageLoader, null);

          Assertions.assertThat(pane.getText()).isEqualTo("Append Title");
          Assertions.assertThat(pane.isComplete()).isFalse();
          Assertions.assertThat(pane.getSummary()).isEmpty();
          Assertions.assertThat(pane.getTexts()).isEmpty();

          final TextField summaryField =
              lookup(pane, ContentAppendConfigurationPane.SUMMARY_ID, TextField.class);

          summaryField.setText("enteredSummary");
          Assertions.assertThat(pane.isComplete()).isTrue();
          Assertions.assertThat(pane.getSummary()).isEqualTo("enteredSummary");
        });
  }
}
