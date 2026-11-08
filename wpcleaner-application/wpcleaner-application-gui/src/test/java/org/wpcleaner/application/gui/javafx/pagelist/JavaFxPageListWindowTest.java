package org.wpcleaner.application.gui.javafx.pagelist;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.image.ImageView;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.wpcleaner.api.api.ApiException;
import org.wpcleaner.application.gui.GuiSpringBootTest;
import org.wpcleaner.application.gui.javafx.JavaFxTest;
import org.wpcleaner.application.gui.javafx.JavaFxWindowsRegistry;

@GuiSpringBootTest
class JavaFxPageListWindowTest extends JavaFxTest {

  @Autowired private JavaFxPageListWindowServices services;
  @Autowired private JavaFxWindowsRegistry windowsRegistry;

  @AfterEach
  void tearDown() throws InterruptedException, ExecutionException, TimeoutException {
    runOnJavaFx(windowsRegistry::closeAllWindows);
  }

  @DisplayName("should apply the page action to each selected page from the toolbar")
  @Test
  void analyzeSelectedPages() throws InterruptedException, ExecutionException, TimeoutException {
    // GIVEN
    final List<String> openedPages = new ArrayList<>();
    final JavaFxPageListWindow window =
        callOnJavaFx(
            () ->
                new JavaFxPageListWindow(
                    services, "Pages", _ -> List.of("Paris", "Lyon", "Nice"), openedPages::add));
    @SuppressWarnings("unchecked")
    final ListView<String> pages =
        waitForJavaFx(
            () ->
                find(window, JavaFxPageListWindow.PAGES_ID, ListView.class)
                    .filter(list -> !list.getItems().isEmpty()));
    final Button button =
        callOnJavaFx(() -> lookup(window, JavaFxPageListWindow.ANALYZE_ID, Button.class));
    Assertions.assertThat(callOnJavaFx(button::isDisable)).isTrue();
    runOnJavaFx(() -> pages.getSelectionModel().selectIndices(0, 2));
    Assertions.assertThat(callOnJavaFx(button::isDisable)).isFalse();

    // WHEN
    runOnJavaFx(button::fire);

    // THEN
    Assertions.assertThat(callOnJavaFx(() -> List.copyOf(openedPages)))
        .containsExactly("Paris", "Nice");
    Assertions.assertThat(button.getGraphic()).isInstanceOf(ImageView.class);
    Assertions.assertThat(button.getTooltip().getText()).isEqualTo("Analyze the selected pages");
  }

  @DisplayName("should report an error when the pages can't be retrieved")
  @Test
  void displayError() throws InterruptedException, ExecutionException, TimeoutException {
    // GIVEN
    final List<String> openedPages = new ArrayList<>();

    // WHEN
    final JavaFxPageListWindow window =
        callOnJavaFx(
            () ->
                new JavaFxPageListWindow(
                    services,
                    "Pages",
                    _ -> {
                      throw new ApiException("Server unavailable", "No details");
                    },
                    openedPages::add));

    // THEN
    Assertions.assertThat(closeAlert(window))
        .isEqualTo("Error retrieving the pages: Server unavailable");
    Assertions.assertThat(openedPages).isEmpty();
  }
}
