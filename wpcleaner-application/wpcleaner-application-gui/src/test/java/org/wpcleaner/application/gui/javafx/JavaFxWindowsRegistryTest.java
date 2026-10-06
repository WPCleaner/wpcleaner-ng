/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wpcleaner.application.gui.javafx;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import org.assertj.core.api.Assertions;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.wpcleaner.application.gui.javafx.core.action.JavaFxActionServices;
import org.wpcleaner.application.gui.javafx.core.window.JavaFxWindow;
import org.wpcleaner.application.gui.javafx.core.window.JavaFxWindowServices;
import org.wpcleaner.lib.image.ImageLoader;

class JavaFxWindowsRegistryTest extends JavaFxTest {

  private static final class RegistryTestWindow extends JavaFxWindow<JavaFxWindowServices> {

    RegistryTestWindow(final JavaFxWindowServices services) {
      super(services);
      initialize();
    }

    @NonNull
    @Override
    public String getName() {
      return "registryTestWindow";
    }

    @NonNull
    @Override
    protected Scene createScene() {
      return new Scene(new StackPane(), 100, 100);
    }
  }

  @DisplayName(
      "JavaFxWindowsRegistry registers windows, filters visible ones, and closes all windows")
  @Test
  void testRegisterAndCloseAllWindows()
      throws InterruptedException, ExecutionException, TimeoutException {
    runOnJavaFx(
        () -> {
          final JavaFxWindowsRegistry registry = new JavaFxWindowsRegistry();

          final JavaFxWindowServices services = Mockito.mock(JavaFxWindowServices.class);
          final ImageLoader imageLoader = Mockito.mock(ImageLoader.class);
          final JavaFxActionServices actionServices = Mockito.mock(JavaFxActionServices.class);

          Mockito.when(services.imageLoader()).thenReturn(imageLoader);
          Mockito.when(services.actionServices()).thenReturn(actionServices);
          Mockito.when(services.windowsRegistry()).thenReturn(registry);

          final RegistryTestWindow window1 = new RegistryTestWindow(services);
          final RegistryTestWindow window2 = new RegistryTestWindow(services);

          window1.getStage().show();
          window2.getStage().show();

          Assertions.assertThat(registry.getVisibleWindows())
              .containsExactlyInAnyOrder(window1, window2);

          registry.closeAllWindows();

          Assertions.assertThat(window1.getStage().isShowing()).isFalse();
          Assertions.assertThat(window2.getStage().isShowing()).isFalse();
          Assertions.assertThat(registry.getVisibleWindows()).isEmpty();
        });
  }
}
