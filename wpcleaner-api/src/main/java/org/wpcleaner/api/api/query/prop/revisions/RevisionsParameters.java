package org.wpcleaner.api.api.query.prop.revisions;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

public enum RevisionsParameters {
  CONTENT_FORMAT_MAIN("rvcontentformat-main"),
  CONTINUE("rvcontinue"),
  DIRECTION("rvdir"),
  END("rvend"),
  END_ID("rvendid"),
  EXCLUDE_USER("rvexcludeuser"),
  LIMIT("rvlimit"),
  PROPERTIES("rvprop"),
  SECTION("rvsection"),
  SLOTS("rvslots"),
  START("rvstart"),
  START_ID("rvstartid"),
  TAG("rvtag"),
  USER("rvuser"),
  ;

  public final String value;

  RevisionsParameters(final String value) {
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
    CONTENT("content"),
    CONTENT_MODEL("contentmodel"),
    FLAGS("flags"),
    IDS("ids"),
    PARSED_COMMENT("parsedcomment"),
    ROLES("roles"),
    SHA1("sha1"),
    SIZE("size"),
    SLOT_SHA1("slotsha1"),
    SLOT_SIZE("slotsize"),
    TAGS("tags"),
    TIMESTAMP("timestamp"),
    USER("user"),
    USER_ID("userid"),
    ;

    public final String value;

    Properties(final String value) {
      this.value = value;
    }
  }
}
