package org.wpcleaner.application.bot.utils;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public final class Futures {

  private Futures() {}

  public static void waitFor(final Future<?> future) throws IOException, InterruptedException {
    try {
      future.get();
    } catch (final ExecutionException e) {
      if (e.getCause() instanceof final IOException cause) {
        throw new IOException(cause.getMessage(), e);
      }
      throw new IllegalStateException(e);
    }
  }
}
