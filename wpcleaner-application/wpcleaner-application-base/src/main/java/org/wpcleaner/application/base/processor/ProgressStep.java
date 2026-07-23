package org.wpcleaner.application.base.processor;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

public final class ProgressStep implements AutoCloseable {

  private final ProgressTracker tracker;
  private final String description;

  ProgressStep(final ProgressTracker tracker, final String description) {
    this.tracker = tracker;
    this.description = description;
  }

  public String getDescription() {
    return description;
  }

  @Override
  public void close() {
    tracker.end(this);
  }
}
