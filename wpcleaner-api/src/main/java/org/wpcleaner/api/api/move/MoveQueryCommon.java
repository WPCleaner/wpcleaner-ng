package org.wpcleaner.api.api.move;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.time.Instant;
import java.util.List;
import org.jspecify.annotations.Nullable;

@SuppressWarnings("PMD.AvoidFieldNameMatchingMethodName")
public record MoveQueryCommon(
    @Nullable Boolean ignoreWarnings,
    @Nullable Boolean moveSubpages,
    @Nullable Boolean moveTalk,
    @Nullable Boolean noRedirect,
    @Nullable String reason,
    @Nullable List<String> tags,
    String to,
    String token,
    @Nullable String watchlist,
    @Nullable Instant watchlistExpiry) {

  public Builder builder() {
    return emptyBuilder()
        .ignoreWarnings(ignoreWarnings)
        .moveSubpages(moveSubpages)
        .moveTalk(moveTalk)
        .noRedirect(noRedirect)
        .reason(reason)
        .tags(tags)
        .to(to)
        .token(token)
        .watchlist(watchlist)
        .watchlistExpiry(watchlistExpiry);
  }

  public static Builder emptyBuilder() {
    return new Builder();
  }

  public static class Builder {

    @Nullable private Boolean ignoreWarnings;
    @Nullable private Boolean moveSubpages;
    @Nullable private Boolean moveTalk;
    @Nullable private Boolean noRedirect;
    @Nullable private String reason;
    @Nullable private List<String> tags;
    private String to = "";
    private String token = "";
    @Nullable private String watchlist;
    @Nullable private Instant watchlistExpiry;

    public Builder ignoreWarnings(@Nullable final Boolean ignoreWarnings) {
      this.ignoreWarnings = ignoreWarnings;
      return this;
    }

    public Builder moveSubpages(@Nullable final Boolean moveSubpages) {
      this.moveSubpages = moveSubpages;
      return this;
    }

    public Builder moveTalk(@Nullable final Boolean moveTalk) {
      this.moveTalk = moveTalk;
      return this;
    }

    public Builder noRedirect(@Nullable final Boolean noRedirect) {
      this.noRedirect = noRedirect;
      return this;
    }

    public Builder reason(@Nullable final String reason) {
      this.reason = reason;
      return this;
    }

    public Builder tags(@Nullable final List<String> tags) {
      this.tags = tags;
      return this;
    }

    public Builder to(final String to) {
      this.to = to;
      return this;
    }

    public Builder token(final String token) {
      this.token = token;
      return this;
    }

    public Builder watchlist(@Nullable final String watchlist) {
      this.watchlist = watchlist;
      return this;
    }

    public Builder watchlistExpiry(@Nullable final Instant watchlistExpiry) {
      this.watchlistExpiry = watchlistExpiry;
      return this;
    }

    public MoveQueryCommon build() {
      return new MoveQueryCommon(
          ignoreWarnings,
          moveSubpages,
          moveTalk,
          noRedirect,
          reason,
          tags,
          to,
          token,
          watchlist,
          watchlistExpiry);
    }
  }
}
