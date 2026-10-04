package org.wpcleaner.application.gui.javafx.core.window;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import org.assertj.core.api.Assertions;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.wpcleaner.application.gui.javafx.JavaFxTest;
import org.wpcleaner.application.gui.javafx.JavaFxWindowsRegistry;
import org.wpcleaner.application.gui.javafx.core.action.JavaFxActionServices;
import org.wpcleaner.lib.image.ImageLoader;

class JavaFxWindowTest extends JavaFxTest {

  private static final class TestWindow extends JavaFxWindow<JavaFxWindowServices> {

    TestWindow(final JavaFxWindowServices services) {
      super(services);
      initialize();
    }

    @NonNull
    @Override
    public String getName() {
      return "testWindow";
    }

    @NonNull
    @Override
    protected Scene createScene() {
      return new Scene(new StackPane(), 100, 100);
    }
  }

  @DisplayName("JavaFxWindow default help page is null and F1 key triggers displayHelp")
  @Test
  void testDefaultHelpPageAndF1Key()
      throws InterruptedException, ExecutionException, TimeoutException {
    runOnJavaFx(
        () -> {
          final JavaFxWindowServices services = Mockito.mock(JavaFxWindowServices.class);
          final ImageLoader imageLoader = Mockito.mock(ImageLoader.class);
          final JavaFxActionServices actionServices = Mockito.mock(JavaFxActionServices.class);
          final JavaFxWindowsRegistry windowsRegistry = Mockito.mock(JavaFxWindowsRegistry.class);

          Mockito.when(services.imageLoader()).thenReturn(imageLoader);
          Mockito.when(services.actionServices()).thenReturn(actionServices);
          Mockito.when(services.windowsRegistry()).thenReturn(windowsRegistry);

          final TestWindow window = new TestWindow(services);

          Assertions.assertThat(window.getHelpPage()).isNull();

          final KeyEvent f1 =
              new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.F1, false, false, false, false);
          window.getStage().getScene().getRoot().fireEvent(f1);
          Mockito.verify(actionServices).displayHelp(window);

          window.displayHelp();
          Mockito.verify(actionServices, Mockito.times(2)).displayHelp(window);

          window.getStage().close();
        });
  }
}
