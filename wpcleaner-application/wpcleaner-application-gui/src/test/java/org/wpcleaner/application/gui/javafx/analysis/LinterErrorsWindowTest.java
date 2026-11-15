package org.wpcleaner.application.gui.javafx.analysis;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.stream.IntStream;
import javafx.scene.control.TableView;
import org.assertj.core.api.Assertions;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.wpcleaner.api.repository.linter.LinterCategory;
import org.wpcleaner.api.repository.linter.LinterPriority;
import org.wpcleaner.api.rest.lint.LintError;
import org.wpcleaner.application.gui.GuiSpringBootTest;
import org.wpcleaner.application.gui.javafx.JavaFxTest;
import org.wpcleaner.application.gui.javafx.core.pageanalysis.PageAnalysisArea;

@GuiSpringBootTest
class LinterErrorsWindowTest extends JavaFxTest {

  private static final String TEXT = "<center>Paris</center> <b>x";

  @Autowired private JavaFxAnalysisWindowServices services;

  @AfterEach
  void tearDown() throws InterruptedException, ExecutionException, TimeoutException {
    runOnJavaFx(services.windowsRegistry()::closeAllWindows);
  }

  @DisplayName("should display the errors detected by Linter")
  @Test
  void displayErrors() throws InterruptedException, ExecutionException, TimeoutException {
    // GIVEN
    final PageAnalysisArea area = createArea();

    // WHEN
    final LinterErrorsWindow window = openWindow(area, createErrors());

    // THEN
    Assertions.assertThat(callOnJavaFx(() -> window.getStage().getTitle()))
        .isEqualTo("Errors detected by Linter in Paris");
    Assertions.assertThat(callOnJavaFx(() -> getDisplayedErrors(window)))
        .containsExactly(
            List.of("Balises HTML obsolètes", "Low", "0", "22", "name=center", ""),
            List.of("missing-end-tag", "Medium", "23", "", "inTable=false;name=b", "Modèle:B"),
            List.of("duplicate-ids", "High", "", "", "", "Multiple templates"),
            List.of("stripped-tag", "Low", "27", "27", "", ""),
            List.of("misnested-tag", "Medium", "0", "1", "", ""));
  }

  @DisplayName("should select the error in the text, or its start when its end is unknown")
  @Test
  void selectErrors() throws InterruptedException, ExecutionException, TimeoutException {
    // GIVEN
    final PageAnalysisArea area = createArea();
    final LinterErrorsWindow window = openWindow(area, createErrors());

    // WHEN
    runOnJavaFx(() -> getTable(window).getSelectionModel().select(0));

    // THEN
    Assertions.assertThat(callOnJavaFx(area::getSelectedText)).isEqualTo("<center>Paris</center>");
    runOnJavaFx(() -> getTable(window).getSelectionModel().select(1));
    Assertions.assertThat(callOnJavaFx(area::getSelectedText)).isEmpty();
    Assertions.assertThat(callOnJavaFx(area::getCaretPosition)).isEqualTo(23);
    runOnJavaFx(() -> getTable(window).getSelectionModel().select(2));
    Assertions.assertThat(callOnJavaFx(area::getCaretPosition)).isEqualTo(23);
    runOnJavaFx(() -> getTable(window).getSelectionModel().select(3));
    Assertions.assertThat(callOnJavaFx(area::getCaretPosition)).isEqualTo(TEXT.length());
    runOnJavaFx(() -> getTable(window).getSelectionModel().select(4));
    Assertions.assertThat(callOnJavaFx(area::getSelectedText)).isEqualTo("<");
  }

  @DisplayName("should ignore positions outside the text, but accept the end of the text")
  @Test
  void selectErrorsOutsideText() throws InterruptedException, ExecutionException, TimeoutException {
    // GIVEN
    final PageAnalysisArea area = createArea();
    final LinterErrorsWindow window =
        openWindow(
            area,
            List.of(
                createError("obsolete-tag", 0, TEXT.length() + 1),
                createError("obsolete-tag", TEXT.length() + 1, TEXT.length() + 2),
                createError("obsolete-tag", 5, 4),
                createError("missing-end-tag", 23, TEXT.length())));

    // WHEN
    runOnJavaFx(
        () -> {
          area.moveTo(3);
          getTable(window).getSelectionModel().select(0);
        });

    // THEN
    Assertions.assertThat(callOnJavaFx(area::getSelectedText)).isEmpty();
    Assertions.assertThat(callOnJavaFx(area::getCaretPosition)).isZero();
    runOnJavaFx(
        () -> {
          area.moveTo(3);
          getTable(window).getSelectionModel().select(1);
        });
    Assertions.assertThat(callOnJavaFx(area::getCaretPosition)).isEqualTo(3);
    runOnJavaFx(() -> getTable(window).getSelectionModel().select(2));
    Assertions.assertThat(callOnJavaFx(area::getCaretPosition)).isEqualTo(5);
    runOnJavaFx(() -> getTable(window).getSelectionModel().select(3));
    Assertions.assertThat(callOnJavaFx(area::getSelectedText)).isEqualTo("<b>x");
  }

  private static List<LinterError> createErrors() {
    return List.of(
        new LinterError(
            new LintError("obsolete-tag", List.of(0, 22, 8, 9), Map.of("name", "center"), null),
            createCategory(LinterPriority.LOW, "Balises HTML obsolètes")),
        new LinterError(
            new LintError(
                "missing-end-tag",
                Arrays.asList(23, null),
                Map.of("name", "b", "inTable", false),
                new LintError.TemplateInfo(null, "Modèle:B")),
            createCategory(LinterPriority.MEDIUM, null)),
        new LinterError(
            new LintError(
                "duplicate-ids", List.of(), Map.of(), new LintError.TemplateInfo(true, null)),
            createCategory(LinterPriority.HIGH, null)),
        new LinterError(
            new LintError(
                "stripped-tag",
                List.of(TEXT.length(), TEXT.length()),
                Map.of(),
                new LintError.TemplateInfo(false, null)),
            createCategory(LinterPriority.LOW, null)),
        new LinterError(
            new LintError(
                "misnested-tag", List.of(0, 1), Map.of(), new LintError.TemplateInfo(null, null)),
            createCategory(LinterPriority.MEDIUM, null)));
  }

  private static LinterCategory createCategory(
      final LinterPriority priority, @Nullable final String label) {
    return new LinterCategory("category", priority, label);
  }

  private static LinterError createError(final String type, final int begin, final int end) {
    return new LinterError(
        new LintError(type, List.of(begin, end), Map.of(), null),
        createCategory(LinterPriority.LOW, null));
  }

  private PageAnalysisArea createArea()
      throws InterruptedException, ExecutionException, TimeoutException {
    return callOnJavaFx(
        () -> {
          final PageAnalysisArea area = new PageAnalysisArea(services.colorizer());
          area.replaceText(TEXT);
          return area;
        });
  }

  private LinterErrorsWindow openWindow(final PageAnalysisArea area, final List<LinterError> errors)
      throws InterruptedException, ExecutionException, TimeoutException {
    return callOnJavaFx(() -> new LinterErrorsWindow(services, null, "Paris", errors, area));
  }

  @SuppressWarnings("unchecked")
  private static TableView<LinterError> getTable(final LinterErrorsWindow window) {
    return lookup(window, LinterErrorsWindow.ERRORS_ID, TableView.class);
  }

  private static List<List<Object>> getDisplayedErrors(final LinterErrorsWindow window) {
    final TableView<LinterError> table = getTable(window);
    return IntStream.range(0, table.getItems().size())
        .mapToObj(
            row ->
                table.getColumns().stream()
                    .map(column -> (Object) column.getCellData(row))
                    .toList())
        .toList();
  }
}
