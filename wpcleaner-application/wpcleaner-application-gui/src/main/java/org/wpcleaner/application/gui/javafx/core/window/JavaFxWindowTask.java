package org.wpcleaner.application.gui.javafx.core.window;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import java.util.function.Consumer;
import javafx.beans.property.BooleanProperty;
import javafx.concurrent.Task;
import org.wpcleaner.api.progress.LongRunningTask;
import org.wpcleaner.api.progress.ProgressTracker;
import org.wpcleaner.api.utils.GT;

class JavaFxWindowTask<T> extends Task<T> {

  private final BooleanProperty running;
  private final LongRunningTask<T> task;
  private final ProgressTracker progressTracker;
  private final Consumer<T> afterTask;
  private final Consumer<Exception> onError;

  JavaFxWindowTask(
      final BooleanProperty running,
      final JavaFxProgressOverlay overlay,
      final LongRunningTask<T> task,
      final Consumer<T> afterTask,
      final Consumer<Exception> onError) {
    this.running = running;
    this.task = task;
    this.afterTask = afterTask;
    this.onError = onError;
    this.progressTracker = new ProgressTracker(this::updateProgress);
    if (task.showProgress()) {
      overlay.setText(GT._T("Processing..."));
      messageProperty().addListener((_, _, newValue) -> overlay.setText(newValue));
    }
  }

  @Override
  protected T call() throws Exception {
    return task.call(progressTracker);
  }

  @Override
  protected void scheduled() {
    super.scheduled();
    if (task.showProgress()) {
      running.set(true);
    }
  }

  @Override
  protected void cancelled() {
    super.cancelled();
    if (task.showProgress()) {
      running.set(false);
    }
  }

  @Override
  protected void failed() {
    super.failed();
    if (task.showProgress()) {
      running.set(false);
    }
    final Throwable exception = getException();
    if (exception instanceof Exception e) {
      onError.accept(e);
    } else {
      onError.accept(new RuntimeException(exception));
    }
  }

  @Override
  protected void succeeded() {
    super.succeeded();
    if (task.showProgress()) {
      running.set(false);
    }
    afterTask.accept(getValue());
  }

  private void updateProgress(final List<String> descriptions) {
    updateMessage(descriptions.isEmpty() ? GT._T("Processing...") : descriptions.getLast());
  }
}
