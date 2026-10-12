package org.wpcleaner.api.api.move;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

public enum MoveParameters {
  FROM("from"),
  FROM_ID("fromid"),
  IGNORE_WARNINGS("ignorewarnings"),
  MOVE_SUBPAGES("movesubpages"),
  MOVE_TALK("movetalk"),
  NO_REDIRECT("noredirect"),
  REASON("reason"),
  TAGS("tags"),
  TO("to"),
  TOKEN("token"),
  WATCHLIST("watchlist"),
  WATCHLIST_EXPIRY("watchlistexpiry"),
  ;

  public final String value;

  MoveParameters(final String value) {
    this.value = value;
  }

  public enum Watchlist {
    NO_CHANGE("nochange"),
    PREFERENCES("preferences"),
    UNWATCH("unwatch"),
    WATCH("watch"),
    ;

    public final String value;

    Watchlist(final String value) {
      this.value = value;
    }
  }
}
