package org.wpcleaner.application.gui.javafx.recentchanges.userpagehosting;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.net.URI;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.wpcleaner.api.repository.namespace.CommonNamespaces;
import org.wpcleaner.application.gui.javafx.JavaFxTest;
import org.wpcleaner.application.gui.javafx.core.control.DefaultStyles;
import org.wpcleaner.application.gui.javafx.core.window.JavaFxWindow;
import org.wpcleaner.application.gui.javafx.recentchanges.FilteredRecentChange;
import org.wpcleaner.application.gui.javafx.recentchanges.JavaFxRecentChangesWindowServices;
import org.wpcleaner.application.gui.javafx.recentchanges.options.RecentChangesFilter;
import org.wpcleaner.lib.image.ImageLoader;

class UserPageHostingButtonTest extends JavaFxTest {

  @DisplayName("Button disable state reflects whether current recent change is applicable")
  @Test
  void testDisablingState() throws InterruptedException, ExecutionException, TimeoutException {
    runOnJavaFx(
        () -> {
          final JavaFxRecentChangesWindowServices services =
              Mockito.mock(JavaFxRecentChangesWindowServices.class);
          final ImageLoader imageLoader = Mockito.mock(ImageLoader.class);
          Mockito.when(services.imageLoader()).thenReturn(imageLoader);

          final UserPageHostingAction action = Mockito.mock(UserPageHostingAction.class);
          final ObjectProperty<FilteredRecentChange> currentRecentChange =
              new SimpleObjectProperty<>();

          final UserPageHostingButton button =
              new UserPageHostingButton(services, action, currentRecentChange);

          Assertions.assertThat(button.getStyle()).isEqualTo(DefaultStyles.TOOLBAR_ELEMENT);
          Assertions.assertThat(button.getTooltip()).isNotNull();
          Assertions.assertThat(button.getTooltip().getText())
              .isEqualTo("Prevent user page being used as hosting");
          Assertions.assertThat(button.isDisable()).isTrue();

          final FilteredRecentChange userPage =
              createRecentChange(CommonNamespaces.USER.id, "User:NicoV");
          Mockito.when(action.canApply(userPage)).thenReturn(true);

          currentRecentChange.set(userPage);
          Assertions.assertThat(button.isDisable()).isFalse();

          final FilteredRecentChange mainPage =
              createRecentChange(CommonNamespaces.MAIN.id, "Main Page");
          Mockito.when(action.canApply(mainPage)).thenReturn(false);

          currentRecentChange.set(mainPage);
          Assertions.assertThat(button.isDisable()).isTrue();

          currentRecentChange.set(null);
          Assertions.assertThat(button.isDisable()).isTrue();
        });
  }

  @DisplayName("Button click fires action apply when change can be applied")
  @Test
  void testButtonClickAppliesAction()
      throws InterruptedException, ExecutionException, TimeoutException {
    runOnJavaFx(
        () -> {
          final JavaFxRecentChangesWindowServices services =
              Mockito.mock(JavaFxRecentChangesWindowServices.class);
          final ImageLoader imageLoader = Mockito.mock(ImageLoader.class);
          Mockito.when(services.imageLoader()).thenReturn(imageLoader);

          final UserPageHostingAction action = Mockito.mock(UserPageHostingAction.class);
          final ObjectProperty<FilteredRecentChange> currentRecentChange =
              new SimpleObjectProperty<>();

          final UserPageHostingButton button =
              new UserPageHostingButton(services, action, currentRecentChange);

          final FilteredRecentChange userPage =
              createRecentChange(CommonNamespaces.USER.id, "User:NicoV");
          Mockito.when(action.canApply(userPage)).thenReturn(true);

          currentRecentChange.set(userPage);
          button.fire();

          Mockito.verify(action, Mockito.times(1)).apply(userPage);
        });
  }

  @DisplayName("Button click does not fire action when change cannot be applied or is null")
  @Test
  void testButtonClickDoesNotApplyWhenCannotApply()
      throws InterruptedException, ExecutionException, TimeoutException {
    runOnJavaFx(
        () -> {
          final JavaFxRecentChangesWindowServices services =
              Mockito.mock(JavaFxRecentChangesWindowServices.class);
          final ImageLoader imageLoader = Mockito.mock(ImageLoader.class);
          Mockito.when(services.imageLoader()).thenReturn(imageLoader);

          final UserPageHostingAction action = Mockito.mock(UserPageHostingAction.class);
          final ObjectProperty<FilteredRecentChange> currentRecentChange =
              new SimpleObjectProperty<>();

          final UserPageHostingButton button =
              new UserPageHostingButton(services, action, currentRecentChange);

          button.fire();
          Mockito.verify(action, Mockito.never()).apply(Mockito.any());

          final FilteredRecentChange mainPage =
              createRecentChange(CommonNamespaces.MAIN.id, "Main Page");
          Mockito.when(action.canApply(mainPage)).thenReturn(false);

          currentRecentChange.set(mainPage);
          button.fire();
          Mockito.verify(action, Mockito.never()).apply(Mockito.any());
        });
  }

  @DisplayName("Constructors with owner and services create UserPageHostingAction properly")
  @Test
  void testPublicConstructors() throws InterruptedException, ExecutionException, TimeoutException {
    runOnJavaFx(
        () -> {
          final JavaFxRecentChangesWindowServices services =
              Mockito.mock(JavaFxRecentChangesWindowServices.class);
          final JavaFxWindow<?> owner = Mockito.mock(JavaFxWindow.class);
          final ImageLoader imageLoader = Mockito.mock(ImageLoader.class);
          Mockito.when(services.imageLoader()).thenReturn(imageLoader);

          final ObjectProperty<FilteredRecentChange> currentRecentChange =
              new SimpleObjectProperty<>();

          final UserPageHostingButton button1 =
              new UserPageHostingButton(services, owner, currentRecentChange);
          final UserPageHostingButton button2 =
              new UserPageHostingButton(services, owner, currentRecentChange);

          Assertions.assertThat(button1.isDisable()).isTrue();
          Assertions.assertThat(button2.isDisable()).isTrue();

          final FilteredRecentChange userPage =
              createRecentChange(CommonNamespaces.USER.id, "User:NicoV");
          currentRecentChange.set(userPage);

          Assertions.assertThat(button1.isDisable()).isFalse();
          Assertions.assertThat(button2.isDisable()).isFalse();

          final FilteredRecentChange subPage =
              createRecentChange(CommonNamespaces.USER.id, "User:NicoV/sandbox");
          currentRecentChange.set(subPage);

          Assertions.assertThat(button1.isDisable()).isTrue();
          Assertions.assertThat(button2.isDisable()).isTrue();
        });
  }

  @DisplayName("Button right click calls action configure")
  @Test
  void testButtonRightClickCallsConfigure()
      throws InterruptedException, ExecutionException, TimeoutException {
    runOnJavaFx(
        () -> {
          final JavaFxRecentChangesWindowServices services =
              Mockito.mock(JavaFxRecentChangesWindowServices.class);
          final ImageLoader imageLoader = Mockito.mock(ImageLoader.class);
          Mockito.when(services.imageLoader()).thenReturn(imageLoader);

          final UserPageHostingAction action = Mockito.mock(UserPageHostingAction.class);
          final ObjectProperty<FilteredRecentChange> currentRecentChange =
              new SimpleObjectProperty<>();

          final UserPageHostingButton button =
              new UserPageHostingButton(services, action, currentRecentChange);

          final MouseEvent rightClickEvent =
              new MouseEvent(
                  MouseEvent.MOUSE_CLICKED,
                  0,
                  0,
                  0,
                  0,
                  MouseButton.SECONDARY,
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
          button.fireEvent(rightClickEvent);

          Mockito.verify(action, Mockito.times(1)).configure();
          Mockito.verify(action, Mockito.never()).apply(Mockito.any());
        });
  }

  @DisplayName("Button left click on mouse clicked does not call configure")
  @Test
  void testButtonLeftClickDoesNotCallConfigure()
      throws InterruptedException, ExecutionException, TimeoutException {
    runOnJavaFx(
        () -> {
          final JavaFxRecentChangesWindowServices services =
              Mockito.mock(JavaFxRecentChangesWindowServices.class);
          final ImageLoader imageLoader = Mockito.mock(ImageLoader.class);
          Mockito.when(services.imageLoader()).thenReturn(imageLoader);

          final UserPageHostingAction action = Mockito.mock(UserPageHostingAction.class);
          final ObjectProperty<FilteredRecentChange> currentRecentChange =
              new SimpleObjectProperty<>();

          final UserPageHostingButton button =
              new UserPageHostingButton(services, action, currentRecentChange);

          final MouseEvent leftClickEvent =
              new MouseEvent(
                  MouseEvent.MOUSE_CLICKED,
                  0,
                  0,
                  0,
                  0,
                  MouseButton.PRIMARY,
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
          button.fireEvent(leftClickEvent);

          Mockito.verify(action, Mockito.never()).configure();
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
