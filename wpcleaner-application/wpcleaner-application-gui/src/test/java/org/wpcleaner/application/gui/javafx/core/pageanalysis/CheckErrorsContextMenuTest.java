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
import org.wpcleaner.lib.check.ExternalView;
import org.wpcleaner.lib.check.Information;
import org.wpcleaner.lib.check.Replacement;
import org.wpcleaner.lib.check.TextInput;

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
                new Replacement("text", false),
                new Replacement("a\nb", false),
                new Replacement(
                    "123456789012345678901234567890123456789012345678901234567890", false),
                new Replacement(
                    "1234567890123456789012345678901234567890123456789012345678901", false),
                new Replacement("", "Remove the line", false),
                new Replacement("", "", false)));
    final CheckAlgorithmErrors algorithmErrors =
        new CheckAlgorithmErrors(
            algorithmRegistry.getAlgorithms().getFirst(), NAME, null, null, List.of(error));

    // WHEN
    final List<MenuItem> items =
        callOnJavaFx(
            () ->
                new CheckErrorsContextMenu(
                        List.of(algorithmErrors), (_, _) -> {}, (_, _) -> {}, (_, _) -> {}, _ -> {})
                    .getItems());

    // THEN
    Assertions.assertThat(items)
        .extracting(MenuItem::getText)
        .containsExactly(
            NAME,
            "Label",
            "Replace with text",
            "Replace with text",
            "Replace with a↵b",
            "Replace with 123456789012345678901234567890123456789012345678901234567890",
            "Replace with 1234567890123456789012345…7890123456789012345678901",
            "Remove the line",
            "Delete");
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
                        },
                        (_, _) -> {},
                        _ -> {})
                    .getItems());

    // WHEN
    runOnJavaFx(items.get(2)::fire);

    // THEN
    Assertions.assertThat(items)
        .extracting(MenuItem::getText)
        .containsExactly(NAME, "Replace with {{subst:X}}", "Expand");
    Assertions.assertThat(replacedErrors).containsExactly(error);
    Assertions.assertThat(expansions).containsExactly(expandedReplacement);
  }

  @DisplayName("should display an information as a disabled item")
  @Test
  void displayInformation() throws ExecutionException, InterruptedException, TimeoutException {
    // GIVEN
    final CheckError error =
        new CheckError(
            16,
            0,
            2,
            List.of(new Information("U+200E – Left-to-right mark"), new Replacement("a", false)));
    final CheckAlgorithmErrors algorithmErrors =
        new CheckAlgorithmErrors(
            algorithmRegistry.getAlgorithms().getFirst(), NAME, null, null, List.of(error));

    // WHEN
    final List<MenuItem> items =
        callOnJavaFx(
            () ->
                new CheckErrorsContextMenu(
                        List.of(algorithmErrors), (_, _) -> {}, (_, _) -> {}, (_, _) -> {}, _ -> {})
                    .getItems());

    // THEN
    Assertions.assertThat(items)
        .extracting(MenuItem::getText, MenuItem::isDisable)
        .containsExactly(
            Assertions.tuple(NAME, true),
            Assertions.tuple("U+200E – Left-to-right mark", true),
            Assertions.tuple("Replace with a", false));
  }

  @DisplayName("should display the label of an external view and browse its URL")
  @Test
  void browseExternalView() throws ExecutionException, InterruptedException, TimeoutException {
    // GIVEN
    final String url = "https://fr.wikipedia.org/wiki/File:A.jpg";
    final CheckError error =
        new CheckError(30, 0, 14, List.of(new ExternalView("View image", url)));
    final CheckAlgorithmErrors algorithmErrors =
        new CheckAlgorithmErrors(
            algorithmRegistry.getAlgorithms().getFirst(), NAME, null, null, List.of(error));
    final List<String> browsedUrls = new ArrayList<>();
    final List<MenuItem> items =
        callOnJavaFx(
            () ->
                new CheckErrorsContextMenu(
                        List.of(algorithmErrors),
                        (_, _) -> {},
                        (_, _) -> {},
                        (_, _) -> {},
                        browsedUrls::add)
                    .getItems());

    // WHEN
    runOnJavaFx(items.get(1)::fire);

    // THEN
    Assertions.assertThat(items).extracting(MenuItem::getText).containsExactly(NAME, "View image");
    Assertions.assertThat(browsedUrls).containsExactly(url);
  }

  @DisplayName("should display the label of a text input and call the text input action")
  @Test
  void inputText() throws ExecutionException, InterruptedException, TimeoutException {
    // GIVEN
    final TextInput textInput =
        new TextInput("Add a description...", "Description?", "[[File:A.jpg|", "]]", "[]|=");
    final CheckError error = new CheckError(30, 0, 14, List.of(textInput));
    final CheckAlgorithmErrors algorithmErrors =
        new CheckAlgorithmErrors(
            algorithmRegistry.getAlgorithms().getFirst(), NAME, null, null, List.of(error));
    final List<CheckError> inputErrors = new ArrayList<>();
    final List<TextInput> textInputs = new ArrayList<>();
    final List<MenuItem> items =
        callOnJavaFx(
            () ->
                new CheckErrorsContextMenu(
                        List.of(algorithmErrors),
                        (_, _) -> {},
                        (_, _) -> {},
                        (inputError, input) -> {
                          inputErrors.add(inputError);
                          textInputs.add(input);
                        },
                        _ -> {})
                    .getItems());

    // WHEN
    runOnJavaFx(items.get(1)::fire);

    // THEN
    Assertions.assertThat(items)
        .extracting(MenuItem::getText)
        .containsExactly(NAME, "Add a description...");
    Assertions.assertThat(inputErrors).containsExactly(error);
    Assertions.assertThat(textInputs).containsExactly(textInput);
  }
}
