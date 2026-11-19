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
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
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
import org.wpcleaner.api.api.query.prop.links.Link;
import org.wpcleaner.api.api.query.prop.links.LinksQuery;
import org.wpcleaner.api.api.query.prop.links.MockApiLinks;
import org.wpcleaner.api.api.query.prop.links.Page;
import org.wpcleaner.api.api.query.prop.linkshere.LinkHere;
import org.wpcleaner.api.api.query.prop.linkshere.LinksHereQuery;
import org.wpcleaner.api.api.query.prop.linkshere.MockApiLinksHere;
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
  @Autowired private MockApiLinks mockApiLinks;
  @Autowired private MockApiLinksHere mockApiLinksHere;
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

  @DisplayName("should list the articles of the main namespace linked from the page")
  @Test
  void internalLinksInMainNamespace()
      throws InterruptedException, ExecutionException, TimeoutException {
    // GIVEN
    final JavaFxMainWindow mainWindow = displayMainWindow();
    givenInternalLinks();

    // WHEN
    fireInternalLinks(mainWindow, "Paris", "Articles in main namespace");
    final JavaFxPageListWindow listWindow =
        waitForWindow(windowsRegistry, JavaFxPageListWindow.class);

    // THEN
    Assertions.assertThat(callOnJavaFx(() -> listWindow.getStage().getTitle()))
        .isEqualTo("Internal links in Paris");
    Assertions.assertThat(getPages(listWindow)).containsExactly("Bordeaux", "Marseille");
  }

  @DisplayName("should list the articles associated to the talk pages linked from the page")
  @Test
  void internalLinksWithTalkPagesConverted()
      throws InterruptedException, ExecutionException, TimeoutException {
    // GIVEN
    final JavaFxMainWindow mainWindow = displayMainWindow();
    givenInternalLinks();

    // WHEN
    fireInternalLinks(mainWindow, "Paris", "Articles associated to talk pages");
    final JavaFxPageListWindow listWindow =
        waitForWindow(windowsRegistry, JavaFxPageListWindow.class);

    // THEN
    Assertions.assertThat(getPages(listWindow)).containsExactly("Bordeaux", "Lyon", "Marseille");
  }

  @DisplayName("should list the pages of all namespaces linked from the page")
  @Test
  void internalLinksInAllNamespaces()
      throws InterruptedException, ExecutionException, TimeoutException {
    // GIVEN
    final JavaFxMainWindow mainWindow = displayMainWindow();
    givenInternalLinks();

    // WHEN
    fireInternalLinks(mainWindow, "Paris", "All articles");
    final JavaFxPageListWindow listWindow =
        waitForWindow(windowsRegistry, JavaFxPageListWindow.class);

    // THEN
    Assertions.assertThat(getPages(listWindow))
        .containsExactly(
            "Bordeaux", "Marseille", "Talk:Lyon", "User:Jane Doe", "User talk:John Doe");
  }

  @DisplayName("should warn when listing internal links without page name")
  @Test
  void internalLinksWithoutPage()
      throws InterruptedException, ExecutionException, TimeoutException {
    // GIVEN
    final JavaFxMainWindow mainWindow = displayMainWindow();

    // WHEN
    fireInternalLinks(mainWindow, " ", "All articles");

    // THEN
    Assertions.assertThat(closeAlert(mainWindow)).isEqualTo("The page name cannot be empty.");
  }

  @DisplayName("should list the sorted pages of all namespaces linking to the page")
  @Test
  void whatLinksHere() throws InterruptedException, ExecutionException, TimeoutException {
    // GIVEN
    final JavaFxMainWindow mainWindow = displayMainWindow();
    mockApiLinksHere
        .givenRetrieveLinksHereByTitle(
            WIKI, List.of("Paris"), LinksHereQuery.emptyBuilder().limit(Limit.max()).build())
        .willReturn(
            List.of(
                new org.wpcleaner.api.api.query.prop.linkshere.Page(
                    List.of(
                        new LinkHere(CommonNamespaces.USER.id, 3, false, "User:Jane Doe"),
                        new LinkHere(CommonNamespaces.MAIN.id, 1, false, "Marseille"),
                        new LinkHere(CommonNamespaces.MAIN_TALK.id, 4, false, "Talk:Lyon"),
                        new LinkHere(CommonNamespaces.MAIN.id, 2, true, "Bordeaux")),
                    false,
                    CommonNamespaces.MAIN.id,
                    681_159,
                    "Paris")));

    // WHEN
    fireWhatLinksHere(mainWindow, "Paris");
    final JavaFxPageListWindow listWindow =
        waitForWindow(windowsRegistry, JavaFxPageListWindow.class);

    // THEN
    Assertions.assertThat(callOnJavaFx(() -> listWindow.getStage().getTitle()))
        .isEqualTo("Links to Paris");
    Assertions.assertThat(getPages(listWindow))
        .containsExactly("Bordeaux", "Marseille", "Talk:Lyon", "User:Jane Doe");
  }

  @DisplayName("should warn when listing what links here without page name")
  @Test
  void whatLinksHereWithoutPage()
      throws InterruptedException, ExecutionException, TimeoutException {
    // GIVEN
    final JavaFxMainWindow mainWindow = displayMainWindow();

    // WHEN
    fireWhatLinksHere(mainWindow, " ");

    // THEN
    Assertions.assertThat(closeAlert(mainWindow)).isEqualTo("The page name cannot be empty.");
  }

  private void givenInternalLinks() {
    mockApiLinks
        .givenRetrieveLinksByTitle(
            WIKI, List.of("Paris"), LinksQuery.emptyBuilder().limit(Limit.max()).build())
        .willReturn(
            List.of(
                new Page(
                    List.of(
                        new Link(CommonNamespaces.MAIN.id, "Marseille"),
                        new Link(CommonNamespaces.MAIN_TALK.id, "Talk:Lyon"),
                        new Link(CommonNamespaces.USER.id, "User:Jane Doe"),
                        new Link(CommonNamespaces.USER_TALK.id, "User talk:John Doe"),
                        new Link(CommonNamespaces.MAIN.id, "Bordeaux"),
                        new Link(CommonNamespaces.MAIN.id, "Marseille")),
                    false,
                    CommonNamespaces.MAIN.id,
                    681_159,
                    "Paris")));
  }

  @SuppressWarnings("unchecked")
  private void fireInternalLinks(
      final JavaFxMainWindow mainWindow, final String pageName, final String itemText)
      throws InterruptedException, ExecutionException, TimeoutException {
    final MenuButton button =
        waitForJavaFx(() -> find(mainWindow, ByPagePanel.INTERNAL_LINKS_ID, MenuButton.class));
    runOnJavaFx(() -> lookup(mainWindow, PageInput.PAGE_ID, ComboBox.class).setValue(pageName));
    final MenuItem item =
        callOnJavaFx(
            () ->
                button.getItems().stream()
                    .filter(menuItem -> itemText.equals(menuItem.getText()))
                    .findFirst()
                    .orElseThrow());
    Platform.runLater(item::fire);
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

  @SuppressWarnings("unchecked")
  private void fireWhatLinksHere(final JavaFxMainWindow mainWindow, final String pageName)
      throws InterruptedException, ExecutionException, TimeoutException {
    final Button button =
        waitForJavaFx(() -> find(mainWindow, ByPagePanel.WHAT_LINKS_HERE_ID, Button.class));
    runOnJavaFx(() -> lookup(mainWindow, PageInput.PAGE_ID, ComboBox.class).setValue(pageName));
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
