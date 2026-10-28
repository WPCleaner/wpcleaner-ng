package org.wpcleaner.application.gui.javafx.recentchanges;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import org.wpcleaner.application.gui.javafx.core.action.content.append.ContentAppendAction;

public final class UserPageVariables {

  private UserPageVariables() {}

  public static String getUserName(final String userPageTitle) {
    final int colon = userPageTitle.indexOf(':');
    return colon >= 0 ? userPageTitle.substring(colon + 1) : userPageTitle;
  }

  public static void setUserVariables(final ContentAppendAction action, final String userName) {
    action.setVariable("__USER__", userName);
    action.setVariable("__USER_PAGE__", userName.replace(' ', '_'));
  }
}
