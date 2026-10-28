package org.wpcleaner.application.gui.javafx.main;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import java.util.Optional;
import javafx.application.Platform;
import javafx.scene.control.Button;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.wpcleaner.api.api.query.list.random.ApiRandom;
import org.wpcleaner.api.wiki.definition.WikiDefinition;
import org.wpcleaner.application.gui.javafx.JavaFxImageLoader;
import org.wpcleaner.application.gui.javafx.JavaFxTest;
import org.wpcleaner.application.gui.javafx.core.window.JavaFxWindow;
import org.wpcleaner.application.gui.settings.interesting.InterestingByWikiSettings;
import org.wpcleaner.application.gui.settings.interesting.InterestingSettings;
import org.wpcleaner.application.gui.settings.interesting.InterestingSettingsManager;

class PageInputTest extends JavaFxTest {

  @DisplayName("PageInput initializes with correct pages and options")
  @Test
  void testPageInputInitialization() {
    final JavaFxWindow<?> owner = BDDMockito.mock(JavaFxWindow.class);
    final WikiDefinition wiki = BDDMockito.mock(WikiDefinition.class);
    final InterestingSettingsManager settingsManager =
        BDDMockito.mock(InterestingSettingsManager.class);
    final InterestingSettings settings = BDDMockito.mock(InterestingSettings.class);
    final InterestingByWikiSettings wikiSettings = BDDMockito.mock(InterestingByWikiSettings.class);
    final JavaFxImageLoader imageLoader = BDDMockito.mock(JavaFxImageLoader.class);
    final ApiRandom apiRandom = BDDMockito.mock(ApiRandom.class);

    BDDMockito.given(settingsManager.getCurrentSettings()).willReturn(settings);
    BDDMockito.given(settings.getByWikiSettings(wiki)).willReturn(Optional.of(wikiSettings));
    BDDMockito.given(wikiSettings.pages()).willReturn(List.of("Page2", "Page1"));
    BDDMockito.given(imageLoader.getImageView(BDDMockito.any(), BDDMockito.any()))
        .willReturn(Optional.empty());

    Platform.runLater(
        () -> {
          final PageInput pageInput =
              new PageInput(owner, wiki, settingsManager, imageLoader, apiRandom);

          Assertions.assertThat(pageInput.comboBox.getItems()).containsExactly("Page1", "Page2");
          Assertions.assertThat(pageInput.getPage()).isEmpty();

          pageInput.comboBox.setValue("Page1");
          Assertions.assertThat(pageInput.getPage()).isEqualTo("Page1");
        });
  }

  @DisplayName("PageInput add and remove page buttons")
  @Test
  void testPageInputAddRemoveButtons() {
    final JavaFxWindow<?> owner = BDDMockito.mock(JavaFxWindow.class);
    final WikiDefinition wiki = BDDMockito.mock(WikiDefinition.class);
    final InterestingSettingsManager settingsManager =
        BDDMockito.mock(InterestingSettingsManager.class);
    final InterestingSettings settings = BDDMockito.mock(InterestingSettings.class);
    final InterestingSettings newSettings = BDDMockito.mock(InterestingSettings.class);
    final InterestingByWikiSettings wikiSettings = BDDMockito.mock(InterestingByWikiSettings.class);
    final JavaFxImageLoader imageLoader = BDDMockito.mock(JavaFxImageLoader.class);
    final ApiRandom apiRandom = BDDMockito.mock(ApiRandom.class);

    BDDMockito.given(settingsManager.getCurrentSettings()).willReturn(settings);
    BDDMockito.given(settings.getByWikiSettings(wiki)).willReturn(Optional.of(wikiSettings));
    BDDMockito.given(wikiSettings.pages()).willReturn(List.of("pageB", "PageA", "Page2"));
    BDDMockito.given(settings.withByWikiSettings(BDDMockito.eq(wiki), BDDMockito.any()))
        .willReturn(newSettings);
    BDDMockito.given(imageLoader.getImageView(BDDMockito.any(), BDDMockito.any()))
        .willReturn(Optional.empty());

    Platform.runLater(
        () -> {
          final PageInput pageInput =
              new PageInput(owner, wiki, settingsManager, imageLoader, apiRandom);

          final Button addPageButton =
              lookup(pageInput.toolBar, PageInput.ADD_PAGE_ID, Button.class);
          final Button removePageButton =
              lookup(pageInput.toolBar, PageInput.REMOVE_PAGE_ID, Button.class);

          // Initial state
          Assertions.assertThat(pageInput.comboBox.getItems())
              .containsExactly("Page2", "PageA", "pageB");

          // Test add empty value
          pageInput.comboBox.setValue(" ");
          addPageButton.fire();
          Assertions.assertThat(pageInput.comboBox.getItems())
              .containsExactly("Page2", "PageA", "pageB");
          BDDMockito.then(settingsManager)
              .should(BDDMockito.never())
              .updateSettings(BDDMockito.any());

          // Test add existing value
          pageInput.comboBox.setValue("PageA");
          addPageButton.fire();
          Assertions.assertThat(pageInput.comboBox.getItems())
              .containsExactly("Page2", "PageA", "pageB");
          BDDMockito.then(settingsManager)
              .should(BDDMockito.never())
              .updateSettings(BDDMockito.any());

          // Test add new value and verify sorting
          pageInput.comboBox.setValue("Page1");
          addPageButton.fire();
          Assertions.assertThat(pageInput.comboBox.getItems())
              .containsExactly("Page1", "Page2", "PageA", "pageB");
          BDDMockito.then(settingsManager).should(BDDMockito.times(1)).updateSettings(newSettings);

          // Prepare mock for remove
          BDDMockito.given(settingsManager.getCurrentSettings()).willReturn(newSettings);
          BDDMockito.given(newSettings.withByWikiSettings(BDDMockito.eq(wiki), BDDMockito.any()))
              .willReturn(settings);

          // Test remove existing value
          pageInput.comboBox.setValue("Page2");
          removePageButton.fire();
          Assertions.assertThat(pageInput.comboBox.getItems())
              .containsExactly("Page1", "PageA", "pageB");
          BDDMockito.then(settingsManager).should(BDDMockito.times(1)).updateSettings(settings);

          // Test remove non-existing value
          pageInput.comboBox.setValue("Page3");
          removePageButton.fire();
          Assertions.assertThat(pageInput.comboBox.getItems())
              .containsExactly("Page1", "PageA", "pageB");
          BDDMockito.then(settingsManager).should(BDDMockito.times(1)).updateSettings(settings);
        });
  }
}
