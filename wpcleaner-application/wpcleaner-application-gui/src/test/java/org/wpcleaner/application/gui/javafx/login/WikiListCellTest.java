/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wpcleaner.application.gui.javafx.login;

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
import org.wpcleaner.lib.image.ImageCollection;

class WikiListCellTest {

  @BeforeAll
  static void setUpClass() {
    JavaFxInitializer.initialize();
  }

  @DisplayName("WikiListCell formats name and code correctly")
  @Test
  void testWikiListCellFormatting() {
    final JavaFxImageLoader mockImageLoader = Mockito.mock(JavaFxImageLoader.class);
    Mockito.when(mockImageLoader.getImageView(Mockito.any(), Mockito.any()))
        .thenReturn(Optional.empty());

    Platform.runLater(
        () -> {
          final WikiListCell cell = new WikiListCell(mockImageLoader);
          final WikiDefinition wiki = Mockito.mock(WikiDefinition.class);
          Mockito.when(wiki.code()).thenReturn("en");
          Mockito.when(wiki.name()).thenReturn("English Wikipedia");
          Mockito.when(wiki.image()).thenReturn(ImageCollection.LOGO_WIKIPEDIA);

          cell.updateItem(wiki, false);

          Assertions.assertThat(cell.getText()).isEqualTo("en - English Wikipedia");
        });
  }

  @DisplayName("WikiListCell handles null items correctly")
  @Test
  void testWikiListCellNullItem() {
    final JavaFxImageLoader mockImageLoader = Mockito.mock(JavaFxImageLoader.class);

    Platform.runLater(
        () -> {
          final WikiListCell cell = new WikiListCell(mockImageLoader);
          cell.updateItem(null, false);

          Assertions.assertThat(cell.getText()).isNull();
          Assertions.assertThat(cell.getGraphic()).isNull();
        });
  }
}
