package org.wpcleaner.api.api.edit;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.time.Instant;
import java.util.List;
import org.jspecify.annotations.Nullable;

@SuppressWarnings("PMD.AvoidFieldNameMatchingMethodName")
public record EditQueryCommon(
    @Nullable String appendText,
    @Nullable Instant baseTimestamp,
    @Nullable Boolean bot,
    @Nullable String contentFormat,
    @Nullable String contentModel,
    @Nullable Boolean createOnly,
    @Nullable String md5,
    @Nullable Boolean minor,
    @Nullable Boolean noCreate,
    @Nullable Boolean notMinor,
    @Nullable String prependText,
    @Nullable Boolean recreate,
    @Nullable Boolean redirect,
    @Nullable String section,
    @Nullable String sectionTitle,
    @Nullable Instant startTimestamp,
    @Nullable String summary,
    @Nullable List<String> tags,
    @Nullable String text,
    String token,
    @Nullable Integer undo,
    @Nullable Integer undoAfter,
    @Nullable String watchlist) {

  public Builder builder() {
    return emptyBuilder()
        .appendText(appendText)
        .baseTimestamp(baseTimestamp)
        .bot(bot)
        .contentFormat(contentFormat)
        .contentModel(contentModel)
        .createOnly(createOnly)
        .md5(md5)
        .minor(minor)
        .noCreate(noCreate)
        .notMinor(notMinor)
        .prependText(prependText)
        .recreate(recreate)
        .redirect(redirect)
        .section(section)
        .sectionTitle(sectionTitle)
        .startTimestamp(startTimestamp)
        .summary(summary)
        .tags(tags)
        .text(text)
        .token(token)
        .undo(undo)
        .undoAfter(undoAfter)
        .watchlist(watchlist);
  }

  public static Builder emptyBuilder() {
    return new Builder();
  }

  @SuppressWarnings("PMD.TooManyFields")
  public static class Builder {

    @Nullable private String appendText;
    @Nullable private Instant baseTimestamp;
    @Nullable private Boolean bot;
    @Nullable private String contentFormat;
    @Nullable private String contentModel;
    @Nullable private Boolean createOnly;
    @Nullable private String md5;
    @Nullable private Boolean minor;
    @Nullable private Boolean noCreate;
    @Nullable private Boolean notMinor;
    @Nullable private String prependText;
    @Nullable private Boolean recreate;
    @Nullable private Boolean redirect;
    @Nullable private String section;
    @Nullable private String sectionTitle;
    @Nullable private Instant startTimestamp;
    @Nullable private String summary;
    @Nullable private List<String> tags;
    @Nullable private String text;
    private String token = "";
    @Nullable private Integer undo;
    @Nullable private Integer undoAfter;
    @Nullable private String watchlist;

    public Builder appendText(@Nullable final String appendText) {
      this.appendText = appendText;
      return this;
    }

    public Builder baseTimestamp(@Nullable final Instant baseTimestamp) {
      this.baseTimestamp = baseTimestamp;
      return this;
    }

    public Builder bot(@Nullable final Boolean bot) {
      this.bot = bot;
      return this;
    }

    public Builder contentFormat(@Nullable final String contentFormat) {
      this.contentFormat = contentFormat;
      return this;
    }

    public Builder contentModel(@Nullable final String contentModel) {
      this.contentModel = contentModel;
      return this;
    }

    public Builder createOnly(@Nullable final Boolean createOnly) {
      this.createOnly = createOnly;
      return this;
    }

    public Builder md5(@Nullable final String md5) {
      this.md5 = md5;
      return this;
    }

    public Builder minor(@Nullable final Boolean minor) {
      this.minor = minor;
      return this;
    }

    public Builder noCreate(@Nullable final Boolean noCreate) {
      this.noCreate = noCreate;
      return this;
    }

    public Builder notMinor(@Nullable final Boolean notMinor) {
      this.notMinor = notMinor;
      return this;
    }

    public Builder prependText(@Nullable final String prependText) {
      this.prependText = prependText;
      return this;
    }

    public Builder recreate(@Nullable final Boolean recreate) {
      this.recreate = recreate;
      return this;
    }

    public Builder redirect(@Nullable final Boolean redirect) {
      this.redirect = redirect;
      return this;
    }

    public Builder section(@Nullable final String section) {
      this.section = section;
      return this;
    }

    public Builder sectionTitle(@Nullable final String sectionTitle) {
      this.sectionTitle = sectionTitle;
      return this;
    }

    public Builder startTimestamp(@Nullable final Instant startTimestamp) {
      this.startTimestamp = startTimestamp;
      return this;
    }

    public Builder summary(@Nullable final String summary) {
      this.summary = summary;
      return this;
    }

    public Builder tags(@Nullable final List<String> tags) {
      this.tags = tags;
      return this;
    }

    public Builder text(@Nullable final String text) {
      this.text = text;
      return this;
    }

    public Builder token(final String token) {
      this.token = token;
      return this;
    }

    public Builder undo(@Nullable final Integer undo) {
      this.undo = undo;
      return this;
    }

    public Builder undoAfter(@Nullable final Integer undoAfter) {
      this.undoAfter = undoAfter;
      return this;
    }

    public Builder watchlist(@Nullable final String watchlist) {
      this.watchlist = watchlist;
      return this;
    }

    public EditQueryCommon build() {
      return new EditQueryCommon(
          appendText,
          baseTimestamp,
          bot,
          contentFormat,
          contentModel,
          createOnly,
          md5,
          minor,
          noCreate,
          notMinor,
          prependText,
          recreate,
          redirect,
          section,
          sectionTitle,
          startTimestamp,
          summary,
          tags,
          text,
          token,
          undo,
          undoAfter,
          watchlist);
    }
  }
}
