package org.wpcleaner.application.bot.request;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.time.Duration;
import java.util.concurrent.TimeUnit;

public final class RequestThrottle {

  private final long intervalNanos;
  private long nextRequestNanos;

  public RequestThrottle(final Duration interval) {
    this.intervalNanos = interval.toNanos();
    this.nextRequestNanos = System.nanoTime();
  }

  public void await() throws InterruptedException {
    final long waitNanos = nextRequestNanos - System.nanoTime();
    if (waitNanos > 0) {
      TimeUnit.NANOSECONDS.sleep(waitNanos);
    }
    nextRequestNanos = System.nanoTime() + intervalNanos;
  }
}
