package org.wpcleaner.application.gui.javafx.recentchanges.userpagehosting;

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

record UserPageHostingConfig(
    @JsonProperty("userPageSummary") String userPageSummary,
    @JsonProperty("userPageText") String userPageText,
    @JsonProperty("userTalkPageSummary") String userTalkPageSummary,
    @JsonProperty("userTalkPageTexts") @JsonSetter(nulls = Nulls.AS_EMPTY)
        List<UserTalkPageTextConfig> userTalkPageTexts) {
  @JsonCreator
  public UserPageHostingConfig(
      @Nullable final String userPageSummary,
      @Nullable final String userPageText,
      @Nullable final String userTalkPageSummary,
      @Nullable final List<UserTalkPageTextConfig> userTalkPageTexts) {
    this.userPageSummary = Objects.requireNonNullElse(userPageSummary, "");
    this.userPageText = Objects.requireNonNullElse(userPageText, "");
    this.userTalkPageSummary = Objects.requireNonNullElse(userTalkPageSummary, "");
    this.userTalkPageTexts = Objects.requireNonNullElse(userTalkPageTexts, List.of());
  }

  @JsonIgnore
  public boolean isComplete() {
    return isFieldComplete(userPageSummary)
        && isFieldComplete(userPageText)
        && isFieldComplete(userTalkPageSummary)
        && isTextsComplete();
  }

  private boolean isFieldComplete(@Nullable final String value) {
    return value != null && !value.isBlank();
  }

  private boolean isTextsComplete() {
    if (userTalkPageTexts.isEmpty()) {
      return false;
    }
    for (final UserTalkPageTextConfig textConfig : userTalkPageTexts) {
      if (!isFieldComplete(textConfig.label()) || !isFieldComplete(textConfig.text())) {
        return false;
      }
    }
    return true;
  }
}
