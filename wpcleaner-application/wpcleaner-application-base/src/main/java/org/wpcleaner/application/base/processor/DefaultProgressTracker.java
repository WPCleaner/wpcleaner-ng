package org.wpcleaner.application.base.processor;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

public class DefaultProgressTracker implements ProgressTracker {

  private final List<ProgressStep> currentSteps = new ArrayList<>();
  private final Consumer<List<String>> tracked;
  private final ReentrantLock lock = new ReentrantLock();

  public DefaultProgressTracker(final Consumer<List<String>> tracked) {
    this.tracked = tracked;
  }

  @Override
  public ProgressStep start(final String description) {
    final ProgressStep step = new ProgressStep(this, description);
    lock.lock();
    try {
      currentSteps.add(step);
      notifyTracker();
    } finally {
      lock.unlock();
    }
    return step;
  }

  @Override
  public void end(final ProgressStep step) {
    lock.lock();
    try {
      currentSteps.remove(step);
      notifyTracker();
    } finally {
      lock.unlock();
    }
  }

  private void notifyTracker() {
    tracked.accept(currentSteps.stream().map(ProgressStep::getDescription).toList());
  }
}
