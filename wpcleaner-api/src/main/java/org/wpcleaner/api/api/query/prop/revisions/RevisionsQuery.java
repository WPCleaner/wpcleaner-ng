package org.wpcleaner.api.api.query.prop.revisions;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.jspecify.annotations.Nullable;
import org.wpcleaner.api.api.query.prop.revisions.RevisionsParameters.Direction;
import org.wpcleaner.api.api.query.prop.revisions.RevisionsParameters.Properties;

@SuppressWarnings("PMD.AvoidFieldNameMatchingMethodName")
public record RevisionsQuery(
    @Nullable String contentFormatMain,
    @Nullable Direction direction,
    @Nullable Instant end,
    @Nullable Integer endId,
    @Nullable String excludeUser,
    @Nullable String limit,
    @Nullable Set<Properties> properties,
    @Nullable String rvContinue,
    @Nullable Integer section,
    @Nullable Set<String> slots,
    @Nullable Instant start,
    @Nullable Integer startId,
    @Nullable String tag,
    @Nullable String user) {

  public RevisionsQuery {
    checkLimit(limit);
  }

  private static void checkLimit(@Nullable final String limit) {
    if (limit == null || Objects.equals(limit, "max")) {
      return;
    }
    try {
      //noinspection ResultOfMethodCallIgnored
      Integer.parseUnsignedInt(limit);
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException(
          "RevisionsQuery.limit should be an integer or the String max", e);
    }
  }

  public Builder builder() {
    return emptyBuilder()
        .contentFormatMain(contentFormatMain)
        .direction(direction)
        .end(end)
        .endId(endId)
        .excludeUser(excludeUser)
        .limit(limit)
        .properties(properties)
        .rvContinue(rvContinue)
        .section(section)
        .slots(slots)
        .start(start)
        .startId(startId)
        .tag(tag)
        .user(user);
  }

  public static Builder emptyBuilder() {
    return new Builder();
  }

  public static class Builder {

    @Nullable private String contentFormatMain;
    @Nullable private Direction direction;
    @Nullable private Instant end;
    @Nullable private Integer endId;
    @Nullable private String excludeUser;
    @Nullable private String limit;
    @Nullable private Set<Properties> properties;
    @Nullable private String rvContinue;
    @Nullable private Integer section;
    @Nullable private Set<String> slots;
    @Nullable private Instant start;
    @Nullable private Integer startId;
    @Nullable private String tag;
    @Nullable private String user;

    public Builder contentFormatMain(@Nullable final String contentFormatMain) {
      this.contentFormatMain = contentFormatMain;
      return this;
    }

    public Builder direction(@Nullable final Direction direction) {
      this.direction = direction;
      return this;
    }

    public Builder end(@Nullable final Instant end) {
      this.end = end;
      return this;
    }

    public Builder endId(@Nullable final Integer endId) {
      this.endId = endId;
      return this;
    }

    public Builder excludeUser(@Nullable final String excludeUser) {
      this.excludeUser = excludeUser;
      return this;
    }

    public Builder limit(@Nullable final Integer limit) {
      this.limit = Optional.ofNullable(limit).map(Integer::toUnsignedString).orElse(null);
      return this;
    }

    public Builder limit(@Nullable final String limit) {
      this.limit = limit;
      return this;
    }

    public Builder properties(@Nullable final Set<Properties> properties) {
      this.properties = properties;
      return this;
    }

    public Builder rvContinue(@Nullable final String rvContinue) {
      this.rvContinue = rvContinue;
      return this;
    }

    public Builder section(@Nullable final Integer section) {
      this.section = section;
      return this;
    }

    public Builder slots(@Nullable final Set<String> slots) {
      this.slots = slots;
      return this;
    }

    public Builder start(@Nullable final Instant start) {
      this.start = start;
      return this;
    }

    public Builder startId(@Nullable final Integer startId) {
      this.startId = startId;
      return this;
    }

    public Builder tag(@Nullable final String tag) {
      this.tag = tag;
      return this;
    }

    public Builder user(@Nullable final String user) {
      this.user = user;
      return this;
    }

    public RevisionsQuery build() {
      return new RevisionsQuery(
          contentFormatMain,
          direction,
          end,
          endId,
          excludeUser,
          limit,
          properties,
          rvContinue,
          section,
          slots,
          start,
          startId,
          tag,
          user);
    }
  }
}
