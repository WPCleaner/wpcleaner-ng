package org.wpcleaner.application.gui.javafx.recentchanges.userpagehosting;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.wpcleaner.application.gui.javafx.JavaFxTest;
import org.wpcleaner.application.gui.javafx.JavaFxWindowsRegistry;
import org.wpcleaner.application.gui.javafx.core.action.JavaFxActionServices;
import org.wpcleaner.application.gui.javafx.recentchanges.JavaFxRecentChangesWindowServices;
import org.wpcleaner.lib.image.ImageLoader;

class UserPageHostingConfigWindowTest extends JavaFxTest {

  @DisplayName("UserPageHostingConfigWindow initializes controls with existing configuration")
  @Test
  void testUserPageHostingConfigWindowInitializationWithConfig()
      throws InterruptedException, ExecutionException, TimeoutException {
    runOnJavaFx(
        () -> {
          final JavaFxRecentChangesWindowServices services =
              Mockito.mock(JavaFxRecentChangesWindowServices.class);
          final ImageLoader imageLoader = Mockito.mock(ImageLoader.class);
          final JavaFxActionServices actionServices = Mockito.mock(JavaFxActionServices.class);
          final JavaFxWindowsRegistry windowsRegistry = Mockito.mock(JavaFxWindowsRegistry.class);

          Mockito.when(services.imageLoader()).thenReturn(imageLoader);
          Mockito.when(services.actionServices()).thenReturn(actionServices);
          Mockito.when(services.windowsRegistry()).thenReturn(windowsRegistry);

          final Stage ownerStage = new Stage();
          final UserPageHostingConfig initialConfig =
              new UserPageHostingConfig(
                  "userPageSummary",
                  "userPageText",
                  "userTalkPageSummary",
                  List.of(new UserTalkPageTextConfig("label1", "text1", true)));

          final AtomicReference<UserPageHostingConfig> validated = new AtomicReference<>();
          final UserPageHostingConfigWindow window =
              new UserPageHostingConfigWindow(services, ownerStage, initialConfig, validated::set);

          Assertions.assertThat(window.getStage().getTitle())
              .isEqualTo("Configure User Page Hosting Prevention");
          Assertions.assertThat(window.getName()).isEqualTo("userPageHostingConfig");
          Assertions.assertThat(window.getStage().getScene()).isNotNull();
          Assertions.assertThat(window.getStage().isShowing()).isTrue();
          Assertions.assertThat(window.getStage().getModality()).isEqualTo(Modality.WINDOW_MODAL);
          Assertions.assertThat(window.getStage().getOwner()).isEqualTo(ownerStage);

          window.getStage().close();
          ownerStage.close();
        });
  }

  @DisplayName("UserPageHostingConfigWindow initializes controls with null configuration")
  @Test
  void testUserPageHostingConfigWindowInitializationWithoutConfig()
      throws InterruptedException, ExecutionException, TimeoutException {
    runOnJavaFx(
        () -> {
          final JavaFxRecentChangesWindowServices services =
              Mockito.mock(JavaFxRecentChangesWindowServices.class);
          final ImageLoader imageLoader = Mockito.mock(ImageLoader.class);
          final JavaFxActionServices actionServices = Mockito.mock(JavaFxActionServices.class);
          final JavaFxWindowsRegistry windowsRegistry = Mockito.mock(JavaFxWindowsRegistry.class);

          Mockito.when(services.imageLoader()).thenReturn(imageLoader);
          Mockito.when(services.actionServices()).thenReturn(actionServices);
          Mockito.when(services.windowsRegistry()).thenReturn(windowsRegistry);

          final Stage ownerStage = new Stage();
          final UserPageHostingConfigWindow window =
              new UserPageHostingConfigWindow(services, ownerStage, null, _ -> {});

          Assertions.assertThat(window.getStage().getTitle())
              .isEqualTo("Configure User Page Hosting Prevention");
          Assertions.assertThat(window.getName()).isEqualTo("userPageHostingConfig");
          Assertions.assertThat(window.getStage().getScene()).isNotNull();
          Assertions.assertThat(window.getStage().isShowing()).isTrue();

          window.getStage().close();
          ownerStage.close();
        });
  }
}
