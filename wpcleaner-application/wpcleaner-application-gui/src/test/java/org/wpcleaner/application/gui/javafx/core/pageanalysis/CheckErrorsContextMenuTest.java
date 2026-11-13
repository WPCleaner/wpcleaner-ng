package org.wpcleaner.application.gui.javafx.core.pageanalysis;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import javafx.scene.control.MenuItem;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.wpcleaner.application.gui.GuiSpringBootTest;
import org.wpcleaner.application.gui.javafx.JavaFxTest;
import org.wpcleaner.lib.check.CheckAlgorithmRegistry;
import org.wpcleaner.lib.check.CheckError;
import org.wpcleaner.lib.check.Replacement;

@GuiSpringBootTest
class CheckErrorsContextMenuTest extends JavaFxTest {

  private static final String NAME = "Error";

  @Autowired private CheckAlgorithmRegistry algorithmRegistry;

  @DisplayName("should display the label of a replacement, or its text when the label is empty")
  @Test
  void displayLabels() throws ExecutionException, InterruptedException, TimeoutException {
    // GIVEN
    final CheckError error =
        new CheckError(
            48,
            0,
            1,
            List.of(
                new Replacement("text", "Label", false),
                new Replacement("text", "", false),
                new Replacement("", "Remove the line", false),
                new Replacement("", "", false)));
    final CheckAlgorithmErrors algorithmErrors =
        new CheckAlgorithmErrors(
            algorithmRegistry.getAlgorithms().getFirst(), NAME, null, null, List.of(error));

    // WHEN
    final List<MenuItem> items =
        callOnJavaFx(
            () -> new CheckErrorsContextMenu(List.of(algorithmErrors), (_, _) -> {}).getItems());

    // THEN
    Assertions.assertThat(items)
        .extracting(MenuItem::getText)
        .containsExactly(NAME, "Label", "text", "Remove the line", "Delete");
  }
}
