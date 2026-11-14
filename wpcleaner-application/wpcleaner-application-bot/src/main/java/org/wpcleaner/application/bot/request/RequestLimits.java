package org.wpcleaner.application.bot.request;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.time.Duration;

public record RequestLimits(int maxPageIds, int requestsPerMinute) {

  public Duration interval() {
    return Duration.ofMinutes(1).dividedBy(requestsPerMinute);
  }
}
