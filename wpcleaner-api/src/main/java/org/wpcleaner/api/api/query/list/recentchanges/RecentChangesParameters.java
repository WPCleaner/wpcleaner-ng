package org.wpcleaner.api.api.query.list.recentchanges;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import com.fasterxml.jackson.annotation.JsonProperty;

public enum RecentChangesParameters {
  CONTINUE("rccontinue"),
  DIRECTION("rcdir"),
  END("rcend"),
  EXCLUDE_USER("rcexcludeuser"),
  GENERATE_REVISIONS("rcgeneraterevisions"),
  LIMIT("rclimit"),
  NAMESPACE("rcnamespace"),
  PROPERTIES("rcprop"),
  SHOW("rcshow"),
  SLOT("rcslot"),
  START("rcstart"),
  TAG("rctag"),
  TITLE("rctitle"),
  TOP_ONLY("rctoponly"),
  TYPE("rctype"),
  USER("rcuser"),
  ;

  public final String value;

  RecentChangesParameters(final String value) {
    this.value = value;
  }

  public enum Direction {
    NEWER("newer"),
    OLDER("older"),
    ;

    public final String value;

    Direction(final String value) {
      this.value = value;
    }
  }

  public enum Properties {
    COMMENT("comment"),
    FLAGS("flags"),
    IDS("ids"),
    LOG_INFO("loginfo"),
    PARSED_COMMENT("parsedcomment"),
    PATROLLED("patrolled"),
    REDIRECT("redirect"),
    SHA1("sha1"),
    SIZES("sizes"),
    TAGS("tags"),
    TIMESTAMP("timestamp"),
    TITLE("title"),
    USER("user"),
    USER_ID("userid"),
    ;

    public final String value;

    Properties(final String value) {
      this.value = value;
    }
  }

  public enum Show {
    @JsonProperty("anon")
    ANON("anon"),
    @JsonProperty("autopatrolled")
    AUTOPATROLLED("autopatrolled"),
    @JsonProperty("bot")
    BOT("bot"),
    @JsonProperty("minor")
    MINOR("minor"),
    @JsonProperty("not_anon")
    NOT_ANON("!anon"),
    @JsonProperty("not_autopatrolled")
    NOT_AUTOPATROLLED("!autopatrolled"),
    @JsonProperty("not_bot")
    NOT_BOT("!bot"),
    @JsonProperty("not_minor")
    NOT_MINOR("!minor"),
    @JsonProperty("not_patrolled")
    NOT_PATROLLED("!patrolled"),
    @JsonProperty("not_redirect")
    NOT_REDIRECT("!redirect"),
    @JsonProperty("patrolled")
    PATROLLED("patrolled"),
    @JsonProperty("redirect")
    REDIRECT("redirect"),
    @JsonProperty("unpatrolled")
    UNPATROLLED("unpatrolled"),
    ;

    public final String value;

    Show(final String value) {
      this.value = value;
    }
  }

  public enum Slot {
    MAIN("main"),
    ;

    public final String value;

    Slot(final String value) {
      this.value = value;
    }
  }

  public enum Type {
    @JsonProperty("categorize")
    CATEGORIZE("categorize"),
    @JsonProperty("edit")
    EDIT("edit"),
    @JsonProperty("external")
    EXTERNAL("external"),
    @JsonProperty("log")
    LOG("log"),
    @JsonProperty("new")
    NEW("new"),
    ;

    public final String value;

    Type(final String value) {
      this.value = value;
    }
  }
}
