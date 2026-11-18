package org.wpcleaner.application.gui.javafx.main;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.wpcleaner.api.api.CurrentUserService;
import org.wpcleaner.api.api.Limit;
import org.wpcleaner.api.api.query.list.search.MockApiSearch;
import org.wpcleaner.api.api.query.list.search.SearchPage;
import org.wpcleaner.api.api.query.list.search.SearchQuery;
import org.wpcleaner.api.repository.namespace.CommonNamespaces;
import org.wpcleaner.api.repository.namespace.Namespace;
import org.wpcleaner.api.repository.namespace.NamespaceRepository;
import org.wpcleaner.api.wiki.definition.WikiDefinition;
import org.wpcleaner.api.wiki.definition.WikipediaDefinitions;
import org.wpcleaner.application.base.ApplicationLogin;
import org.wpcleaner.application.gui.GuiSpringBootTest;
import org.wpcleaner.application.gui.javafx.JavaFxTest;
import org.wpcleaner.application.gui.javafx.JavaFxWindowsRegistry;
import org.wpcleaner.application.gui.javafx.pagelist.JavaFxPageListWindow;

@GuiSpringBootTest
class ByPagePanelTest extends JavaFxTest {

  private static final WikiDefinition WIKI = WikipediaDefinitions.FR;

  @Autowired private CurrentUserService currentUserService;
  @Autowired private ApplicationLogin guiLogin;
  @Autowired private JavaFxMainWindowFactory mainWindowFactory;
  @Autowired private MockApiSearch mockApiSearch;
  @Autowired private NamespaceRepository namespaceRepository;
  @Autowired private JavaFxWindowsRegistry windowsRegistry;

  @AfterEach
  void tearDown() throws InterruptedException, ExecutionException, TimeoutException {
    runOnJavaFx(windowsRegistry::closeAllWindows);
    currentUserService.logout();
  }

  @DisplayName("should list the sorted titles of the main namespace containing the searched text")
  @Test
  void searchTitles() throws InterruptedException, ExecutionException, TimeoutException {
    // GIVEN
    final JavaFxMainWindow mainWindow = displayMainWindow();
    mockApiSearch
        .givenSearchPages(WIKI, "intitle:\"Café \\\"noir\\\"\"", searchOptions(Namespace.MAIN))
        .willReturn(
            List.of(
                searchPage(0, "Un café \"noir\""),
                searchPage(0, "Café \"noir\""),
                searchPage(0, "Un café \"noir\"")));

    // WHEN
    fireSearchTitles(mainWindow, "café \"noir\"");
    final JavaFxPageListWindow listWindow =
        waitForWindow(windowsRegistry, JavaFxPageListWindow.class);

    // THEN
    Assertions.assertThat(callOnJavaFx(() -> listWindow.getStage().getTitle()))
        .isEqualTo("Search results for café \"noir\"");
    Assertions.assertThat(getPages(listWindow))
        .containsExactly("Café \"noir\"", "Un café \"noir\"");
  }

  @DisplayName("should search the titles in the namespace of the searched text")
  @Test
  void searchTitlesInNamespace() throws InterruptedException, ExecutionException, TimeoutException {
    // GIVEN
    final JavaFxMainWindow mainWindow = displayMainWindow();
    final Namespace userNamespace =
        Namespace.findNamespace(namespaceRepository.getNamespaces(WIKI), CommonNamespaces.USER.id)
            .orElseThrow();
    mockApiSearch
        .givenSearchPages(WIKI, "intitle:\"Jane\"", searchOptions(userNamespace))
        .willReturn(List.of(searchPage(userNamespace.id(), "User:Jane Doe")));

    // WHEN
    fireSearchTitles(mainWindow, "User:jane");
    final JavaFxPageListWindow listWindow =
        waitForWindow(windowsRegistry, JavaFxPageListWindow.class);

    // THEN
    Assertions.assertThat(getPages(listWindow)).containsExactly("User:Jane Doe");
  }

  @DisplayName("should warn when searching titles without text")
  @Test
  void searchTitlesWithoutText() throws InterruptedException, ExecutionException, TimeoutException {
    // GIVEN
    final JavaFxMainWindow mainWindow = displayMainWindow();

    // WHEN
    fireSearchTitles(mainWindow, " ");

    // THEN
    Assertions.assertThat(closeAlert(mainWindow)).isEqualTo("The page name cannot be empty.");
  }

  private JavaFxMainWindow displayMainWindow()
      throws InterruptedException, ExecutionException, TimeoutException {
    guiLogin.login(
        WIKI, "Jane Doe", ApplicationLogin.LoginData.of(ApplicationLogin.USER_NAMESPACES));
    mainWindowFactory.displayMainWindow();
    return waitForWindow(windowsRegistry, JavaFxMainWindow.class);
  }

  @SuppressWarnings("unchecked")
  private void fireSearchTitles(final JavaFxMainWindow mainWindow, final String text)
      throws InterruptedException, ExecutionException, TimeoutException {
    final Button button =
        waitForJavaFx(() -> find(mainWindow, ByPagePanel.SEARCH_TITLES_ID, Button.class));
    runOnJavaFx(() -> lookup(mainWindow, PageInput.PAGE_ID, ComboBox.class).setValue(text));
    Platform.runLater(button::fire);
  }

  private List<String> getPages(final JavaFxPageListWindow listWindow)
      throws InterruptedException, ExecutionException, TimeoutException {
    @SuppressWarnings("unchecked")
    final ListView<String> pages =
        waitForJavaFx(
            () ->
                Optional.of(lookup(listWindow, JavaFxPageListWindow.PAGES_ID, ListView.class))
                    .filter(list -> !list.getItems().isEmpty()));
    return callOnJavaFx(() -> List.copyOf(pages.getItems()));
  }

  private static SearchQuery searchOptions(final Namespace namespace) {
    return SearchQuery.emptyBuilder().limit(Limit.max()).namespace(Set.of(namespace)).build();
  }

  private static SearchPage searchPage(final int namespace, final String title) {
    return new SearchPage(
        null,
        null,
        null,
        namespace,
        title.hashCode(),
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        title,
        null,
        null);
  }
}
