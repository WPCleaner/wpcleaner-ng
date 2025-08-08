package org.wpcleaner.api.wiki.definition;

/*
 * SPDX-FileCopyrightText: © 2024 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

public enum WikiGroup {
  WIKIMEDIA(true),
  OTHER(false);

  private final boolean authenticationShared;

  WikiGroup(final boolean authenticationShared) {
    this.authenticationShared = authenticationShared;
  }

  public boolean isAuthenticationShared() {
    return authenticationShared;
  }
}
