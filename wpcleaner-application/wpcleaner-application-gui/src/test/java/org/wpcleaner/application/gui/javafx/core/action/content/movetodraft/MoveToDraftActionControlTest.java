package org.wpcleaner.application.gui.javafx.core.action.content.movetodraft;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.wpcleaner.application.gui.javafx.JavaFxTest;
import org.wpcleaner.application.gui.javafx.core.control.DefaultStyles;
import org.wpcleaner.lib.image.ImageCollection;
import org.wpcleaner.lib.image.ImageLoader;
import org.wpcleaner.lib.image.ImageSize;

class MoveToDraftActionControlTest extends JavaFxTest {

  @DisplayName("MoveToDraftActionControl initializes children properly with config")
  @Test
  void testInitializationWithConfiguration()
      throws InterruptedException, ExecutionException, TimeoutException {
    runOnJavaFx(
        () -> {
          final ImageLoader imageLoader = Mockito.mock(ImageLoader.class);
          final AtomicBoolean refreshed = new AtomicBoolean(false);
          final MoveToDraftConfiguration initial =
              new MoveToDraftConfiguration("Draft", "Moving to draft");
          final MoveToDraftActionControl pane =
              new MoveToDraftActionControl(
                  imageLoader, "Move Title", initial, () -> refreshed.set(true));

          Assertions.assertThat(pane.getText()).isEqualTo("Move Title");
          Assertions.assertThat(pane.isCollapsible()).isFalse();
          Assertions.assertThat(pane.isComplete()).isTrue();
          Assertions.assertThat(pane.getSummary()).isEqualTo("Moving to draft");
          Assertions.assertThat(pane.getSubPageName()).isEqualTo("Draft");

          final GridPane grid = (GridPane) pane.getContent();
          Assertions.assertThat(grid.getChildren()).hasSize(5);

          final Label summaryLabel = (Label) grid.getChildren().get(0);
          Assertions.assertThat(summaryLabel.getText()).isEqualTo("Summary:");

          final TextField summaryField = (TextField) grid.getChildren().get(1);
          Assertions.assertThat(summaryField.getText()).isEqualTo("Moving to draft");
          Assertions.assertThat(GridPane.getHgrow(summaryField)).isEqualTo(Priority.ALWAYS);
          Assertions.assertThat(GridPane.getColumnSpan(summaryField)).isEqualTo(2);

          final Label subPageNameLabel = (Label) grid.getChildren().get(2);
          Assertions.assertThat(subPageNameLabel.getText()).isEqualTo("Sub-page name:");

          final TextField subPageNameField = (TextField) grid.getChildren().get(3);
          Assertions.assertThat(subPageNameField.getText()).isEqualTo("Draft");
          Assertions.assertThat(GridPane.getHgrow(subPageNameField)).isEqualTo(Priority.ALWAYS);

          final Button refreshButton = (Button) grid.getChildren().get(4);
          Assertions.assertThat(refreshButton.getStyle()).isEqualTo(DefaultStyles.TOOLBAR_ELEMENT);
          Assertions.assertThat(refreshButton.getTooltip().getText())
              .isEqualTo("Refresh draft page content");
          Mockito.verify(imageLoader).getImageResource(ImageCollection.REFRESH, ImageSize.BUTTON);

          refreshButton.fire();
          Assertions.assertThat(refreshed.get()).isTrue();

          refreshed.set(false);
          subPageNameField.fireEvent(new javafx.event.ActionEvent());
          Assertions.assertThat(refreshed.get()).isTrue();
        });
  }

  @DisplayName("MoveToDraftActionControl initializes empty when config has empty fields")
  @Test
  void testInitializationWithEmptyConfiguration()
      throws InterruptedException, ExecutionException, TimeoutException {
    runOnJavaFx(
        () -> {
          final ImageLoader imageLoader = Mockito.mock(ImageLoader.class);
          final MoveToDraftActionControl pane =
              new MoveToDraftActionControl(
                  imageLoader, "Move Title", new MoveToDraftConfiguration("", ""), () -> {});

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

  @DisplayName("MoveToDraftActionControl throws NullPointerException when imageLoader is null")
  @Test
  void testConstructorThrowsNullPointerExceptionWhenImageLoaderIsNull()
      throws InterruptedException, ExecutionException, TimeoutException {
    runOnJavaFx(
        () ->
            Assertions.assertThatThrownBy(
                    () ->
                        new MoveToDraftActionControl(
                            null, "Move Title", new MoveToDraftConfiguration("", ""), () -> {}))
                .isInstanceOf(NullPointerException.class));
  }

  @DisplayName("MoveToDraftActionControl throws NullPointerException when initialConfig is null")
  @Test
  void testConstructorThrowsNullPointerExceptionWhenInitialConfigIsNull()
      throws InterruptedException, ExecutionException, TimeoutException {
    runOnJavaFx(
        () -> {
          final ImageLoader imageLoader = Mockito.mock(ImageLoader.class);
          Assertions.assertThatThrownBy(
                  () -> new MoveToDraftActionControl(imageLoader, "Move Title", null, () -> {}))
              .isInstanceOf(NullPointerException.class);
        });
  }
}
