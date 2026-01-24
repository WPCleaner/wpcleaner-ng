package org.wpcleaner.application.gui.swing.core.worker;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import jakarta.annotation.Nullable;
import java.awt.Component;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import javax.swing.JRootPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import org.springframework.stereotype.Service;

@Service
public class SwingWorkerProcessor {

  public <R> void process(
      final Component component,
      final Callable<R> process,
      final Consumer<R> onSuccess,
      final Consumer<Throwable> onFailure) {
    new InternalSwingWorker<>(SwingUtilities.getRootPane(component), process, onSuccess, onFailure)
        .execute();
  }

  private static final class InternalSwingWorker<R> extends SwingWorker<R, String> {

    @Nullable private final JRootPane rootPane;
    @Nullable private final Component previousGlassPane;
    private final ProgressPanel progressPanel;
    private final Callable<R> process;
    private final Consumer<R> onSuccess;
    private final Consumer<Throwable> onFailure;

    private InternalSwingWorker(
        @Nullable final JRootPane rootPane,
        final Callable<R> process,
        final Consumer<R> onSuccess,
        final Consumer<Throwable> onFailure) {
      this.rootPane = rootPane;
      Component possiblePrevious = null;
      if (rootPane != null) {
        possiblePrevious = rootPane.getGlassPane();
      }
      previousGlassPane = possiblePrevious;
      this.progressPanel = new ProgressPanel();
      if (rootPane != null) {
        rootPane.setGlassPane(progressPanel);
        progressPanel.addMouseListener(new NoOpMouseListener());
        progressPanel.addKeyListener(new NoOpKeyListener());
        progressPanel.setVisible(true);
        progressPanel.repaint();
      }
      this.process = process;
      this.onSuccess = onSuccess;
      this.onFailure = onFailure;
    }

    @Override
    protected R doInBackground() throws Exception {
      return process.call();
    }

    @Override
    @SuppressWarnings("PMD.AvoidCatchingThrowable")
    protected void done() {
      if (isCancelled() || !isDone()) {
        return;
      }
      if (rootPane != null) {
        progressPanel.setVisible(false);
        rootPane.setGlassPane(previousGlassPane);
      }
      final R result;
      try {
        result = get();
      } catch (Throwable e) {
        onFailure.accept(e);
        return;
      }
      onSuccess.accept(result);
      super.done();
    }
  }
}
