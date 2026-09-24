package org.wpcleaner.application.gui.javafx.recentchanges.userpagehosting;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.net.URI;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.wpcleaner.api.repository.namespace.CommonNamespaces;
import org.wpcleaner.application.gui.javafx.recentchanges.FilteredRecentChange;
import org.wpcleaner.application.gui.javafx.recentchanges.JavaFxRecentChangesWindowServices;
import org.wpcleaner.application.gui.javafx.recentchanges.RecentChangesAction;
import org.wpcleaner.application.gui.javafx.recentchanges.options.RecentChangesFilter;

class UserPageHostingActionTest {

  @DisplayName("canApply returns true only for user pages that are not subpages")
  @Test
  void testCanApply() {
    final JavaFxRecentChangesWindowServices services =
        Mockito.mock(JavaFxRecentChangesWindowServices.class);
    final RecentChangesAction action = new UserPageHostingAction(services);

    final FilteredRecentChange userPage =
        createRecentChange(CommonNamespaces.USER.id, "User:NicoV");
    final FilteredRecentChange userSubPage =
        createRecentChange(CommonNamespaces.USER.id, "User:NicoV/sandbox");
    final FilteredRecentChange articlePage =
        createRecentChange(CommonNamespaces.MAIN.id, "Main Page");
    final FilteredRecentChange nullNamespacePage = createRecentChange(null, "User:NicoV");

    Assertions.assertThat(action.canApply(userPage)).isTrue();
    Assertions.assertThat(action.canApply(userSubPage)).isFalse();
    Assertions.assertThat(action.canApply(articlePage)).isFalse();
    Assertions.assertThat(action.canApply(nullNamespacePage)).isFalse();
  }

  private FilteredRecentChange createRecentChange(final Integer ns, final String title) {
    return new FilteredRecentChange(
        "comment",
        0,
        URI.create("https://diff"),
        Mockito.mock(RecentChangesFilter.class),
        ns,
        URI.create("https://page"),
        1,
        2,
        3,
        List.of(),
        null,
        title,
        "User");
  }
}
