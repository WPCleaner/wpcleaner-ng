package org.wpcleaner.api.repository.namespace;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

public enum CommonNamespaces {
  MEDIA(-2),
  SPECIAL(-1),
  MAIN(0),
  MAIN_TALK(1),
  USER(2),
  USER_TALK(3),
  PROJECT(4), // On Wikipedia, this is WIKIPEDIA namespace
  PROJECT_TALK(5), // On Wikipedia, this is WIKIPEDIA_TALK namespace
  FILE(6),
  FILE_TALK(7),
  MEDIAWIKI(8),
  MEDIAWIKI_TALK(9),
  TEMPLATE(10),
  TEMPLATE_TALK(11),
  HELP(12),
  HELP_TALK(13),
  CATEGORY(14),
  CATEGORY_TALK(15),
  PORTAL(100),
  PORTAL_TALK(101),
  TIMED_TEXT(710),
  TIMED_TEXT_TALK(711),
  MODULE(828),
  MODULE_TALK(829),
  EVENT(1728),
  EVENT_TALK(1729);

  public final int id;

  CommonNamespaces(final int id) {
    this.id = id;
  }
}
