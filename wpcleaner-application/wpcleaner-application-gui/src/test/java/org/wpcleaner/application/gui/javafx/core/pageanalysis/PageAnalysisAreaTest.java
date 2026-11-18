package org.wpcleaner.application.gui.javafx.core.pageanalysis;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.wpcleaner.api.analysis.PageAnalysisTestFactory;
import org.wpcleaner.application.gui.GuiSpringBootTest;
import org.wpcleaner.application.gui.javafx.JavaFxTest;
import org.wpcleaner.application.gui.javafx.core.pageanalysis.coloration.PageSyntaxColorizer;

@GuiSpringBootTest
class PageAnalysisAreaTest extends JavaFxTest {

  private static final String TITLE = "Utilisateur:Exemple";
  private static final String LEFT_TO_RIGHT_TEXT =
      "'''Paris''' est la capitale de la France et la commune la plus peuplée du pays.";
  private static final String RIGHT_TO_LEFT_TEXT =
      "'''• درست العربيّة والاجتماعيّات بمعهد المعلّمين المركزيّ ببغداد حتّى 1993 وهو اختصاص"
          + " يجمع بين تدريس العربيّة والبيداغوجيا وعلم نفس التّلميذ.''' \n[[Catégorie:Test]]";
  private static final double WINDOW_SIZE = 300;

  @Autowired private PageSyntaxColorizer colorizer;

  @DisplayName("should wrap left-to-right text")
  @Test
  void wrapLeftToRightText() throws ExecutionException, InterruptedException, TimeoutException {
    // GIVEN
    final PageAnalysisArea area = createArea();

    // WHEN
    showText(area, LEFT_TO_RIGHT_TEXT);

    // THEN
    Assertions.assertThat(area.isWrapText()).isTrue();
  }

  @DisplayName("should display right-to-left text without wrapping it")
  @Test
  void displayRightToLeftText() throws ExecutionException, InterruptedException, TimeoutException {
    // GIVEN
    final PageAnalysisArea area = createArea();

    // WHEN
    showText(area, RIGHT_TO_LEFT_TEXT);

    // THEN
    Assertions.assertThat(area.isWrapText()).isFalse();
    Assertions.assertThat(area.getText()).isEqualTo(RIGHT_TO_LEFT_TEXT);
  }

  @DisplayName("should wrap again left-to-right text after right-to-left text")
  @Test
  void wrapAgainAfterRightToLeftText()
      throws ExecutionException, InterruptedException, TimeoutException {
    // GIVEN
    final PageAnalysisArea area = createArea();
    showText(area, RIGHT_TO_LEFT_TEXT);

    // WHEN
    showText(area, LEFT_TO_RIGHT_TEXT);

    // THEN
    Assertions.assertThat(area.isWrapText()).isTrue();
  }

  private PageAnalysisArea createArea()
      throws ExecutionException, InterruptedException, TimeoutException {
    return callOnJavaFx(() -> new PageAnalysisArea(colorizer));
  }

  private void showText(final PageAnalysisArea area, final String text)
      throws ExecutionException, InterruptedException, TimeoutException {
    runOnJavaFx(
        () -> {
          final Stage stage = new Stage();
          stage.setScene(new Scene(new StackPane(area), WINDOW_SIZE, WINDOW_SIZE));
          stage.show();
          area.updateAnalysis(PageAnalysisTestFactory.createPageAnalysis(TITLE, text));
          stage.getScene().getRoot().layout();
          stage.close();
        });
  }
}
