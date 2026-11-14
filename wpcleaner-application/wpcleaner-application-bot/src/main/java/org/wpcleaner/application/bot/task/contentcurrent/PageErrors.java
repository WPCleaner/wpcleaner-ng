package org.wpcleaner.application.bot.task.contentcurrent;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record PageErrors(
    @JsonProperty("pageId") int pageId,
    @JsonProperty("namespace") int namespace,
    @JsonProperty("title") String title,
    @JsonProperty("errors") List<ReportedError> errors) {

  public PageErrors {
    errors = List.copyOf(errors);
  }
}
