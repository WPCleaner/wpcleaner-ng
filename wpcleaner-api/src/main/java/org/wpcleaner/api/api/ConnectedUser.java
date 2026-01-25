package org.wpcleaner.api.api;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import org.wpcleaner.api.wiki.definition.WikiDefinition;

public record ConnectedUser(
    boolean isLoggedIn,
    WikiDefinition wiki,
    String username,
    boolean demo,
    List<String> groups,
    List<String> rights) {

  static ConnectedUser notLoggedIn(final WikiDefinition wiki) {
    return new ConnectedUser(false, wiki, "", false, List.of(), List.of());
  }

  ConnectedUser withLogin(final WikiDefinition wiki, final String username, final boolean demo) {
    return new ConnectedUser(true, wiki, username, demo, List.of(), List.of());
  }

  ConnectedUser withGroups(final List<String> groups) {
    return new ConnectedUser(isLoggedIn, wiki, username, demo, List.copyOf(groups), rights);
  }

  ConnectedUser withRights(final List<String> rights) {
    return new ConnectedUser(isLoggedIn, wiki, username, demo, groups, List.copyOf(rights));
  }
}
