package org.wpcleaner.api.api;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import org.wpcleaner.api.wiki.definition.WikiDefinition;

public record ConnectedUser(
    boolean isLoggedIn, WikiDefinition wiki, String username, boolean demo) {

  static ConnectedUser notLoggedIn(final WikiDefinition wiki) {
    return new ConnectedUser(false, wiki, "", false);
  }

  ConnectedUser withLogin(final WikiDefinition wiki, final String username, final boolean demo) {
    return new ConnectedUser(true, wiki, username, demo);
  }
}
