package org.wpcleaner.application.gui.javafx.main;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import javafx.scene.control.ListView;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import org.assertj.core.api.Assertions;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.client.MockRestServiceServer;
import org.wpcleaner.api.api.CurrentUserService;
import org.wpcleaner.api.api.Limit;
import org.wpcleaner.api.api.query.list.linterrors.LintErrorsQuery;
import org.wpcleaner.api.api.query.list.linterrors.MockApiLintErrors;
import org.wpcleaner.api.api.query.list.linterrors.PageLintError;
import org.wpcleaner.api.api.query.meta.siteinfo.Namespace;
import org.wpcleaner.api.wiki.definition.WikiDefinition;
import org.wpcleaner.api.wiki.definition.WikipediaDefinitions;
import org.wpcleaner.application.base.ApplicationLogin;
import org.wpcleaner.application.gui.GuiSpringBootTest;
import org.wpcleaner.application.gui.javafx.JavaFxTest;
import org.wpcleaner.application.gui.javafx.pagelist.JavaFxPageListWindow;

@GuiSpringBootTest
class LinterMenuButtonTest extends JavaFxTest {

  private static final WikiDefinition WIKI = WikipediaDefinitions.FR;
  private static final Map<String, List<String>> CATEGORIES =
      Map.of("high", List.of("duplicate-ids"), "low", List.of("obsolete-tag", "stripped-tag"));
  private static final List<Namespace> NAMESPACES =
      List.of(
          new Namespace(null, "first-letter", true, 0, "", "", false),
          new Namespace("Special", "first-letter", false, -1, "Spécial", "Spécial", false),
          new Namespace("User", "first-letter", false, 2, "Utilisateur", "Utilisateur", true));

  @Autowired private JavaFxMainWindowFactory mainWindowFactory;
  @Autowired private JavaFxMainWindowServices services;
  @Autowired private CurrentUserService currentUserService;
  @Autowired private ApplicationLogin guiLogin;
  @Autowired private MockApiLintErrors mockApiLintErrors;
  @Autowired private MockRestServiceServer mediaWikiServer;

  @AfterEach
  void tearDown() throws InterruptedException, ExecutionException, TimeoutException {
    runOnJavaFx(services.windowsRegistry()::closeAllWindows);
    currentUserService.logout();
    mediaWikiServer.reset();
  }

  @DisplayName("should list the Linter categories by priority, then the namespaces")
  @Test
  void listCategories() throws InterruptedException, ExecutionException, TimeoutException {
    // GIVEN
    login(CATEGORIES);
    services.linterCategories().setLabels(WIKI, Map.of("obsolete-tag", "Balises obsolètes"));

    // WHEN
    final MenuButton button = displayLinterButton();

    // THEN
    Assertions.assertThat(button.isDisable()).isFalse();
    Assertions.assertThat(callOnJavaFx(() -> getTexts(button.getItems())))
        .containsExactly("High priority", "Low priority");
    final Menu low = (Menu) button.getItems().get(1);
    Assertions.assertThat(callOnJavaFx(() -> getTexts(low.getItems())))
        .containsExactly("Balises obsolètes", "stripped-tag");
    final Menu obsolete = (Menu) low.getItems().getFirst();
    Assertions.assertThat(callOnJavaFx(() -> getTexts(obsolete.getItems())))
        .containsExactly("All namespaces", null, "Main", "Utilisateur");
  }

  @DisplayName("should disable the Linter button when the wiki has no Linter category")
  @Test
  void listNoCategory() throws InterruptedException, ExecutionException, TimeoutException {
    // GIVEN
    login(Map.of());

    // WHEN
    final MenuButton button = displayLinterButton();

    // THEN
    Assertions.assertThat(button.isDisable()).isTrue();
  }

  @DisplayName("should list the pages with a Linter error, sorted by namespace and title")
  @Test
  void openPages() throws InterruptedException, ExecutionException, TimeoutException {
    // GIVEN
    login(CATEGORIES);
    final MenuButton button = displayLinterButton();
    final Menu obsolete = (Menu) ((Menu) button.getItems().get(1)).getItems().getFirst();
    givenLintErrors(
        false, List.of("Utilisateur:Zoé", "Zurich", "Paris", "Utilisateur:Anne", "Paris"));
    givenLintErrors(true, List.of("Nice"));

    // WHEN
    runOnJavaFx(() -> obsolete.getItems().getFirst().fire());
    final List<String> allPages = waitForPages("Linter: obsolete-tag");
    runOnJavaFx(services.windowsRegistry()::closeAllWindows);
    runOnJavaFx(() -> getMainNamespaceItem(obsolete).fire());
    final List<String> mainPages = waitForPages("Linter: obsolete-tag");

    // THEN
    Assertions.assertThat(allPages)
        .containsExactly("Paris", "Zurich", "Utilisateur:Anne", "Utilisateur:Zoé");
    Assertions.assertThat(mainPages).containsExactly("Nice");
    mediaWikiServer.verify();
  }

  private static MenuItem getMainNamespaceItem(final Menu category) {
    return category.getItems().get(2);
  }

  private void givenLintErrors(final boolean mainNamespace, final List<String> titles) {
    mockApiLintErrors
        .givenRetrieveLintErrors(
            WIKI,
            LintErrorsQuery.emptyBuilder()
                .categories(Set.of("obsolete-tag"))
                .namespace(
                    mainNamespace
                        ? Set.copyOf(
                            services.namespaceRepository().getNamespaces(WIKI).stream()
                                .filter(namespace -> namespace.id() == 0)
                                .toList())
                        : null)
                .limit(Limit.max())
                .build())
        .willReturn(
            titles.stream()
                .map(
                    title ->
                        new PageLintError(
                            "obsolete-tag",
                            1L,
                            List.of(0, 1),
                            title.startsWith("Utilisateur:") ? 2 : 0,
                            1L,
                            Map.of(),
                            null,
                            title))
                .toList());
  }

  private List<String> waitForPages(final String title)
      throws InterruptedException, ExecutionException, TimeoutException {
    final JavaFxPageListWindow listWindow =
        waitForWindow(services.windowsRegistry(), JavaFxPageListWindow.class);
    Assertions.assertThat(callOnJavaFx(() -> listWindow.getStage().getTitle())).isEqualTo(title);
    return waitForJavaFx(
        () ->
            Optional.of(lookup(listWindow, JavaFxPageListWindow.PAGES_ID, ListView.class))
                .filter(list -> !list.getItems().isEmpty())
                .map(list -> List.copyOf((List<?>) list.getItems()))
                .map(items -> items.stream().map(String::valueOf).toList()));
  }

  private static List<@Nullable String> getTexts(final List<MenuItem> items) {
    return items.stream().map(MenuItem::getText).toList();
  }

  private void login(final Map<String, List<String>> categories) {
    guiLogin.login(
        WIKI,
        "Jane Doe",
        ApplicationLogin.LoginData.of(NAMESPACES).withLinterCategories(categories));
  }

  private MenuButton displayLinterButton()
      throws InterruptedException, ExecutionException, TimeoutException {
    mainWindowFactory.displayMainWindow();
    final JavaFxMainWindow mainWindow =
        waitForWindow(services.windowsRegistry(), JavaFxMainWindow.class);
    return waitForJavaFx(() -> find(mainWindow, LinterMenuButton.LINTER_ID, MenuButton.class));
  }
}
