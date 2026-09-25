package org.wpcleaner.application.gui.javafx;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javafx.application.Platform;
import org.junit.jupiter.api.BeforeAll;

public class JavaFxTest {

  @BeforeAll
  static void setUpClass() {
    JavaFxInitializer.initialize();
  }

  @SuppressWarnings("PMD.AvoidCatchingThrowable")
  protected void runOnJavaFx(final Runnable runnable)
      throws InterruptedException, ExecutionException, TimeoutException {
    final CompletableFuture<Void> future = new CompletableFuture<>();
    Platform.runLater(
        () -> {
          try {
            runnable.run();
            future.complete(null);
          } catch (final Throwable e) {
            future.completeExceptionally(e);
          }
        });
    future.get(15, TimeUnit.SECONDS);
  }
}
