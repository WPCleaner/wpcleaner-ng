package org.wpcleaner.application.gui.javafx.core.window;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import org.assertj.core.api.Assertions;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.wpcleaner.api.progress.LongRunningTask;
import org.wpcleaner.api.progress.ProgressStep;
import org.wpcleaner.api.progress.ProgressTracker;
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

    BooleanProperty loadingProperty() {
      return loading;
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

  @DisplayName("JavaFxWindow executeAsync runs task and calls afterTask on JavaFX thread")
  @Test
  void testExecuteAsyncSuccess() throws InterruptedException, ExecutionException, TimeoutException {
    final CompletableFuture<String> resultFuture = new CompletableFuture<>();
    final CompletableFuture<Boolean> threadFuture = new CompletableFuture<>();
    final CompletableFuture<Boolean> daemonFuture = new CompletableFuture<>();
    final CompletableFuture<String> threadNameFuture = new CompletableFuture<>();
    final CompletableFuture<Boolean> loadingBeforeFuture = new CompletableFuture<>();

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
          final LongRunningTask<String> task =
              tracker -> {
                threadNameFuture.complete(Thread.currentThread().getName());
                daemonFuture.complete(Thread.currentThread().isDaemon());
                try (ProgressStep _ = tracker.start("Running step")) {
                  return "success";
                }
              };

          window.executeAsync(
              task,
              result -> {
                threadFuture.complete(Platform.isFxApplicationThread());
                resultFuture.complete(result);
              },
              resultFuture::completeExceptionally);

          loadingBeforeFuture.complete(window.loadingProperty().get());
        });

    Assertions.assertThat(loadingBeforeFuture.get(5, TimeUnit.SECONDS)).isTrue();
    Assertions.assertThat(daemonFuture.get(5, TimeUnit.SECONDS)).isTrue();
    Assertions.assertThat(threadNameFuture.get(5, TimeUnit.SECONDS)).startsWith("JavaFxWindow");
    Assertions.assertThat(threadFuture.get(5, TimeUnit.SECONDS)).isTrue();
    Assertions.assertThat(resultFuture.get(5, TimeUnit.SECONDS)).isEqualTo("success");
  }

  @DisplayName("JavaFxWindow executeAsync handles failure and calls onError on JavaFX thread")
  @Test
  void testExecuteAsyncFailure() throws InterruptedException, ExecutionException, TimeoutException {
    final CompletableFuture<Throwable> errorFuture = new CompletableFuture<>();
    final CompletableFuture<Boolean> threadFuture = new CompletableFuture<>();
    final CompletableFuture<Boolean> loadingBeforeFuture = new CompletableFuture<>();

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
          final LongRunningTask<String> task =
              _ -> {
                throw new IllegalStateException("Task failed");
              };

          window.executeAsync(
              task,
              _ -> {},
              error -> {
                threadFuture.complete(Platform.isFxApplicationThread());
                errorFuture.complete(error);
              });

          loadingBeforeFuture.complete(window.loadingProperty().get());
        });

    Assertions.assertThat(loadingBeforeFuture.get(5, TimeUnit.SECONDS)).isTrue();
    Assertions.assertThat(threadFuture.get(5, TimeUnit.SECONDS)).isTrue();
    Assertions.assertThat(errorFuture.get(5, TimeUnit.SECONDS))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("Task failed");
  }

  @DisplayName("JavaFxWindow executeAsync does not set loading when showProgress is false")
  @Test
  void testExecuteAsyncWithoutProgress()
      throws InterruptedException, ExecutionException, TimeoutException {
    final CompletableFuture<String> resultFuture = new CompletableFuture<>();
    final CompletableFuture<Boolean> threadFuture = new CompletableFuture<>();
    final CompletableFuture<Boolean> loadingBeforeFuture = new CompletableFuture<>();
    final CompletableFuture<Boolean> loadingAfterFuture = new CompletableFuture<>();

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
          final LongRunningTask<String> task =
              new LongRunningTask<>() {
                @NonNull
                @Override
                public String call(@NonNull final ProgressTracker tracker) {
                  return "background-done";
                }

                @Override
                public boolean showProgress() {
                  return false;
                }
              };

          window.executeAsync(
              task,
              result -> {
                threadFuture.complete(Platform.isFxApplicationThread());
                loadingAfterFuture.complete(window.loadingProperty().get());
                resultFuture.complete(result);
              },
              resultFuture::completeExceptionally);

          loadingBeforeFuture.complete(window.loadingProperty().get());
        });

    Assertions.assertThat(loadingBeforeFuture.get(5, TimeUnit.SECONDS)).isFalse();
    Assertions.assertThat(loadingAfterFuture.get(5, TimeUnit.SECONDS)).isFalse();
    Assertions.assertThat(threadFuture.get(5, TimeUnit.SECONDS)).isTrue();
    Assertions.assertThat(resultFuture.get(5, TimeUnit.SECONDS)).isEqualTo("background-done");
  }
}
