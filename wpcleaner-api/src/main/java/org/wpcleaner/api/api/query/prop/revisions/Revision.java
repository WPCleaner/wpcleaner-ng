package org.wpcleaner.api.api.query.prop.revisions;

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
public record Revision(
    @Nullable String comment,
    boolean minor,
    @Nullable Integer parentId,
    @Nullable String parsedComment,
    @Nullable Integer revId,
    @Nullable String sha1,
    @Nullable Integer size,
    Map<String, RevisionSlot> slots,
    List<String> tags,
    @Nullable Instant timestamp,
    @Nullable String user,
    @Nullable Integer userId) {
  @JsonCreator
  public Revision(
      @JsonProperty("comment") @Nullable final String comment,
      @JsonProperty("minor") @Nullable final String minor,
      @JsonProperty("parentid") @Nullable final Integer parentId,
      @JsonProperty("parsedcomment") @Nullable final String parsedComment,
      @JsonProperty("revid") @Nullable final Integer revId,
      @JsonProperty("sha1") @Nullable final String sha1,
      @JsonProperty("size") @Nullable final Integer size,
      @JsonProperty("slots") @JsonSetter(nulls = Nulls.AS_EMPTY)
          final Map<String, RevisionSlot> slots,
      @JsonProperty("tags") @JsonSetter(nulls = Nulls.AS_EMPTY) final List<String> tags,
      @JsonProperty("timestamp") @Nullable final Instant timestamp,
      @JsonProperty("user") @Nullable final String user,
      @JsonProperty("userid") @Nullable final Integer userId) {
    this(
        comment,
        Objects.nonNull(minor),
        parentId,
        parsedComment,
        revId,
        sha1,
        size,
        Objects.requireNonNullElseGet(slots, Map::of),
        Objects.requireNonNullElseGet(tags, List::of),
        timestamp,
        user,
        userId);
  }
}
