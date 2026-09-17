package org.wpcleaner.application.gui.javafx.recentchanges.userpagehosting;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

record UserTalkPageTextConfig(String label, String text, Boolean addedByDefault) {
  @JsonCreator
  public UserTalkPageTextConfig(
      @Nullable final String label,
      @Nullable final String text,
      @Nullable final Boolean addedByDefault) {
    this.label = Objects.requireNonNullElse(label, "");
    this.text = Objects.requireNonNullElse(text, "");
    this.addedByDefault = Objects.requireNonNullElse(addedByDefault, Boolean.FALSE);
  }
}
