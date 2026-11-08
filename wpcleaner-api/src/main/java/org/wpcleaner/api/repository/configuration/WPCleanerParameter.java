package org.wpcleaner.api.repository.configuration;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

public enum WPCleanerParameter {
  CHECK_WIKI_COMMENT("check_wiki_comment", true, false),
  CHECK_WIKI_PROJECT_PAGE("check_wiki_project_page", true, false),
  CHECK_WIKI_TRANSLATION_PAGE("check_wiki_translation_page", true, false);

  public final String value;
  private final boolean general;
  private final boolean user;

  WPCleanerParameter(final String value, final boolean general, final boolean user) {
    this.value = value;
    this.general = general;
    this.user = user;
  }

  boolean isGeneral() {
    return general;
  }

  boolean isUser() {
    return user;
  }
}
