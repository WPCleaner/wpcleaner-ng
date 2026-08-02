package org.wpcleaner.api.api.query.list.recentchanges;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

@SuppressWarnings("PMD.ExcessiveParameterList")
public record RecentChange(
    boolean autopatrolled,
    boolean bot,
    @Nullable String comment,
    @Nullable String logAction,
    @Nullable Integer logId,
    @Nullable Map<String, Object> logParams,
    @Nullable String logType,
    boolean minor,
    @Nullable Integer newLen,
    @Nullable Integer ns,
    @Nullable Integer oldLen,
    @Nullable Integer oldRevId,
    @Nullable Integer pageId,
    @Nullable String parsedComment,
    boolean patrolled,
    @Nullable Integer rcId,
    boolean redirect,
    @Nullable Integer revId,
    @Nullable String sha1,
    List<String> tags,
    @Nullable Instant timestamp,
    @Nullable String title,
    @Nullable String type,
    @Nullable String user,
    @Nullable Integer userId) {
  @JsonCreator
  public RecentChange(
      @JsonProperty("autopatrolled") @Nullable final String autopatrolled,
      @JsonProperty("bot") @Nullable final String bot,
      @JsonProperty("comment") @Nullable final String comment,
      @JsonProperty("logaction") @Nullable final String logAction,
      @JsonProperty("logid") @Nullable final Integer logId,
      @JsonProperty("logparams") @Nullable final Map<String, Object> logParams,
      @JsonProperty("logtype") @Nullable final String logType,
      @JsonProperty("minor") @Nullable final String minor,
      @JsonProperty("newlen") @Nullable final Integer newLen,
      @JsonProperty("ns") @Nullable final Integer ns,
      @JsonProperty("oldlen") @Nullable final Integer oldLen,
      @JsonProperty("old_revid") @Nullable final Integer oldRevId,
      @JsonProperty("pageid") @Nullable final Integer pageId,
      @JsonProperty("parsedcomment") @Nullable final String parsedComment,
      @JsonProperty("patrolled") @Nullable final String patrolled,
      @JsonProperty("rcid") @Nullable final Integer rcId,
      @JsonProperty("redirect") @Nullable final String redirect,
      @JsonProperty("revid") @Nullable final Integer revId,
      @JsonProperty("sha1") @Nullable final String sha1,
      @JsonProperty("tags") @JsonSetter(nulls = Nulls.AS_EMPTY) final List<String> tags,
      @JsonProperty("timestamp") @Nullable final Instant timestamp,
      @JsonProperty("title") @Nullable final String title,
      @JsonProperty("type") @Nullable final String type,
      @JsonProperty("user") @Nullable final String user,
      @JsonProperty("userid") @Nullable final Integer userId) {
    this(
        Objects.nonNull(autopatrolled),
        Objects.nonNull(bot),
        comment,
        logAction,
        logId,
        logParams,
        logType,
        Objects.nonNull(minor),
        newLen,
        ns,
        oldLen,
        oldRevId,
        pageId,
        parsedComment,
        Objects.nonNull(patrolled),
        rcId,
        Objects.nonNull(redirect),
        revId,
        sha1,
        Objects.requireNonNullElseGet(tags, List::of),
        timestamp,
        title,
        type,
        user,
        userId);
  }
}
