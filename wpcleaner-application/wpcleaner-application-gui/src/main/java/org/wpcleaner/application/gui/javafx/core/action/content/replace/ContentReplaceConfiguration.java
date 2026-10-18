package org.wpcleaner.application.gui.javafx.core.action.content.replace;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

public record ContentReplaceConfiguration(
    @JsonProperty("summary") String summary, @JsonProperty("text") String text) {
  @JsonCreator
  public ContentReplaceConfiguration(@Nullable final String summary, @Nullable final String text) {
    this.summary = Objects.requireNonNullElse(summary, "");
    this.text = Objects.requireNonNullElse(text, "");
  }

  @JsonIgnore
  public boolean isComplete() {
    return !summary.isBlank() && !text.isBlank();
  }
}
