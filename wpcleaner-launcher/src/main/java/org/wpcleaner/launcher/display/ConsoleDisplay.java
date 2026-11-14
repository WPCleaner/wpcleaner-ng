package org.wpcleaner.launcher.display;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.io.PrintStream;

public final class ConsoleDisplay implements LauncherDisplay {

  private static final int PERCENT_STEP = 10;
  private static final int PERCENT = 100;

  private final PrintStream output;
  private final PrintStream errorOutput;
  private int displayedPercent = -1;

  public ConsoleDisplay(final PrintStream output, final PrintStream errorOutput) {
    this.output = output;
    this.errorOutput = errorOutput;
  }

  @Override
  public void progress(final String status, final long current, final long total) {
    final int percent = total > 0 ? (int) (current * PERCENT / total) : 0;
    if (displayedPercent < 0 || percent >= displayedPercent + PERCENT_STEP) {
      displayedPercent = percent - percent % PERCENT_STEP;
      output.println("%s: %d%%".formatted(status, displayedPercent));
    }
  }

  @Override
  public void closeProgress() {
    displayedPercent = -1;
  }

  @Override
  public void inform(final String message) {
    output.println(message);
  }

  @Override
  public void error(final String message) {
    errorOutput.println(message);
  }

  @Override
  public boolean confirm(final String message) {
    output.println(message);
    return false;
  }
}
