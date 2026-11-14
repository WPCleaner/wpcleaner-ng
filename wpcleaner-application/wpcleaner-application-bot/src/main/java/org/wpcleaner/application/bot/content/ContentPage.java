package org.wpcleaner.application.bot.content;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

public record ContentPage(int id, int namespace, String title, String model, String text) {

  public static final String WIKITEXT_MODEL = "wikitext";

  public boolean isWikitext() {
    return WIKITEXT_MODEL.equals(model);
  }
}
