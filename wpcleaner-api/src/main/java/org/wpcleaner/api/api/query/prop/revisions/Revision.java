package org.wpcleaner.api.api.query.prop.revisions;

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

public record Revision(
    @JsonProperty("comment") @Nullable String comment,
    @JsonProperty("minor") @Nullable String minor,
    @JsonProperty("parentid") @Nullable Integer parentId,
    @JsonProperty("parsedcomment") @Nullable String parsedComment,
    @JsonProperty("revid") @Nullable Integer revid,
    @JsonProperty("sha1") @Nullable String sha1,
    @JsonProperty("size") @Nullable Integer size,
    @JsonProperty("slots") @JsonSetter(nulls = Nulls.AS_EMPTY) Map<String, RevisionSlot> slots,
    @JsonProperty("tags") @JsonSetter(nulls = Nulls.AS_EMPTY) List<String> tags,
    @JsonProperty("timestamp") @Nullable Instant timestamp,
    @JsonProperty("user") @Nullable String user,
    @JsonProperty("userid") @Nullable Integer userid) {

  public boolean isMinor() {
    return minor != null;
  }
}
