package org.wpcleaner.application.gui.javafx.main;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import java.util.Optional;
import javafx.application.Platform;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.wpcleaner.api.wiki.definition.WikiDefinition;
import org.wpcleaner.application.gui.javafx.JavaFxImageLoader;
import org.wpcleaner.application.gui.javafx.JavaFxInitializer;
import org.wpcleaner.application.gui.settings.interesting.InterestingByWikiSettings;
import org.wpcleaner.application.gui.settings.interesting.InterestingSettings;
import org.wpcleaner.application.gui.settings.interesting.InterestingSettingsManager;

class PageInputTest {

  @BeforeAll
  static void setUpClass() {
    JavaFxInitializer.initialize();
  }

  @DisplayName("PageInput initializes with correct pages and options")
  @Test
  void testPageInputInitialization() {
    final WikiDefinition wiki = Mockito.mock(WikiDefinition.class);
    final InterestingSettingsManager settingsManager =
        Mockito.mock(InterestingSettingsManager.class);
    final InterestingSettings settings = Mockito.mock(InterestingSettings.class);
    final InterestingByWikiSettings wikiSettings = Mockito.mock(InterestingByWikiSettings.class);
    final JavaFxImageLoader imageLoader = Mockito.mock(JavaFxImageLoader.class);

    Mockito.when(settingsManager.getCurrentSettings()).thenReturn(settings);
    Mockito.when(settings.getByWikiSettings(wiki)).thenReturn(Optional.of(wikiSettings));
    Mockito.when(wikiSettings.pages()).thenReturn(List.of("Page1", "Page2"));
    Mockito.when(imageLoader.getImageView(Mockito.any(), Mockito.any()))
        .thenReturn(Optional.empty());

    Platform.runLater(
        () -> {
          final PageInput pageInput = new PageInput(wiki, settingsManager, imageLoader);

          Assertions.assertThat(pageInput.comboBox.getItems()).containsExactly("Page1", "Page2");
          Assertions.assertThat(pageInput.getPage()).isEmpty();

          pageInput.comboBox.setValue("Page1");
          Assertions.assertThat(pageInput.getPage()).isEqualTo("Page1");
        });
  }
}
