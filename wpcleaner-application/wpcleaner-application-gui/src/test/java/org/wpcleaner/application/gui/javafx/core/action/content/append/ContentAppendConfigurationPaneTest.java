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
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.wpcleaner.application.gui.javafx.JavaFxTest;
import org.wpcleaner.lib.image.ImageCollection;
import org.wpcleaner.lib.image.ImageLoader;
import org.wpcleaner.lib.image.ImageSize;

class ContentAppendConfigurationPaneTest extends JavaFxTest {

  @DisplayName("ContentAppendConfigurationPane initializes children properly with config")
  @Test
  void testInitializationWithConfiguration()
      throws InterruptedException, ExecutionException, TimeoutException {
    runOnJavaFx(
        () -> {
          final ImageLoader imageLoader = BDDMockito.mock(ImageLoader.class);
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

          final GridPane grid = (GridPane) pane.getContent();
          final TextField summaryField = (TextField) grid.getChildren().get(1);
          Assertions.assertThat(summaryField.getText()).isEqualTo("mySummary");
          Assertions.assertThat(GridPane.getHgrow(summaryField)).isEqualTo(Priority.ALWAYS);

          final VBox tableBox = (VBox) grid.getChildren().get(3);
          final TableView<?> tableView = (TableView<?>) tableBox.getChildren().getFirst();
          Assertions.assertThat(tableView.getColumns()).hasSize(3);
          Assertions.assertThat(tableView.getColumns().get(0).isSortable()).isFalse();
          Assertions.assertThat(tableView.getColumns().get(1).isSortable()).isFalse();
          Assertions.assertThat(tableView.getColumns().get(2).isSortable()).isFalse();

          final HBox buttonsBox = (HBox) tableBox.getChildren().get(1);
          final Button addButton = (Button) buttonsBox.getChildren().get(0);
          final Button removeButton = (Button) buttonsBox.getChildren().get(1);

          Assertions.assertThat(addButton.getText()).isEmpty();
          Assertions.assertThat(removeButton.getText()).isEmpty();
          BDDMockito.then(imageLoader)
              .should()
              .getImageResource(ImageCollection.LIST_ADD, ImageSize.BUTTON);
          BDDMockito.then(imageLoader)
              .should()
              .getImageResource(ImageCollection.LIST_REMOVE, ImageSize.BUTTON);

          Assertions.assertThat(removeButton.isDisabled()).isTrue();
          tableView.getSelectionModel().select(0);
          Assertions.assertThat(removeButton.isDisabled()).isFalse();

          Assertions.assertThat(GridPane.getVgrow(tableBox)).isEqualTo(Priority.ALWAYS);
          Assertions.assertThat(VBox.getVgrow(tableView)).isEqualTo(Priority.ALWAYS);
        });
  }

  @DisplayName("ContentAppendConfigurationPane initializes children empty when config is null")
  @Test
  void testInitializationWithoutConfiguration()
      throws InterruptedException, ExecutionException, TimeoutException {
    runOnJavaFx(
        () -> {
          final ImageLoader imageLoader = BDDMockito.mock(ImageLoader.class);
          final ContentAppendConfigurationPane pane =
              new ContentAppendConfigurationPane("Append Title", imageLoader, null);

          Assertions.assertThat(pane.getText()).isEqualTo("Append Title");
          Assertions.assertThat(pane.isComplete()).isFalse();
          Assertions.assertThat(pane.getSummary()).isEmpty();
          Assertions.assertThat(pane.getTexts()).isEmpty();

          final GridPane grid = (GridPane) pane.getContent();
          final TextField summaryField = (TextField) grid.getChildren().get(1);

          summaryField.setText("enteredSummary");
          Assertions.assertThat(pane.isComplete()).isTrue();
          Assertions.assertThat(pane.getSummary()).isEqualTo("enteredSummary");
        });
  }
}
