package org.wpcleaner.api.api.query.list.recentchanges;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.jspecify.annotations.Nullable;

@SuppressWarnings("PMD.DataClass")
public record RecentChange(
    @JsonProperty("autopatrolled") @Nullable String autopatrolled,
    @JsonProperty("bot") @Nullable String bot,
    @JsonProperty("comment") @Nullable String comment,
    @JsonProperty("logaction") @Nullable String logAction,
    @JsonProperty("logid") @Nullable Integer logId,
    @JsonProperty("logparams") @Nullable Map<String, Object> logParams,
    @JsonProperty("logtype") @Nullable String logType,
    @JsonProperty("minor") @Nullable String minor,
    @JsonProperty("newlen") @Nullable Integer newLen,
    @JsonProperty("ns") @Nullable Integer ns,
    @JsonProperty("oldlen") @Nullable Integer oldLen,
    @JsonProperty("old_revid") @Nullable Integer oldRevId,
    @JsonProperty("pageid") @Nullable Integer pageId,
    @JsonProperty("parsedcomment") @Nullable String parsedComment,
    @JsonProperty("patrolled") @Nullable String patrolled,
    @JsonProperty("rcid") @Nullable Integer rcId,
    @JsonProperty("redirect") @Nullable String redirect,
    @JsonProperty("revid") @Nullable Integer revId,
    @JsonProperty("sha1") @Nullable String sha1,
    @JsonProperty("tags") @JsonSetter(nulls = Nulls.AS_EMPTY) List<String> tags,
    @JsonProperty("timestamp") @Nullable Instant timestamp,
    @JsonProperty("title") @Nullable String title,
    @JsonProperty("type") @Nullable String type,
    @JsonProperty("user") @Nullable String user,
    @JsonProperty("userid") @Nullable Integer userId) {

  public boolean isAutopatrolled() {
    return autopatrolled != null;
  }

  public boolean isBot() {
    return bot != null;
  }

  public boolean isMinor() {
    return minor != null;
  }

  public boolean isPatrolled() {
    return patrolled != null;
  }

  public boolean isRedirect() {
    return redirect != null;
  }
}
