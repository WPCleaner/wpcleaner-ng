package org.wpcleaner.application.gui.javafx.core.pageanalysis;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import javafx.scene.control.Button;
import javafx.scene.control.Labeled;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.wpcleaner.api.analysis.PageAnalysisTestFactory;
import org.wpcleaner.application.gui.GuiSpringBootTest;
import org.wpcleaner.application.gui.core.desktop.DesktopService;
import org.wpcleaner.application.gui.javafx.JavaFxTest;

@GuiSpringBootTest
@Import(PageAnalysisCheckPaneFactory.class)
class PageAnalysisCheckPaneDescriptionTest extends JavaFxTest {

  private static final String TITLE = "Paris";
  private static final String TEXT = "Voir [[Paris]].";
  private static final String PRIORITY = "error_048_prio_frwiki";
  private static final String LINK = "P:CS/048";

  @Autowired private PageAnalysisCheckPaneFactory paneFactory;
  @Autowired private DesktopService desktopService;

  @DisplayName("should display the description of the algorithm as plain text")
  @Test
  void displayDescription() throws ExecutionException, InterruptedException, TimeoutException {
    // GIVEN
    paneFactory.givenCheckWikiConfiguration(
        Map.of(
            PRIORITY,
            "3",
            "error_048_desc_frwiki",
            "Un <a href=\"https://fr.wikipedia.org\">lien interne</a> vers le titre :"
                + " remplacer [[Titre]] par &#39;&#39;&#39;Titre&#39;&#39;&#39; &amp;lt;"));
    final PageAnalysisCheckPane pane = createPaneWithAnalysis();

    // WHEN
    final Tooltip tooltip = callOnJavaFx(() -> getDisplayedCell(pane).getTooltip());

    // THEN
    Assertions.assertThat(tooltip.getText())
        .isEqualTo("Un lien interne vers le titre : remplacer [[Titre]] par '''Titre''' &lt;");
  }

  @DisplayName("should display no tooltip for an algorithm without description")
  @Test
  void displayNoDescription() throws ExecutionException, InterruptedException, TimeoutException {
    // GIVEN
    paneFactory.givenCheckWikiConfiguration(Map.of(PRIORITY, "3"));
    final PageAnalysisCheckPane pane = createPaneWithAnalysis();

    // WHEN
    final Tooltip tooltip = callOnJavaFx(() -> getDisplayedCell(pane).getTooltip());

    // THEN
    Assertions.assertThat(tooltip).isNull();
  }

  @DisplayName("should display an icon with a tooltip for opening the page describing the error")
  @Test
  void displayLinkButton() throws ExecutionException, InterruptedException, TimeoutException {
    // GIVEN
    paneFactory.givenCheckWikiConfiguration(Map.of(PRIORITY, "3"));
    final PageAnalysisCheckPane pane = createPaneWithAnalysis();

    // WHEN
    final Button button = callOnJavaFx(() -> getLinkButton(pane));

    // THEN
    Assertions.assertThat(button.getText()).isEmpty();
    Assertions.assertThat(button.getGraphic()).isInstanceOf(ImageView.class);
    Assertions.assertThat(button.getTooltip().getText())
        .isEqualTo("Open the page describing the error");
  }

  @DisplayName("should open the page describing the selected algorithm")
  @Test
  void openLink() throws ExecutionException, InterruptedException, TimeoutException {
    // GIVEN
    paneFactory.givenCheckWikiConfiguration(Map.of(PRIORITY, "3", "error_048_link_frwiki", LINK));
    final PageAnalysisCheckPane pane = createPaneWithAnalysis();
    final Button button = callOnJavaFx(() -> getLinkButton(pane));
    Assertions.assertThat(button.isDisable()).isTrue();
    runOnJavaFx(() -> pane.getErrorsListView().getSelectionModel().selectFirst());
    Assertions.assertThat(button.isDisable()).isFalse();

    // WHEN
    runOnJavaFx(button::fire);

    // THEN
    BDDMockito.then(desktopService)
        .should(Mockito.timeout(5000))
        .browse(PageAnalysisTestFactory.WIKI.pageUrl(LINK));
  }

  @DisplayName("should not allow opening a page for an algorithm without link")
  @Test
  void openNoLink() throws ExecutionException, InterruptedException, TimeoutException {
    // GIVEN
    paneFactory.givenCheckWikiConfiguration(Map.of(PRIORITY, "3"));
    final PageAnalysisCheckPane pane = createPaneWithAnalysis();
    final Button button = callOnJavaFx(() -> getLinkButton(pane));

    // WHEN
    runOnJavaFx(() -> pane.getErrorsListView().getSelectionModel().selectFirst());

    // THEN
    Assertions.assertThat(button.isDisable()).isTrue();
  }

  private PageAnalysisCheckPane createPaneWithAnalysis()
      throws ExecutionException, InterruptedException, TimeoutException {
    return callOnJavaFx(
        () -> {
          final PageAnalysisCheckPane pane = paneFactory.createPane();
          pane.updateAnalysis(PageAnalysisTestFactory.createPageAnalysis(TITLE, TEXT));
          return pane;
        });
  }

  private static Labeled getDisplayedCell(final PageAnalysisCheckPane pane) {
    return PageAnalysisCheckPaneFactory.displayedCells(pane).getFirst();
  }

  private static Button getLinkButton(final PageAnalysisCheckPane pane) {
    PageAnalysisCheckPaneFactory.show(pane);
    return lookup(pane, CheckErrorsPane.LINK_ID, Button.class);
  }
}
