package org.wpcleaner.application.gui.javafx.recentchanges.userpagedraft;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import org.jspecify.annotations.Nullable;
import org.wpcleaner.application.gui.javafx.core.action.content.append.ContentAppendConfiguration;
import org.wpcleaner.application.gui.javafx.core.action.content.movetodraft.MoveToDraftConfiguration;
import org.wpcleaner.application.gui.javafx.core.action.content.replace.ContentReplaceConfiguration;

record UserPageDraftConfig(
    @JsonProperty("moveToDraft") MoveToDraftConfiguration moveToDraft,
    @JsonProperty("userPage") ContentReplaceConfiguration userPage,
    @JsonProperty("userTalkPage") ContentAppendConfiguration userTalkPage) {
  @JsonCreator
  public UserPageDraftConfig(
      @Nullable final MoveToDraftConfiguration moveToDraft,
      @Nullable final ContentReplaceConfiguration userPage,
      @Nullable final ContentAppendConfiguration userTalkPage) {
    this.moveToDraft =
        Objects.requireNonNullElseGet(moveToDraft, () -> new MoveToDraftConfiguration("", ""));
    this.userPage =
        Objects.requireNonNullElseGet(userPage, () -> new ContentReplaceConfiguration("", ""));
    this.userTalkPage =
        Objects.requireNonNullElseGet(userTalkPage, () -> new ContentAppendConfiguration("", null));
  }

  @JsonIgnore
  public boolean isComplete() {
    return moveToDraft.isComplete() && userPage.isComplete() && userTalkPage.isComplete();
  }
}
