package org.wpcleaner.application.gui.javafx.core.action.content.append;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import java.util.List;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

public record ContentAppendConfiguration(
    @JsonProperty("summary") String summary,
    @JsonProperty("texts") @JsonSetter(nulls = Nulls.AS_EMPTY)
        List<ContentAppendConfigurationText> texts) {
  @JsonCreator
  public ContentAppendConfiguration(
      @Nullable final String summary, @Nullable final List<ContentAppendConfigurationText> texts) {
    this.summary = Objects.requireNonNullElse(summary, "");
    this.texts = Objects.requireNonNullElse(texts, List.of());
  }

  @JsonIgnore
  public boolean isComplete() {
    if (summary.isBlank() || texts.isEmpty()) {
      return false;
    }
    for (final ContentAppendConfigurationText text : texts) {
      if (!text.isComplete()) {
        return false;
      }
    }
    return true;
  }
}
