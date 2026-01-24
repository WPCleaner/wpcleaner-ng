package org.wpcleaner.application.base.processor;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

public class ProgressTracker {

  private final List<ProgressStep> currentSteps = new ArrayList<>();
  private final Consumer<List<String>> tracker;
  private final ReentrantLock lock = new ReentrantLock();

  public ProgressTracker(final Consumer<List<String>> tracker) {
    this.tracker = tracker;
  }

  public ProgressStep start(final String description) {
    final ProgressStep step = new ProgressStep(description);
    lock.lock();
    try {
      currentSteps.add(step);
      notifyTracker();
    } finally {
      lock.unlock();
    }
    return step;
  }

  void end(final ProgressStep step) {
    lock.lock();
    try {
      currentSteps.remove(step);
      notifyTracker();
    } finally {
      lock.unlock();
    }
  }

  private void notifyTracker() {
    tracker.accept(currentSteps.stream().map(ProgressStep::getDescription).toList());
  }

  public final class ProgressStep implements AutoCloseable {

    private final String description;

    private ProgressStep(final String description) {
      this.description = description;
    }

    public String getDescription() {
      return description;
    }

    @Override
    public void close() {
      end(this);
    }
  }
}
