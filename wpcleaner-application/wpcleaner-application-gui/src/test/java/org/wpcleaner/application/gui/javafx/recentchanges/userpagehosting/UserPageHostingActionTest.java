package org.wpcleaner.application.gui.javafx.recentchanges.userpagehosting;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.wpcleaner.api.api.CurrentUserService;
import org.wpcleaner.api.api.query.prop.revisions.MockApiRevisions;
import org.wpcleaner.api.repository.namespace.CommonNamespaces;
import org.wpcleaner.api.settings.SettingsPersistence;
import org.wpcleaner.api.wiki.definition.WikiDefinition;
import org.wpcleaner.api.wiki.definition.WikipediaDefinitions;
import org.wpcleaner.application.gui.GuiLogin;
import org.wpcleaner.application.gui.GuiSpringBootTest;
import org.wpcleaner.application.gui.javafx.JavaFxTest;
import org.wpcleaner.application.gui.javafx.JavaFxWindowsRegistry;
import org.wpcleaner.application.gui.javafx.core.action.content.PageContentDisplay;
import org.wpcleaner.application.gui.javafx.core.action.content.PageContentExpectations;
import org.wpcleaner.application.gui.javafx.core.pageanalysis.PageAnalysisScrollPane;
import org.wpcleaner.application.gui.javafx.core.window.EmptyWindow;
import org.wpcleaner.application.gui.javafx.recentchanges.FilteredRecentChangeFixtures;
import org.wpcleaner.application.gui.javafx.recentchanges.JavaFxRecentChangesWindowServices;
import org.wpcleaner.application.gui.javafx.recentchanges.RecentChangesAction;

@GuiSpringBootTest
class UserPageHostingActionTest extends JavaFxTest {

  private static final WikiDefinition WIKI = WikipediaDefinitions.EN;
  private static final String CONFIG =
      """
      {
        "userPage": {
          "summary": "Summary",
          "text": "Text"
        },
        "userTalkPage": {
          "summary": "Talk summary",
          "texts": [
            {
              "addedByDefault": true,
              "label": "Notice",
              "text": "Notice text"
            }
          ]
        }
      }
      """;

  @Autowired private JavaFxRecentChangesWindowServices services;
  @Autowired private CurrentUserService currentUserService;
  @Autowired private GuiLogin guiLogin;
  @Autowired private JavaFxWindowsRegistry windowsRegistry;
  @Autowired private MockApiRevisions mockApiRevisions;

  @BeforeEach
  void setUp() throws IOException {
    guiLogin.login(WIKI, "Bot", GuiLogin.USER_NAMESPACES);
    Files.deleteIfExists(getConfigFile());
  }

  @AfterEach
  void tearDown() throws InterruptedException, ExecutionException, TimeoutException, IOException {
    runOnJavaFx(windowsRegistry::closeAllWindows);
    Files.deleteIfExists(getConfigFile());
    currentUserService.logout();
  }

  @DisplayName("canApply returns true only for user pages that are not subpages")
  @Test
  void canApply() throws InterruptedException, ExecutionException, TimeoutException {
    final RecentChangesAction action = createAction();

    Assertions.assertThat(
            action.canApply(
                FilteredRecentChangeFixtures.create(CommonNamespaces.USER.id, "User:NicoV")))
        .isTrue();
    Assertions.assertThat(
            action.canApply(
                FilteredRecentChangeFixtures.create(
                    CommonNamespaces.USER.id, "User:NicoV/sandbox")))
        .isFalse();
    Assertions.assertThat(
            action.canApply(
                FilteredRecentChangeFixtures.create(CommonNamespaces.MAIN.id, "Main Page")))
        .isFalse();
    Assertions.assertThat(action.canApply(FilteredRecentChangeFixtures.create(null, "User:NicoV")))
        .isFalse();
  }

  @DisplayName("configure displays configuration window")
  @Test
  void configureDisplaysConfigurationWindow()
      throws InterruptedException, ExecutionException, TimeoutException {
    final UserPageHostingAction action = createAction();

    runOnJavaFx(action::configure);

    Assertions.assertThat(waitForWindow(windowsRegistry, UserPageHostingConfigWindow.class))
        .isNotNull();
  }

  @DisplayName("apply retrieves user page and talk page in a single API call")
  @Test
  void applyRetrievesPagesInSingleApiCall()
      throws InterruptedException, ExecutionException, TimeoutException, IOException {
    writeConfig();
    PageContentExpectations.givenPagesContent(
        mockApiRevisions,
        WIKI,
        Map.of("User:NicoV", "User content", "User talk:NicoV", "Talk content"));
    final UserPageHostingAction action = createAction();

    runOnJavaFx(
        () ->
            action.apply(
                FilteredRecentChangeFixtures.create(CommonNamespaces.USER.id, "User:NicoV")));

    final UserPageHostingActionWindow window =
        waitForWindow(windowsRegistry, UserPageHostingActionWindow.class);
    runOnJavaFx(
        () -> {
          assertDisplay(
              window,
              UserPageHostingActionWindow.USER_PAGE_DISPLAY_ID,
              "User:NicoV",
              "User content");
          assertDisplay(
              window,
              UserPageHostingActionWindow.USER_TALK_PAGE_DISPLAY_ID,
              "User talk:NicoV",
              "Talk content");
        });
  }

  private UserPageHostingAction createAction()
      throws InterruptedException, ExecutionException, TimeoutException {
    return callOnJavaFx(() -> new UserPageHostingAction(services, new EmptyWindow<>(services)));
  }

  private static void writeConfig() throws IOException {
    final Path configFile = getConfigFile();
    Files.createDirectories(configFile.getParent());
    Files.writeString(configFile, CONFIG, StandardCharsets.UTF_8);
  }

  private static Path getConfigFile() {
    return SettingsPersistence.getFolder().resolve(WIKI.code()).resolve("rc-userpage-hosting.json");
  }

  private static void assertDisplay(
      final UserPageHostingActionWindow window,
      final String displayId,
      final String title,
      final String content) {
    final VBox display = lookup(window, displayId, VBox.class);
    Assertions.assertThat(lookup(display, PageContentDisplay.TITLE_ID, Label.class).getText())
        .isEqualTo(title);
    Assertions.assertThat(
            lookup(display, PageContentDisplay.CONTENT_ID, PageAnalysisScrollPane.class)
                .getArea()
                .getText())
        .isEqualTo(content);
  }
}
