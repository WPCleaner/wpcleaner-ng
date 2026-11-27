package org.wpcleaner.application.gui.javafx.core.pageanalysis;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.ArrayList;
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
import org.wpcleaner.lib.check.ExpandedReplacement;
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
            () ->
                new CheckErrorsContextMenu(List.of(algorithmErrors), (_, _) -> {}, (_, _) -> {})
                    .getItems());

    // THEN
    Assertions.assertThat(items)
        .extracting(MenuItem::getText)
        .containsExactly(NAME, "Label", "text", "Remove the line", "Delete");
  }

  @DisplayName("should display the label of an expansion and call the expansion action")
  @Test
  void expand() throws ExecutionException, InterruptedException, TimeoutException {
    // GIVEN
    final ExpandedReplacement expandedReplacement = new ExpandedReplacement("{{X}}", "Expand");
    final CheckError error =
        new CheckError(
            34, 0, 5, List.of(new Replacement("{{subst:X}}", false), expandedReplacement));
    final CheckAlgorithmErrors algorithmErrors =
        new CheckAlgorithmErrors(
            algorithmRegistry.getAlgorithms().getFirst(), NAME, null, null, List.of(error));
    final List<CheckError> replacedErrors = new ArrayList<>();
    final List<ExpandedReplacement> expansions = new ArrayList<>();
    final List<MenuItem> items =
        callOnJavaFx(
            () ->
                new CheckErrorsContextMenu(
                        List.of(algorithmErrors),
                        (_, _) -> replacedErrors.add(error),
                        (expandedError, expansion) -> {
                          replacedErrors.add(expandedError);
                          expansions.add(expansion);
                        })
                    .getItems());

    // WHEN
    runOnJavaFx(items.get(2)::fire);

    // THEN
    Assertions.assertThat(items)
        .extracting(MenuItem::getText)
        .containsExactly(NAME, "{{subst:X}}", "Expand");
    Assertions.assertThat(replacedErrors).containsExactly(error);
    Assertions.assertThat(expansions).containsExactly(expandedReplacement);
  }
}
