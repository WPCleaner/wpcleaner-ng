package org.wpcleaner.application.gui.javafx.core.action.content.append;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

public record ContentAppendConfigurationText(
    @JsonAlias("defaultSelected") @JsonProperty("addedByDefault") Boolean addedByDefault,
    @JsonProperty("label") String label,
    @JsonProperty("text") String text) {
  @JsonCreator
  public ContentAppendConfigurationText(
      @JsonAlias("defaultSelected") @JsonProperty("addedByDefault") @Nullable
          final Boolean addedByDefault,
      @JsonProperty("label") @Nullable final String label,
      @JsonProperty("text") @Nullable final String text) {
    this.addedByDefault = Objects.requireNonNullElse(addedByDefault, Boolean.FALSE);
    this.label = Objects.requireNonNullElse(label, "");
    this.text = Objects.requireNonNullElse(text, "");
  }

  @JsonIgnore
  public boolean isComplete() {
    return !label.isBlank() && !text.isBlank();
  }
}
