package org.wpcleaner.application.gui.javafx.recentchanges;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import org.assertj.core.api.Assertions;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.wpcleaner.api.api.CurrentUserService;
import org.wpcleaner.api.repository.namespace.CommonNamespaces;
import org.wpcleaner.api.settings.SettingsPersistence;
import org.wpcleaner.api.wiki.definition.WikiDefinition;
import org.wpcleaner.api.wiki.definition.WikipediaDefinitions;
import org.wpcleaner.application.gui.GuiLogin;
import org.wpcleaner.application.gui.GuiSpringBootTest;
import org.wpcleaner.application.gui.javafx.JavaFxTest;
import org.wpcleaner.application.gui.javafx.JavaFxWindowsRegistry;
import org.wpcleaner.application.gui.javafx.core.control.DefaultStyles;
import org.wpcleaner.application.gui.javafx.core.window.EmptyWindow;
import org.wpcleaner.application.gui.javafx.core.window.JavaFxWindow;
import org.wpcleaner.application.gui.javafx.recentchanges.userpagehosting.UserPageHostingAction;
import org.wpcleaner.lib.image.ImageCollection;

@GuiSpringBootTest
class RecentChangesConfigurableActionButtonTest extends JavaFxTest {

  private static final WikiDefinition WIKI = WikipediaDefinitions.EN;

  @Autowired private JavaFxRecentChangesWindowServices services;
  @Autowired private CurrentUserService currentUserService;
  @Autowired private GuiLogin guiLogin;
  @Autowired private JavaFxWindowsRegistry windowsRegistry;

  @BeforeEach
  void setUp() throws IOException {
    guiLogin.login(WIKI, "Bot", GuiLogin.USER_NAMESPACES);
    Files.deleteIfExists(
        SettingsPersistence.getFolder().resolve(WIKI.code()).resolve("rc-userpage-hosting.json"));
  }

  @AfterEach
  void tearDown() throws InterruptedException, ExecutionException, TimeoutException {
    runOnJavaFx(windowsRegistry::closeAllWindows);
    currentUserService.logout();
  }

  @DisplayName("RecentChangesConfigurableActionButton reflects state and handles events")
  @Test
  void actionButtonStateAndEvents()
      throws InterruptedException, ExecutionException, TimeoutException {
    final ObjectProperty<@Nullable FilteredRecentChange> currentRecentChange =
        new SimpleObjectProperty<>();
    final RecentChangesConfigurableActionButton button =
        callOnJavaFx(
            () ->
                new RecentChangesConfigurableActionButton(
                    services.imageLoader(),
                    ImageCollection.MOVE_PAGE,
                    "Custom Tooltip",
                    new UserPageHostingAction(services, new EmptyWindow<>(services)),
                    currentRecentChange));

    runOnJavaFx(
        () -> {
          Assertions.assertThat(button.getStyle()).isEqualTo(DefaultStyles.TOOLBAR_ELEMENT);
          Assertions.assertThat(button.getGraphic()).isInstanceOf(ImageView.class);
          Assertions.assertThat(button.getTooltip().getText()).isEqualTo("Custom Tooltip");
          Assertions.assertThat(button.isDisable()).isTrue();

          currentRecentChange.set(
              FilteredRecentChangeFixtures.create(CommonNamespaces.USER.id, "User:NicoV"));
          Assertions.assertThat(button.isDisable()).isFalse();

          button.fire();
        });
    assertConfigurationWindowOpened();

    runOnJavaFx(() -> button.getOnMouseClicked().handle(createClick(MouseButton.SECONDARY)));
    assertConfigurationWindowOpened();

    runOnJavaFx(
        () -> {
          currentRecentChange.set(
              FilteredRecentChangeFixtures.create(CommonNamespaces.MAIN.id, "Main Page"));
          Assertions.assertThat(button.isDisable()).isTrue();

          currentRecentChange.set(null);
          Assertions.assertThat(button.isDisable()).isTrue();
        });
  }

  private void assertConfigurationWindowOpened()
      throws InterruptedException, ExecutionException, TimeoutException {
    final JavaFxWindow<?> window =
        waitForJavaFx(
            () ->
                windowsRegistry.getVisibleWindows().stream()
                    .filter(visible -> "userPageHostingConfig".equals(visible.getName()))
                    .findFirst());
    runOnJavaFx(() -> window.getStage().close());
  }

  private static MouseEvent createClick(final MouseButton mouseButton) {
    return new MouseEvent(
        MouseEvent.MOUSE_CLICKED,
        0,
        0,
        0,
        0,
        mouseButton,
        1,
        false,
        false,
        false,
        false,
        false,
        false,
        false,
        false,
        false,
        false,
        null);
  }
}
