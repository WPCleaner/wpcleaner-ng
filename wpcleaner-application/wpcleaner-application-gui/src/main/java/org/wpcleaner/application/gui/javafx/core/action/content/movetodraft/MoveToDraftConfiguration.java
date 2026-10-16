package org.wpcleaner.application.gui.javafx.core.action.content.movetodraft;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

public record MoveToDraftConfiguration(
    @JsonProperty("subPageName") String subPageName, @JsonProperty("summary") String summary) {
  @JsonCreator
  public MoveToDraftConfiguration(
      @JsonProperty("subPageName") @Nullable final String subPageName,
      @JsonProperty("summary") @Nullable final String summary) {
    this.subPageName = Objects.requireNonNullElse(subPageName, "");
    this.summary = Objects.requireNonNullElse(summary, "");
  }

  @JsonIgnore
  public boolean isComplete() {
    return !subPageName.isBlank() && !summary.isBlank();
  }
}
