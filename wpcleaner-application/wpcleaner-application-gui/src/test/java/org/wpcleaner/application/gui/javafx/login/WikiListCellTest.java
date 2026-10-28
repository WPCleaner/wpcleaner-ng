/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wpcleaner.application.gui.javafx.login;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import javafx.scene.image.ImageView;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.wpcleaner.api.wiki.definition.WikipediaDefinitions;
import org.wpcleaner.application.gui.GuiSpringBootTest;
import org.wpcleaner.application.gui.javafx.JavaFxImageLoader;
import org.wpcleaner.application.gui.javafx.JavaFxTest;

@GuiSpringBootTest
class WikiListCellTest extends JavaFxTest {

  @Autowired private JavaFxLoginWindowServices services;

  @DisplayName("WikiListCell formats name and code correctly and displays the wiki logo")
  @Test
  void wikiListCellFormatting() throws InterruptedException, ExecutionException, TimeoutException {
    runOnJavaFx(
        () -> {
          final WikiListCell cell = new WikiListCell(new JavaFxImageLoader(services.imageLoader()));

          cell.updateItem(WikipediaDefinitions.EN, false);

          Assertions.assertThat(cell.getText()).isEqualTo("en - English Wikipedia");
          Assertions.assertThat(cell.getGraphic()).isInstanceOf(ImageView.class);
        });
  }

  @DisplayName("WikiListCell handles null items correctly")
  @Test
  void wikiListCellNullItem() throws InterruptedException, ExecutionException, TimeoutException {
    runOnJavaFx(
        () -> {
          final WikiListCell cell = new WikiListCell(new JavaFxImageLoader(services.imageLoader()));
          cell.updateItem(null, false);

          Assertions.assertThat(cell.getText()).isNull();
          Assertions.assertThat(cell.getGraphic()).isNull();
        });
  }
}
