package org.wpcleaner.api.api.query.list.categorymembers;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

public enum CategoryMembersParameters {
  CONTINUE("cmcontinue"),
  DIRECTION("cmdir"),
  END("cmend"),
  END_HEX_SORT_KEY("cmendhexsortkey"),
  END_SORT_KEY_PREFIX("cmendsortkeyprefix"),
  LIMIT("cmlimit"),
  NAMESPACE("cmnamespace"),
  PAGE_ID("cmpageid"),
  PROPERTIES("cmprop"),
  SORT("cmsort"),
  START("cmstart"),
  START_HEX_SORT_KEY("cmstarthexsortkey"),
  START_SORT_KEY_PREFIX("cmstartsortkeyprefix"),
  TITLE("cmtitle"),
  TYPE("cmtype"),
  ;

  public final String value;

  CategoryMembersParameters(final String value) {
    this.value = value;
  }

  public enum Direction {
    ASC("asc"),
    ASCENDING("ascending"),
    DESC("desc"),
    DESCENDING("descending"),
    NEWER("newer"),
    OLDER("older"),
    ;

    public final String value;

    Direction(final String value) {
      this.value = value;
    }
  }

  public enum Properties {
    IDS("ids"),
    SORT_KEY("sortkey"),
    SORT_KEY_PREFIX("sortkeyprefix"),
    TIMESTAMP("timestamp"),
    TITLE("title"),
    TYPE("type"),
    ;

    public final String value;

    Properties(final String value) {
      this.value = value;
    }
  }

  public enum Sort {
    SORT_KEY("sortkey"),
    TIMESTAMP("timestamp"),
    ;

    public final String value;

    Sort(final String value) {
      this.value = value;
    }
  }

  public enum Type {
    FILE("file"),
    PAGE("page"),
    SUBCATEGORY("subcat"),
    ;

    public final String value;

    Type(final String value) {
      this.value = value;
    }
  }
}
