package org.wpcleaner.application.gui.javafx.core.pageanalysis;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import javafx.scene.Scene;
import javafx.scene.control.IndexRange;
import javafx.scene.control.Labeled;
import javafx.scene.control.TitledPane;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.wpcleaner.api.analysis.PageAnalysisTestFactory;
import org.wpcleaner.application.gui.GuiSpringBootTest;
import org.wpcleaner.application.gui.core.style.PageAnalysisStylePropertiesInitializer;
import org.wpcleaner.application.gui.javafx.JavaFxTest;
import org.wpcleaner.application.gui.javafx.core.pageanalysis.coloration.PageSyntaxColorizer;
import org.wpcleaner.application.gui.javafx.core.style.JavaFxStylePropertiesRegistry;
import org.wpcleaner.lib.check.CheckAlgorithmRegistry;

@GuiSpringBootTest
class PageAnalysisCheckPaneTest extends JavaFxTest {

  private static final String TITLE = "Paris";
  private static final String FIRST_LINK = "[[Paris]]";
  private static final String TEXT =
      FIRST_LINK + " est une ville.\n== Histoire ==\nVoir [[Paris]] et [[Lyon]].";
  private static final int SECOND_LINK_BEGIN = TEXT.lastIndexOf(FIRST_LINK);
  private static final int OTHER_LINK_BEGIN = TEXT.indexOf("[[Lyon]]");

  @Autowired private PageSyntaxColorizer colorizer;
  @Autowired private CheckAlgorithmRegistry algorithmRegistry;
  @Autowired private JavaFxStylePropertiesRegistry styleRegistry;

  @DisplayName("should list only the algorithms detected in the page")
  @Test
  void listDetectedAlgorithms() throws ExecutionException, InterruptedException, TimeoutException {
    // GIVEN
    final PageAnalysisCheckPane pane =
        callOnJavaFx(() -> new PageAnalysisCheckPane(colorizer, algorithmRegistry));

    // WHEN
    runOnJavaFx(() -> pane.updateAnalysis(PageAnalysisTestFactory.createPageAnalysis(TITLE, TEXT)));

    // THEN
    Assertions.assertThat(pane.getErrorsListView().getItems())
        .singleElement()
        .satisfies(
            algorithmErrors -> {
              Assertions.assertThat(algorithmErrors.algorithm().name()).isEqualTo("Error No. 48");
              Assertions.assertThat(algorithmErrors.errors()).hasSize(2);
            });
  }

  @DisplayName("should display the list of errors with a title")
  @Test
  void displayTitle() throws ExecutionException, InterruptedException, TimeoutException {
    // GIVEN
    final PageAnalysisCheckPane pane =
        callOnJavaFx(() -> new PageAnalysisCheckPane(colorizer, algorithmRegistry));

    // WHEN
    final TitledPane errorsPane = (TitledPane) pane.getItems().getFirst();

    // THEN
    Assertions.assertThat(errorsPane.getText()).isEqualTo("Check Wiki errors");
    Assertions.assertThat(errorsPane.getContent()).isSameAs(pane.getErrorsListView());
  }

  @DisplayName("should display the number of errors after the algorithm name")
  @Test
  void displayErrorsCount() throws ExecutionException, InterruptedException, TimeoutException {
    // GIVEN
    final PageAnalysisCheckPane pane =
        callOnJavaFx(() -> new PageAnalysisCheckPane(colorizer, algorithmRegistry));

    // WHEN
    runOnJavaFx(() -> pane.updateAnalysis(PageAnalysisTestFactory.createPageAnalysis(TITLE, TEXT)));

    // THEN
    Assertions.assertThat(callOnJavaFx(() -> displayedTexts(pane)))
        .containsExactly("Error No. 48 (2)");
  }

  @DisplayName("should display only the algorithm name for a single error")
  @Test
  void displaySingleError() throws ExecutionException, InterruptedException, TimeoutException {
    // GIVEN
    final PageAnalysisCheckPane pane =
        callOnJavaFx(() -> new PageAnalysisCheckPane(colorizer, algorithmRegistry));

    // WHEN
    runOnJavaFx(
        () ->
            pane.updateAnalysis(
                PageAnalysisTestFactory.createPageAnalysis(TITLE, FIRST_LINK + " est une ville.")));

    // THEN
    Assertions.assertThat(callOnJavaFx(() -> displayedTexts(pane))).containsExactly("Error No. 48");
  }

  @DisplayName("should list no algorithm when no error is detected in the page")
  @Test
  void listNoAlgorithm() throws ExecutionException, InterruptedException, TimeoutException {
    // GIVEN
    final PageAnalysisCheckPane pane =
        callOnJavaFx(() -> new PageAnalysisCheckPane(colorizer, algorithmRegistry));

    // WHEN
    runOnJavaFx(
        () -> pane.updateAnalysis(PageAnalysisTestFactory.createPageAnalysis("Marseille", TEXT)));

    // THEN
    Assertions.assertThat(pane.getErrorsListView().getItems()).isEmpty();
  }

  @DisplayName(
      "should highlight the errors of the selected algorithm over the syntax and select the first one")
  @Test
  void selectAlgorithm() throws ExecutionException, InterruptedException, TimeoutException {
    // GIVEN
    final PageAnalysisCheckPane pane =
        callOnJavaFx(() -> new PageAnalysisCheckPane(colorizer, algorithmRegistry));
    runOnJavaFx(() -> pane.updateAnalysis(PageAnalysisTestFactory.createPageAnalysis(TITLE, TEXT)));

    // WHEN
    runOnJavaFx(() -> pane.getErrorsListView().getSelectionModel().selectFirst());

    // THEN
    final PageAnalysisArea area = pane.getArea();
    final String errorStyle =
        styleRegistry.getStyle(PageAnalysisStylePropertiesInitializer.INTERNAL_LINK)
            + styleRegistry.getStyle(PageAnalysisStylePropertiesInitializer.CHECK_ERROR);
    Assertions.assertThat(callOnJavaFx(area::getSelection))
        .isEqualTo(new IndexRange(0, FIRST_LINK.length()));
    Assertions.assertThat(callOnJavaFx(() -> area.getStyleOfChar(0))).isEqualTo(errorStyle);
    Assertions.assertThat(callOnJavaFx(() -> area.getStyleOfChar(SECOND_LINK_BEGIN)))
        .isEqualTo(errorStyle);
    Assertions.assertThat(callOnJavaFx(() -> area.getStyleOfChar(OTHER_LINK_BEGIN)))
        .isEqualTo(styleRegistry.getStyle(PageAnalysisStylePropertiesInitializer.INTERNAL_LINK));
  }

  @DisplayName("should select the first error again when double-clicking on the algorithm")
  @Test
  void doubleClickAlgorithm() throws ExecutionException, InterruptedException, TimeoutException {
    // GIVEN
    final PageAnalysisCheckPane pane =
        callOnJavaFx(() -> new PageAnalysisCheckPane(colorizer, algorithmRegistry));
    runOnJavaFx(
        () -> {
          pane.updateAnalysis(PageAnalysisTestFactory.createPageAnalysis(TITLE, TEXT));
          pane.getErrorsListView().getSelectionModel().selectFirst();
          pane.getArea().selectRange(OTHER_LINK_BEGIN, OTHER_LINK_BEGIN);
        });

    // WHEN
    runOnJavaFx(() -> displayedCells(pane).getFirst().fireEvent(createClickEvent(2)));

    // THEN
    Assertions.assertThat(callOnJavaFx(pane.getArea()::getSelection))
        .isEqualTo(new IndexRange(0, FIRST_LINK.length()));
  }

  @DisplayName("should not select the first error again when clicking once on the algorithm")
  @Test
  void clickAlgorithm() throws ExecutionException, InterruptedException, TimeoutException {
    // GIVEN
    final PageAnalysisCheckPane pane =
        callOnJavaFx(() -> new PageAnalysisCheckPane(colorizer, algorithmRegistry));
    runOnJavaFx(
        () -> {
          pane.updateAnalysis(PageAnalysisTestFactory.createPageAnalysis(TITLE, TEXT));
          pane.getErrorsListView().getSelectionModel().selectFirst();
          pane.getArea().selectRange(OTHER_LINK_BEGIN, OTHER_LINK_BEGIN);
        });

    // WHEN
    runOnJavaFx(() -> displayedCells(pane).getFirst().fireEvent(createClickEvent(1)));

    // THEN
    Assertions.assertThat(callOnJavaFx(pane.getArea()::getSelection))
        .isEqualTo(new IndexRange(OTHER_LINK_BEGIN, OTHER_LINK_BEGIN));
  }

  @DisplayName("should remove the highlight when no algorithm is selected")
  @Test
  void clearSelection() throws ExecutionException, InterruptedException, TimeoutException {
    // GIVEN
    final PageAnalysisCheckPane pane =
        callOnJavaFx(() -> new PageAnalysisCheckPane(colorizer, algorithmRegistry));
    runOnJavaFx(
        () -> {
          pane.updateAnalysis(PageAnalysisTestFactory.createPageAnalysis(TITLE, TEXT));
          pane.getErrorsListView().getSelectionModel().selectFirst();
        });

    // WHEN
    runOnJavaFx(() -> pane.getErrorsListView().getSelectionModel().clearSelection());

    // THEN
    Assertions.assertThat(callOnJavaFx(() -> pane.getArea().getStyleOfChar(0)))
        .isEqualTo(styleRegistry.getStyle(PageAnalysisStylePropertiesInitializer.INTERNAL_LINK));
  }

  private static List<String> displayedTexts(final PageAnalysisCheckPane pane) {
    return displayedCells(pane).stream().map(Labeled::getText).toList();
  }

  private static List<Labeled> displayedCells(final PageAnalysisCheckPane pane) {
    if (pane.getScene() == null) {
      new Scene(pane, 800, 600);
    }
    pane.applyCss();
    pane.layout();
    return pane.getErrorsListView().lookupAll(".list-cell").stream()
        .map(Labeled.class::cast)
        .filter(cell -> Objects.nonNull(cell.getText()))
        .toList();
  }

  private static MouseEvent createClickEvent(final int clickCount) {
    return new MouseEvent(
        MouseEvent.MOUSE_CLICKED,
        0,
        0,
        0,
        0,
        MouseButton.PRIMARY,
        clickCount,
        false,
        false,
        false,
        false,
        false,
        false,
        false,
        false,
        false,
        false,
        null);
  }
}
