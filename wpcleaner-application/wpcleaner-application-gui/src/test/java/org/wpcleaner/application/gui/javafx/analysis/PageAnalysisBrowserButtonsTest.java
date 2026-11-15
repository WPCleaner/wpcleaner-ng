package org.wpcleaner.application.gui.javafx.analysis;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.time.Instant;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.client.MockRestServiceServer;
import org.wpcleaner.api.api.CurrentUserService;
import org.wpcleaner.api.api.query.prop.revisions.MockApiRevisions;
import org.wpcleaner.api.wiki.definition.WikiDefinition;
import org.wpcleaner.api.wiki.definition.WikipediaDefinitions;
import org.wpcleaner.application.base.ApplicationLogin;
import org.wpcleaner.application.gui.GuiSpringBootTest;
import org.wpcleaner.application.gui.core.desktop.DesktopService;
import org.wpcleaner.application.gui.javafx.JavaFxTest;
import org.wpcleaner.application.gui.javafx.core.pageanalysis.PageAnalysisArea;

@GuiSpringBootTest
class PageAnalysisBrowserButtonsTest extends JavaFxTest {

  private static final WikiDefinition WIKI = WikipediaDefinitions.FR;
  private static final String TITLE = "Rock & roll";
  private static final String TEXT = "Le rock.";
  private static final long BROWSE_TIMEOUT = 5000;

  @Autowired private CurrentUserService currentUserService;
  @Autowired private ApplicationLogin guiLogin;
  @Autowired private JavaFxAnalysisWindowServices services;
  @Autowired private MockApiRevisions mockApiRevisions;
  @Autowired private MockRestServiceServer mediaWikiServer;
  @Autowired private DesktopService desktopService;

  @BeforeEach
  void setUp() {
    currentUserService.logout();
    guiLogin.login(WIKI, "Jane Doe", ApplicationLogin.USER_NAMESPACES);
  }

  @AfterEach
  void tearDown() throws InterruptedException, ExecutionException, TimeoutException {
    runOnJavaFx(services.windowsRegistry()::closeAllWindows);
    currentUserService.logout();
    mediaWikiServer.reset();
  }

  @DisplayName("should open the page in the browser")
  @Test
  void openPage() throws InterruptedException, ExecutionException, TimeoutException {
    // GIVEN
    final JavaFxAnalysisWindow window = openAnalysis();
    final Button button = callOnJavaFx(() -> getButton(window, PageAnalysisPanel.OPEN_PAGE_ID));

    // WHEN
    runOnJavaFx(button::fire);

    // THEN
    Assertions.assertThat(button.getGraphic()).isInstanceOf(ImageView.class);
    Assertions.assertThat(button.getTooltip().getText()).isEqualTo("Open page in browser");
    BDDMockito.then(desktopService)
        .should(BDDMockito.timeout(BROWSE_TIMEOUT))
        .browse("https://fr.wikipedia.org/wiki/Rock_&_roll");
  }

  @DisplayName("should open the page history in the browser")
  @Test
  void openHistory() throws InterruptedException, ExecutionException, TimeoutException {
    // GIVEN
    final JavaFxAnalysisWindow window = openAnalysis();
    final Button button = callOnJavaFx(() -> getButton(window, PageAnalysisPanel.OPEN_HISTORY_ID));

    // WHEN
    runOnJavaFx(button::fire);

    // THEN
    Assertions.assertThat(button.getGraphic()).isInstanceOf(ImageView.class);
    Assertions.assertThat(button.getTooltip().getText()).isEqualTo("Open page history in browser");
    BDDMockito.then(desktopService)
        .should(BDDMockito.timeout(BROWSE_TIMEOUT))
        .browse("https://fr.wikipedia.org/w/index.php?title=Rock_%26_roll&action=history");
  }

  private JavaFxAnalysisWindow openAnalysis()
      throws InterruptedException, ExecutionException, TimeoutException {
    AnalysisPageExpectations.givenPageContent(
        mockApiRevisions, WIKI, TITLE, TEXT, Instant.parse("2026-11-01T10:00:00Z"));
    final JavaFxAnalysisWindow window =
        callOnJavaFx(
            () -> {
              final JavaFxAnalysisWindow created = new JavaFxAnalysisWindow(services);
              created.analyze(TITLE);
              return created;
            });
    waitForJavaFx(
        () ->
            find(window, PageAnalysisArea.AREA_ID, PageAnalysisArea.class)
                .filter(area -> TEXT.equals(area.getText())));
    return window;
  }

  private static Button getButton(final JavaFxAnalysisWindow window, final String id) {
    return lookup(window, id, Button.class);
  }
}
