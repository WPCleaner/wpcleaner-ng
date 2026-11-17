package org.wpcleaner.application.gui.javafx.main;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DialogPane;
import javafx.scene.control.ListView;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentMatchers;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.wpcleaner.api.api.CurrentUserService;
import org.wpcleaner.api.wiki.definition.WikipediaDefinitions;
import org.wpcleaner.application.base.ApplicationLogin;
import org.wpcleaner.application.gui.GuiSpringBootTest;
import org.wpcleaner.application.gui.javafx.JavaFxTest;
import org.wpcleaner.application.gui.javafx.JavaFxWindowsRegistry;
import org.wpcleaner.application.gui.javafx.core.window.JavaFxFileChooser;
import org.wpcleaner.application.gui.javafx.pagelist.JavaFxPageListWindow;

@GuiSpringBootTest
class LoadListButtonTest extends JavaFxTest {

  private static final String FILE_NAME = "frwiki-error-048.txt";

  @Autowired private JavaFxMainWindowFactory mainWindowFactory;
  @Autowired private CurrentUserService currentUserService;
  @Autowired private ApplicationLogin guiLogin;
  @Autowired private JavaFxWindowsRegistry windowsRegistry;
  @Autowired private JavaFxFileChooser fileChooser;

  @TempDir private Path folder;

  @AfterEach
  void tearDown() throws InterruptedException, ExecutionException, TimeoutException {
    runOnJavaFx(windowsRegistry::closeAllWindows);
    currentUserService.logout();
  }

  @DisplayName("should list the pages of an unformatted file")
  @Test
  void loadPageNames()
      throws InterruptedException, ExecutionException, TimeoutException, IOException {
    // GIVEN
    givenFile("Paris\n\n  Lyon \nParis\nModèle:Infobox\n");

    // WHEN
    final List<String> pages = loadList(PageListFileFormat.PAGE_NAMES);

    // THEN
    Assertions.assertThat(pages).containsExactly("Paris", "Lyon", "Modèle:Infobox");
  }

  @DisplayName("should list the internal links of a file written by the bot")
  @Test
  void loadInternalLinks()
      throws InterruptedException, ExecutionException, TimeoutException, IOException {
    // GIVEN
    givenFile(
        """
        <!-- Generated using frwiki-20261101-pages-articles -->
        * [[Paris]]: <nowiki>[[Paris]]</nowiki>
        * [[Lyon]]: <nowiki>[[Lyon|ville]]</nowiki>, <nowiki>[[Paris]]</nowiki>
        * [[:Modèle:Infobox]]: <nowiki>[[Modèle:Infobox]]</nowiki>
        * [[Paris]]: duplicate
        """);

    // WHEN
    final List<String> pages = loadList(PageListFileFormat.INTERNAL_LINKS);

    // THEN
    Assertions.assertThat(pages).containsExactly("Paris", "Lyon", "Modèle:Infobox");
  }

  private void givenFile(final String content) throws IOException {
    final Path file = folder.resolve(FILE_NAME);
    Files.writeString(file, content, StandardCharsets.UTF_8);
    BDDMockito.given(fileChooser.chooseTextFile(ArgumentMatchers.any()))
        .willReturn(Optional.of(file));
  }

  private List<String> loadList(final PageListFileFormat format)
      throws InterruptedException, ExecutionException, TimeoutException {
    guiLogin.login(
        WikipediaDefinitions.FR,
        "Jane Doe",
        ApplicationLogin.LoginData.of(ApplicationLogin.USER_NAMESPACES));
    mainWindowFactory.displayMainWindow();
    final JavaFxMainWindow mainWindow = waitForWindow(windowsRegistry, JavaFxMainWindow.class);
    final Button button =
        waitForJavaFx(() -> find(mainWindow, LoadListButton.LOAD_LIST_ID, Button.class));
    Platform.runLater(button::fire);
    answerDialog(mainWindow, dialogPane -> selectFormat(dialogPane, format));
    final JavaFxPageListWindow listWindow =
        waitForWindow(windowsRegistry, JavaFxPageListWindow.class);
    Assertions.assertThat(callOnJavaFx(() -> listWindow.getStage().getTitle()))
        .isEqualTo("List: " + FILE_NAME);
    @SuppressWarnings("unchecked")
    final ListView<String> pages =
        waitForJavaFx(
            () ->
                Optional.of(lookup(listWindow, JavaFxPageListWindow.PAGES_ID, ListView.class))
                    .filter(list -> !list.getItems().isEmpty()));
    return callOnJavaFx(() -> List.copyOf(pages.getItems()));
  }

  @SuppressWarnings("unchecked")
  private static void selectFormat(final DialogPane dialogPane, final PageListFileFormat format) {
    ((ComboBox<PageListFileFormat>) dialogPane.lookup(".combo-box"))
        .getSelectionModel()
        .select(format);
  }
}
