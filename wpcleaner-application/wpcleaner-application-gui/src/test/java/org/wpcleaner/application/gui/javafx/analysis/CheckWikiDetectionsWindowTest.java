package org.wpcleaner.application.gui.javafx.analysis;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.stream.IntStream;
import javafx.scene.control.TableView;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.wpcleaner.api.api.CurrentUserService;
import org.wpcleaner.api.wiki.definition.WikiDefinition;
import org.wpcleaner.api.wiki.definition.WikipediaDefinitions;
import org.wpcleaner.application.base.ApplicationLogin;
import org.wpcleaner.application.gui.GuiSpringBootTest;
import org.wpcleaner.application.gui.javafx.JavaFxTest;
import org.wpcleaner.application.gui.javafx.JavaFxWindowsRegistry;
import org.wpcleaner.application.gui.javafx.core.pageanalysis.PageAnalysisArea;
import org.wpcleaner.lib.check.configuration.CheckWikiConfiguration;
import org.wpcleaner.lib.check.configuration.CheckWikiConfigurationRepository;
import org.wpcleaner.lib.check.server.CheckWikiDetection;

@GuiSpringBootTest
class CheckWikiDetectionsWindowTest extends JavaFxTest {

  private static final WikiDefinition WIKI = WikipediaDefinitions.FR;
  private static final String TEXT = "== Histoire ==\n[[Paris]] est une ville.";

  @Autowired private JavaFxAnalysisWindowServices services;
  @Autowired private CurrentUserService currentUserService;
  @Autowired private ApplicationLogin guiLogin;
  @Autowired private JavaFxWindowsRegistry windowsRegistry;
  @Autowired private CheckWikiConfigurationRepository checkWikiRepository;

  @BeforeEach
  void setUp() {
    currentUserService.logout();
    guiLogin.login(WIKI, "Jane Doe", ApplicationLogin.USER_NAMESPACES);
    checkWikiRepository.setConfiguration(
        WIKI,
        CheckWikiConfiguration.of(
            Map.of("error_048_head_frwiki", "Lien vers l’article"), Map.of(), "frwiki"));
  }

  @AfterEach
  void tearDown() throws InterruptedException, ExecutionException, TimeoutException {
    runOnJavaFx(windowsRegistry::closeAllWindows);
    currentUserService.logout();
  }

  @DisplayName("should display the errors detected by Check Wiki")
  @Test
  void displayDetections() throws InterruptedException, ExecutionException, TimeoutException {
    // GIVEN
    final PageAnalysisArea area = createArea();

    // WHEN
    final CheckWikiDetectionsWindow window = openWindow(area);

    // THEN
    Assertions.assertThat(callOnJavaFx(() -> window.getStage().getTitle()))
        .isEqualTo("Errors detected by Check Wiki in Paris");
    Assertions.assertThat(callOnJavaFx(() -> getDisplayedDetections(window)))
        .containsExactly(
            List.of("48", "Lien vers l’article", "15", "[[paris]]"),
            List.of("499", "", "3", "<absent>"),
            List.of("64", "Error No. 64", "", "In the whitelist"),
            List.of("498", "", "0", ""));
  }

  @DisplayName("should select the notice in the text, ignoring case, or else the location")
  @Test
  void selectDetections() throws InterruptedException, ExecutionException, TimeoutException {
    // GIVEN
    final PageAnalysisArea area = createArea();
    final CheckWikiDetectionsWindow window = openWindow(area);

    // WHEN
    runOnJavaFx(() -> getTable(window).getSelectionModel().select(0));

    // THEN
    Assertions.assertThat(callOnJavaFx(area::getSelectedText)).isEqualTo("[[Paris]]");
    runOnJavaFx(() -> getTable(window).getSelectionModel().select(1));
    Assertions.assertThat(callOnJavaFx(area::getSelectedText)).isEmpty();
    Assertions.assertThat(callOnJavaFx(area::getCaretPosition)).isEqualTo(3);
    runOnJavaFx(() -> getTable(window).getSelectionModel().select(2));
    Assertions.assertThat(callOnJavaFx(area::getCaretPosition)).isEqualTo(3);
    runOnJavaFx(() -> getTable(window).getSelectionModel().select(3));
    Assertions.assertThat(callOnJavaFx(area::getCaretPosition)).isZero();
  }

  @DisplayName("should select a location or a notice at the ends of the text, but not after")
  @Test
  void selectDetectionsAtTextEnd()
      throws InterruptedException, ExecutionException, TimeoutException {
    // GIVEN
    final PageAnalysisArea area = createArea();
    final CheckWikiDetectionsWindow window =
        openWindow(
            area,
            List.of(
                new CheckWikiDetection(1, TEXT.length(), "", false),
                new CheckWikiDetection(2, TEXT.length() + 1, "", false),
                new CheckWikiDetection(3, 0, "VILLE.", false),
                new CheckWikiDetection(4, 5, "== HISTOIRE", false)));

    // WHEN
    runOnJavaFx(
        () -> {
          area.moveTo(0);
          getTable(window).getSelectionModel().select(0);
        });

    // THEN
    Assertions.assertThat(callOnJavaFx(area::getCaretPosition)).isEqualTo(TEXT.length());
    runOnJavaFx(
        () -> {
          area.moveTo(0);
          getTable(window).getSelectionModel().select(1);
        });
    Assertions.assertThat(callOnJavaFx(area::getCaretPosition)).isZero();
    runOnJavaFx(() -> getTable(window).getSelectionModel().select(2));
    Assertions.assertThat(callOnJavaFx(area::getSelectedText)).isEqualTo("ville.");
    runOnJavaFx(() -> getTable(window).getSelectionModel().select(3));
    Assertions.assertThat(callOnJavaFx(area::getSelectedText)).isEqualTo("== Histoire");
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

  private CheckWikiDetectionsWindow openWindow(final PageAnalysisArea area)
      throws InterruptedException, ExecutionException, TimeoutException {
    return openWindow(
        area,
        List.of(
            new CheckWikiDetection(48, 15, "[[paris]]", false),
            new CheckWikiDetection(499, 3, "<absent>", false),
            new CheckWikiDetection(64, CheckWikiDetection.UNKNOWN_LOCATION, "", true),
            new CheckWikiDetection(498, 0, "", false)));
  }

  private CheckWikiDetectionsWindow openWindow(
      final PageAnalysisArea area, final List<CheckWikiDetection> detections)
      throws InterruptedException, ExecutionException, TimeoutException {
    return callOnJavaFx(
        () -> new CheckWikiDetectionsWindow(services, null, "Paris", detections, area));
  }

  @SuppressWarnings("unchecked")
  private static TableView<CheckWikiDetection> getTable(final CheckWikiDetectionsWindow window) {
    return lookup(window, CheckWikiDetectionsWindow.DETECTIONS_ID, TableView.class);
  }

  private static List<List<Object>> getDisplayedDetections(final CheckWikiDetectionsWindow window) {
    final TableView<CheckWikiDetection> table = getTable(window);
    return IntStream.range(0, table.getItems().size())
        .mapToObj(
            row ->
                table.getColumns().stream()
                    .map(column -> (Object) column.getCellData(row))
                    .toList())
        .toList();
  }
}
