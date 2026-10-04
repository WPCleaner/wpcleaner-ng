package org.wpcleaner.application.gui.javafx.recentchanges.userpagehosting;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.net.URI;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import javafx.stage.Stage;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.wpcleaner.api.api.ConnectedUser;
import org.wpcleaner.api.api.CurrentUserService;
import org.wpcleaner.api.repository.namespace.CommonNamespaces;
import org.wpcleaner.api.wiki.definition.WikiDefinition;
import org.wpcleaner.application.gui.javafx.JavaFxTest;
import org.wpcleaner.application.gui.javafx.JavaFxWindowsRegistry;
import org.wpcleaner.application.gui.javafx.core.action.JavaFxActionServices;
import org.wpcleaner.application.gui.javafx.core.window.JavaFxWindow;
import org.wpcleaner.application.gui.javafx.recentchanges.FilteredRecentChange;
import org.wpcleaner.application.gui.javafx.recentchanges.JavaFxRecentChangesWindowServices;
import org.wpcleaner.application.gui.javafx.recentchanges.RecentChangesAction;
import org.wpcleaner.application.gui.javafx.recentchanges.options.RecentChangesFilter;
import org.wpcleaner.lib.image.ImageLoader;

class UserPageHostingActionTest extends JavaFxTest {

  @DisplayName("canApply returns true only for user pages that are not subpages")
  @Test
  void testCanApply() {
    final JavaFxRecentChangesWindowServices services =
        Mockito.mock(JavaFxRecentChangesWindowServices.class);
    final JavaFxWindow<?> owner = Mockito.mock(JavaFxWindow.class);
    final RecentChangesAction action = new UserPageHostingAction(services, owner);

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

  @DisplayName("configure displays configuration window")
  @Test
  void testConfigureDisplaysConfigurationWindow()
      throws InterruptedException, ExecutionException, TimeoutException {
    runOnJavaFx(
        () -> {
          final JavaFxRecentChangesWindowServices services =
              Mockito.mock(JavaFxRecentChangesWindowServices.class);
          final ImageLoader imageLoader = Mockito.mock(ImageLoader.class);
          final JavaFxActionServices actionServices = Mockito.mock(JavaFxActionServices.class);
          final JavaFxWindowsRegistry windowsRegistry = Mockito.mock(JavaFxWindowsRegistry.class);
          final CurrentUserService currentUserService = Mockito.mock(CurrentUserService.class);
          final ConnectedUser connectedUser = Mockito.mock(ConnectedUser.class);
          final WikiDefinition wiki = Mockito.mock(WikiDefinition.class);

          Mockito.when(services.imageLoader()).thenReturn(imageLoader);
          Mockito.when(services.actionServices()).thenReturn(actionServices);
          Mockito.when(services.windowsRegistry()).thenReturn(windowsRegistry);
          Mockito.when(services.user()).thenReturn(currentUserService);
          Mockito.when(currentUserService.getCurrentUser()).thenReturn(connectedUser);
          Mockito.when(connectedUser.wiki()).thenReturn(wiki);
          Mockito.when(wiki.code()).thenReturn("enwiki");

          final JavaFxWindow<?> owner = Mockito.mock(JavaFxWindow.class);
          final Stage ownerStage = new Stage();
          Mockito.when(owner.getStage()).thenReturn(ownerStage);

          final UserPageHostingAction action = new UserPageHostingAction(services, owner);
          action.configure();

          final ArgumentCaptor<JavaFxWindow<?>> captor = ArgumentCaptor.captor();
          Mockito.verify(windowsRegistry).register(captor.capture());
          Assertions.assertThat(captor.getValue()).isInstanceOf(UserPageHostingConfigWindow.class);
          captor.getValue().getStage().close();
        });
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
