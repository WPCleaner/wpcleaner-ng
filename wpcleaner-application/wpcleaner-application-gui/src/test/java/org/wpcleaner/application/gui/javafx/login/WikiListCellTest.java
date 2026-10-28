/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wpcleaner.application.gui.javafx.login;

import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.wpcleaner.api.wiki.definition.WikiDefinition;
import org.wpcleaner.application.gui.javafx.JavaFxImageLoader;
import org.wpcleaner.application.gui.javafx.JavaFxTest;
import org.wpcleaner.lib.image.ImageCollection;

class WikiListCellTest extends JavaFxTest {

  @DisplayName("WikiListCell formats name and code correctly")
  @Test
  void testWikiListCellFormatting()
      throws InterruptedException, ExecutionException, TimeoutException {
    final JavaFxImageLoader mockImageLoader = BDDMockito.mock(JavaFxImageLoader.class);
    BDDMockito.given(mockImageLoader.getImageView(BDDMockito.any(), BDDMockito.any()))
        .willReturn(Optional.empty());

    runOnJavaFx(
        () -> {
          final WikiListCell cell = new WikiListCell(mockImageLoader);
          final WikiDefinition wiki = BDDMockito.mock(WikiDefinition.class);
          BDDMockito.given(wiki.code()).willReturn("en");
          BDDMockito.given(wiki.name()).willReturn("English Wikipedia");
          BDDMockito.given(wiki.image()).willReturn(ImageCollection.LOGO_WIKIPEDIA);

          cell.updateItem(wiki, false);

          Assertions.assertThat(cell.getText()).isEqualTo("en - English Wikipedia");
        });
  }

  @DisplayName("WikiListCell handles null items correctly")
  @Test
  void testWikiListCellNullItem()
      throws InterruptedException, ExecutionException, TimeoutException {
    final JavaFxImageLoader mockImageLoader = BDDMockito.mock(JavaFxImageLoader.class);

    runOnJavaFx(
        () -> {
          final WikiListCell cell = new WikiListCell(mockImageLoader);
          cell.updateItem(null, false);

          Assertions.assertThat(cell.getText()).isNull();
          Assertions.assertThat(cell.getGraphic()).isNull();
        });
  }
}
