package org.wpcleaner.application.gui.javafx.main;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.client.MockRestServiceServer;
import org.wpcleaner.api.api.CurrentUserService;
import org.wpcleaner.api.api.query.Slot;
import org.wpcleaner.api.api.query.prop.revisions.MockApiRevisions;
import org.wpcleaner.api.api.query.prop.revisions.Page;
import org.wpcleaner.api.api.query.prop.revisions.Revision;
import org.wpcleaner.api.api.query.prop.revisions.RevisionSlot;
import org.wpcleaner.api.api.query.prop.revisions.RevisionsParameters;
import org.wpcleaner.api.api.query.prop.revisions.RevisionsQuery;
import org.wpcleaner.api.wiki.definition.WikiDefinition;
import org.wpcleaner.api.wiki.definition.WikipediaDefinitions;
import org.wpcleaner.application.gui.GuiLogin;
import org.wpcleaner.application.gui.GuiSpringBootTest;
import org.wpcleaner.application.gui.javafx.JavaFxTest;
import org.wpcleaner.application.gui.javafx.JavaFxWindowsRegistry;
import org.wpcleaner.application.gui.javafx.analysis.JavaFxAnalysisWindow;
import org.wpcleaner.application.gui.javafx.core.pageanalysis.CheckAlgorithmErrors;
import org.wpcleaner.application.gui.javafx.core.pageanalysis.CheckErrorsPane;
import org.wpcleaner.application.gui.javafx.pagelist.JavaFxPageListWindow;
import org.wpcleaner.lib.check.server.MockCheckWikiServer;

@GuiSpringBootTest
class ProjectsPanelTest extends JavaFxTest {

  private static final WikiDefinition WIKI = WikipediaDefinitions.FR;
  private static final String NAME = "Lien vers l’article";
  private static final String CHECK_WIKI_CONFIGURATION =
      " error_048_prio_frwiki=3 END\n error_048_head_frwiki=%s END".formatted(NAME);

  @Autowired private JavaFxMainWindowFactory mainWindowFactory;
  @Autowired private CurrentUserService currentUserService;
  @Autowired private GuiLogin guiLogin;
  @Autowired private JavaFxWindowsRegistry windowsRegistry;
  @Autowired private MockApiRevisions mockApiRevisions;
  @Autowired private MockRestServiceServer mediaWikiServer;

  @AfterEach
  void tearDown() throws InterruptedException, ExecutionException, TimeoutException {
    runOnJavaFx(windowsRegistry::closeAllWindows);
    currentUserService.logout();
    mediaWikiServer.reset();
  }

  @DisplayName("should list the Check Wiki algorithms active on the wiki")
  @Test
  void listCheckWikiAlgorithms() throws InterruptedException, ExecutionException, TimeoutException {
    // GIVEN
    login(CHECK_WIKI_CONFIGURATION);

    // WHEN
    final MenuButton button = displayCheckWikiButton();

    // THEN
    Assertions.assertThat(button.isDisable()).isFalse();
    Assertions.assertThat(button.getItems()).extracting(MenuItem::getText).containsExactly(NAME);
  }

  @DisplayName("should disable the Check Wiki button without active algorithm")
  @Test
  void listNoCheckWikiAlgorithm()
      throws InterruptedException, ExecutionException, TimeoutException {
    // GIVEN
    login(" error_048_prio_frwiki=0 END");

    // WHEN
    final MenuButton button = displayCheckWikiButton();

    // THEN
    Assertions.assertThat(button.isDisable()).isTrue();
  }

  @DisplayName("should open the pages of an algorithm and analyze them with the algorithm selected")
  @Test
  void openCheckWikiPages() throws InterruptedException, ExecutionException, TimeoutException {
    // GIVEN
    login(CHECK_WIKI_CONFIGURATION);
    final MenuButton button = displayCheckWikiButton();
    new MockCheckWikiServer(mediaWikiServer).givenPages(WIKI, 48, 100, List.of("Paris", "Lyon"));
    givenPageContent("Paris", "[[Paris]] est une ville.");

    // WHEN
    runOnJavaFx(() -> button.getItems().getFirst().fire());
    final JavaFxPageListWindow listWindow =
        waitForWindow(windowsRegistry, JavaFxPageListWindow.class);
    @SuppressWarnings("unchecked")
    final ListView<String> pages =
        waitForJavaFx(
            () ->
                Optional.of(lookup(listWindow, JavaFxPageListWindow.PAGES_ID, ListView.class))
                    .filter(list -> !list.getItems().isEmpty()));
    runOnJavaFx(
        () -> {
          pages.getSelectionModel().selectFirst();
          pages.fireEvent(
              new KeyEvent(
                  KeyEvent.KEY_PRESSED, "", "", KeyCode.ENTER, false, false, false, false));
        });

    // THEN
    Assertions.assertThat(callOnJavaFx(() -> listWindow.getStage().getTitle()))
        .isEqualTo("Check Wiki: " + NAME);
    Assertions.assertThat(callOnJavaFx(() -> List.copyOf(pages.getItems())))
        .containsExactly("Paris", "Lyon");
    final JavaFxAnalysisWindow analysisWindow =
        waitForWindow(windowsRegistry, JavaFxAnalysisWindow.class);
    final CheckAlgorithmErrors selectedErrors =
        waitForJavaFx(
            () ->
                find(analysisWindow, CheckErrorsPane.ERRORS_ID, ListView.class)
                    .map(list -> list.getSelectionModel().getSelectedItem())
                    .map(CheckAlgorithmErrors.class::cast));
    Assertions.assertThat(selectedErrors.algorithm().number()).isEqualTo(48);
    mediaWikiServer.verify();
  }

  private void login(final String checkWikiConfiguration) {
    guiLogin.login(
        WIKI,
        "Jane Doe",
        GuiLogin.LoginData.of(GuiLogin.USER_NAMESPACES)
            .withCheckWikiConfiguration(checkWikiConfiguration));
  }

  private MenuButton displayCheckWikiButton()
      throws InterruptedException, ExecutionException, TimeoutException {
    mainWindowFactory.displayMainWindow();
    final JavaFxMainWindow mainWindow = waitForWindow(windowsRegistry, JavaFxMainWindow.class);
    return waitForJavaFx(() -> find(mainWindow, ProjectsPanel.CHECK_WIKI_ID, MenuButton.class));
  }

  private void givenPageContent(final String title, final String content) {
    final Revision revision =
        new Revision(
            null,
            false,
            null,
            null,
            123,
            null,
            null,
            Map.of(Slot.MAIN.value, new RevisionSlot(content, null, null, null)),
            List.of(),
            Instant.parse("2026-11-01T10:00:00Z"),
            null,
            null);
    mockApiRevisions
        .givenRetrieveRevisionsByTitle(
            WIKI,
            List.of(title),
            RevisionsQuery.emptyBuilder()
                .properties(
                    Set.of(
                        RevisionsParameters.Properties.CONTENT,
                        RevisionsParameters.Properties.TIMESTAMP))
                .slots(Set.of(Slot.MAIN))
                .build())
        .willReturn(List.of(new Page(false, 0, 1234, List.of(revision), title)));
  }
}
