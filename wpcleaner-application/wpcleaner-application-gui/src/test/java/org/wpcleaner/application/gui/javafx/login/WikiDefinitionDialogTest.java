package org.wpcleaner.application.gui.javafx.login;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.Optional;
import javafx.application.Platform;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.wpcleaner.api.api.query.meta.siteinfo.ApiSiteInfo;
import org.wpcleaner.application.gui.javafx.JavaFxImageLoader;
import org.wpcleaner.application.gui.javafx.JavaFxInitializer;

class WikiDefinitionDialogTest {

  @BeforeAll
  static void setUpClass() {
    JavaFxInitializer.initialize();
  }

  @DisplayName("WikiDefinitionDialog initializes correctly")
  @Test
  void testDialogInitialization() {
    final JavaFxImageLoader mockImageLoader = Mockito.mock(JavaFxImageLoader.class);
    Mockito.when(mockImageLoader.getImageView(Mockito.any(), Mockito.any()))
        .thenReturn(Optional.empty());

    final ApiSiteInfo mockApiSiteInfo = Mockito.mock(ApiSiteInfo.class);

    Platform.runLater(
        () -> {
          final WikiDefinitionDialog dialog =
              new WikiDefinitionDialog(null, mockImageLoader, mockApiSiteInfo);
          Assertions.assertThat(dialog.getTitle()).isEqualTo("Add wiki");
          Assertions.assertThat(dialog.getDialogPane()).isNotNull();
        });
  }
}
