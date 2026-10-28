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
import javafx.scene.control.Button;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.wpcleaner.api.api.Limit;
import org.wpcleaner.api.api.query.list.random.ApiRandom;
import org.wpcleaner.api.api.query.list.random.MockApiRandom;
import org.wpcleaner.api.api.query.list.random.RandomPage;
import org.wpcleaner.api.api.query.list.random.RandomQuery;
import org.wpcleaner.api.repository.namespace.Namespace;
import org.wpcleaner.api.settings.SettingsPersistence;
import org.wpcleaner.api.wiki.definition.WikiDefinition;
import org.wpcleaner.api.wiki.definition.WikipediaDefinitions;
import org.wpcleaner.application.gui.GuiSpringBootTest;
import org.wpcleaner.application.gui.javafx.JavaFxImageLoader;
import org.wpcleaner.application.gui.javafx.JavaFxTest;
import org.wpcleaner.application.gui.javafx.core.window.EmptyWindow;
import org.wpcleaner.application.gui.settings.interesting.InterestingByWikiSettings;
import org.wpcleaner.application.gui.settings.interesting.InterestingSettings;
import org.wpcleaner.application.gui.settings.interesting.InterestingSettingsManager;

@GuiSpringBootTest
class PageInputTest extends JavaFxTest {

  private static final WikiDefinition WIKI = WikipediaDefinitions.EN;

  @Autowired private JavaFxMainWindowServices services;
  @Autowired private ApiRandom apiRandom;
  @Autowired private InterestingSettingsManager settingsManager;
  @Autowired private MockApiRandom mockApiRandom;
  @Autowired private SettingsPersistence settingsPersistence;

  private InterestingSettings initialSettings;

  @BeforeEach
  void setUp() {
    initialSettings = settingsManager.getCurrentSettings();
  }

  @AfterEach
  void tearDown() {
    settingsManager.updateSettings(initialSettings);
  }

  @DisplayName("PageInput initializes with correct pages and options")
  @Test
  void pageInputInitialization() throws InterruptedException, ExecutionException, TimeoutException {
    final PageInput pageInput = createPageInput(List.of("Page2", "Page1"));

    runOnJavaFx(
        () -> {
          Assertions.assertThat(pageInput.comboBox.getItems()).containsExactly("Page1", "Page2");
          Assertions.assertThat(pageInput.getPage()).isEmpty();

          pageInput.comboBox.setValue("Page1");
          Assertions.assertThat(pageInput.getPage()).isEqualTo("Page1");
        });
  }

  @DisplayName("PageInput add and remove page buttons update the stored pages")
  @Test
  void pageInputAddRemoveButtons()
      throws InterruptedException, ExecutionException, TimeoutException {
    final PageInput pageInput = createPageInput(List.of("pageB", "PageA", "Page2"));

    runOnJavaFx(
        () -> {
          final Button addPageButton =
              lookup(pageInput.toolBar, PageInput.ADD_PAGE_ID, Button.class);
          final Button removePageButton =
              lookup(pageInput.toolBar, PageInput.REMOVE_PAGE_ID, Button.class);
          Assertions.assertThat(pageInput.comboBox.getItems())
              .containsExactly("Page2", "PageA", "pageB");

          pageInput.comboBox.setValue(" ");
          addPageButton.fire();
          assertPages(pageInput, "Page2", "PageA", "pageB");
          assertStoredPages("pageB", "PageA", "Page2");

          pageInput.comboBox.setValue("PageA");
          addPageButton.fire();
          assertPages(pageInput, "Page2", "PageA", "pageB");
          assertStoredPages("pageB", "PageA", "Page2");

          pageInput.comboBox.setValue(" Page1 ");
          addPageButton.fire();
          assertPages(pageInput, "Page1", "Page2", "PageA", "pageB");
          assertStoredPages("Page1", "Page2", "PageA", "pageB");

          pageInput.comboBox.setValue("Page2");
          removePageButton.fire();
          assertPages(pageInput, "Page1", "PageA", "pageB");
          assertStoredPages("Page1", "PageA", "pageB");

          pageInput.comboBox.setValue("Page3");
          removePageButton.fire();
          assertPages(pageInput, "Page1", "PageA", "pageB");
          assertStoredPages("Page1", "PageA", "pageB");
        });
  }

  @DisplayName("PageInput random page button selects a random page of the main namespace")
  @Test
  void pageInputRandomPageButton()
      throws InterruptedException, ExecutionException, TimeoutException {
    mockApiRandom
        .givenRetrieveRandomPages(
            WIKI,
            RandomQuery.emptyBuilder().limit(Limit.of(1)).namespace(Set.of(Namespace.MAIN)).build())
        .willReturn(List.of(new RandomPage(1234, 0, "Random article")));
    final PageInput pageInput = createPageInput(List.of());

    runOnJavaFx(() -> lookup(pageInput.toolBar, PageInput.RANDOM_PAGE_ID, Button.class).fire());

    Assertions.assertThat(
            waitForJavaFx(
                () -> Optional.ofNullable(pageInput.comboBox.getValue()).filter(v -> !v.isEmpty())))
        .isEqualTo("Random article");
  }

  @DisplayName(
      "PageInput random page button shows an error when the random page cannot be retrieved")
  @Test
  void pageInputRandomPageButtonWhenFailing()
      throws InterruptedException, ExecutionException, TimeoutException {
    mockApiRandom
        .givenRetrieveRandomPages(
            WIKI,
            RandomQuery.emptyBuilder().limit(Limit.of(1)).namespace(Set.of(Namespace.MAIN)).build())
        .willFail();
    final PageInput pageInput = createPageInput(List.of());

    runOnJavaFx(() -> lookup(pageInput.toolBar, PageInput.RANDOM_PAGE_ID, Button.class).fire());

    Assertions.assertThat(closeAlert(pageInput.owner))
        .startsWith("Error retrieving a random page: ")
        .contains("500");
    Assertions.assertThat(callOnJavaFx(pageInput::getPage)).isEmpty();
  }

  private PageInput createPageInput(final List<String> pages)
      throws InterruptedException, ExecutionException, TimeoutException {
    settingsManager.updateSettings(
        initialSettings.withByWikiSettings(WIKI, new InterestingByWikiSettings(pages)));
    return callOnJavaFx(
        () ->
            new PageInput(
                new EmptyWindow<>(services),
                WIKI,
                settingsManager,
                new JavaFxImageLoader(services.imageLoader()),
                apiRandom));
  }

  private static void assertPages(final PageInput pageInput, final String... pages) {
    Assertions.assertThat(pageInput.comboBox.getItems()).containsExactly(pages);
  }

  private void assertStoredPages(final String... pages) {
    Assertions.assertThat(settingsManager.getCurrentSettings().getByWikiSettings(WIKI))
        .map(InterestingByWikiSettings::pages)
        .contains(List.of(pages));
    Assertions.assertThat(settingsPersistence.load(InterestingSettings.class))
        .flatMap(settings -> settings.getByWikiSettings(WIKI))
        .map(InterestingByWikiSettings::pages)
        .contains(List.of(pages));
  }
}
