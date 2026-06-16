package org.wpcleaner.api.api.query.list.recentchanges;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

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
    ANON("anon"),
    AUTOPATROLLED("autopatrolled"),
    BOT("bot"),
    MINOR("minor"),
    NOT_ANON("!anon"),
    NOT_AUTOPATROLLED("!autopatrolled"),
    NOT_BOT("!bot"),
    NOT_MINOR("!minor"),
    NOT_PATROLLED("!patrolled"),
    NOT_REDIRECT("!redirect"),
    PATROLLED("patrolled"),
    REDIRECT("redirect"),
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
    CATEGORIZE("categorize"),
    EDIT("edit"),
    EXTERNAL("external"),
    LOG("log"),
    NEW("new"),
    ;

    public final String value;

    Type(final String value) {
      this.value = value;
    }
  }
}
