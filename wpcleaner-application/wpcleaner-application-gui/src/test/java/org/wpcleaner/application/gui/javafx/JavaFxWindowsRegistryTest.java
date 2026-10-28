/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wpcleaner.application.gui.javafx;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.wpcleaner.application.gui.GuiSpringBootTest;
import org.wpcleaner.application.gui.javafx.core.window.EmptyWindow;
import org.wpcleaner.application.gui.javafx.recentchanges.JavaFxRecentChangesWindowServices;

@GuiSpringBootTest
class JavaFxWindowsRegistryTest extends JavaFxTest {

  @Autowired private JavaFxRecentChangesWindowServices services;
  @Autowired private JavaFxWindowsRegistry registry;

  @DisplayName(
      "JavaFxWindowsRegistry registers windows, filters visible ones, and closes all windows")
  @Test
  void registerAndCloseAllWindows()
      throws InterruptedException, ExecutionException, TimeoutException {
    runOnJavaFx(
        () -> {
          final EmptyWindow<JavaFxRecentChangesWindowServices> window1 =
              new EmptyWindow<>(services);
          final EmptyWindow<JavaFxRecentChangesWindowServices> window2 =
              new EmptyWindow<>(services);
          final EmptyWindow<JavaFxRecentChangesWindowServices> hiddenWindow =
              new EmptyWindow<>(services);

          window1.getStage().show();
          window2.getStage().show();

          Assertions.assertThat(registry.getVisibleWindows())
              .contains(window1, window2)
              .doesNotContain(hiddenWindow);

          registry.closeAllWindows();

          Assertions.assertThat(window1.getStage().isShowing()).isFalse();
          Assertions.assertThat(window2.getStage().isShowing()).isFalse();
          Assertions.assertThat(registry.getVisibleWindows()).isEmpty();
        });
  }
}
