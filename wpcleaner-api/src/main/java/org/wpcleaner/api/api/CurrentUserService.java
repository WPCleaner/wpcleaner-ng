package org.wpcleaner.api.api;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import org.springframework.stereotype.Service;
import org.wpcleaner.api.wiki.definition.KnownDefinitions;
import org.wpcleaner.api.wiki.definition.WikiDefinition;

@Service
public class CurrentUserService {

  private final KnownDefinitions knownDefinitions;
  private final List<LogoutHook> logoutHooks;
  private ConnectedUser currentUser;

  public CurrentUserService(
      final KnownDefinitions knownDefinitions, final List<LogoutHook> logoutHooks) {
    this.knownDefinitions = knownDefinitions;
    this.logoutHooks = List.copyOf(logoutHooks);
    currentUser = ConnectedUser.notLoggedIn(knownDefinitions.getPreferred());
  }

  public ConnectedUser getCurrentUser() {
    return currentUser;
  }

  public void login(final WikiDefinition wiki, final String username, final boolean demo) {
    currentUser = currentUser.withLogin(wiki, username, demo);
  }

  public void withGroups(final List<String> groups) {
    currentUser = currentUser.withGroups(groups);
  }

  public void withRights(final List<String> rights) {
    currentUser = currentUser.withRights(rights);
  }

  public void logout() {
    for (final LogoutHook logoutHook : logoutHooks) {
      logoutHook.logout(currentUser);
    }
    currentUser = ConnectedUser.notLoggedIn(knownDefinitions.getPreferred());
  }
}
