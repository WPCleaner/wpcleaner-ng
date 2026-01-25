package org.wpcleaner.api.api;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import org.springframework.stereotype.Service;
import org.wpcleaner.api.wiki.definition.KnownDefinitions;
import org.wpcleaner.api.wiki.definition.WikiDefinition;

@Service
public class CurrentUserService {

  private final KnownDefinitions knownDefinitions;
  private ConnectedUser currentUser;

  public CurrentUserService(final KnownDefinitions knownDefinitions) {
    this.knownDefinitions = knownDefinitions;
    currentUser = ConnectedUser.notLoggedIn(knownDefinitions.getPreferred());
  }

  public ConnectedUser getCurrentUser() {
    return currentUser;
  }

  public void login(final WikiDefinition wiki, final String username, final boolean demo) {
    currentUser = currentUser.withLogin(wiki, username, demo);
  }

  public void logout() {
    currentUser = ConnectedUser.notLoggedIn(knownDefinitions.getPreferred());
  }
}
