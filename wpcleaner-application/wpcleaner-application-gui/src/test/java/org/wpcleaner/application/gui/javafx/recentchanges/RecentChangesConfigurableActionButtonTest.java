package org.wpcleaner.application.gui.javafx.recentchanges;

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
import org.wpcleaner.application.gui.javafx.recentchanges.options.RecentChangesFilter;
import org.wpcleaner.lib.image.ImageCollection;
import org.wpcleaner.lib.image.ImageLoader;

class RecentChangesConfigurableActionButtonTest extends JavaFxTest {

  @DisplayName("RecentChangesConfigurableActionButton reflects state and handles events")
  @Test
  void testActionButtonStateAndEvents()
      throws InterruptedException, ExecutionException, TimeoutException {
    runOnJavaFx(
        () -> {
          final ImageLoader imageLoader = Mockito.mock(ImageLoader.class);
          final RecentChangesConfigurableAction action =
              Mockito.mock(RecentChangesConfigurableAction.class);
          final ObjectProperty<FilteredRecentChange> currentRecentChange =
              new SimpleObjectProperty<>();

          final RecentChangesConfigurableActionButton button =
              new RecentChangesConfigurableActionButton(
                  imageLoader,
                  ImageCollection.MOVE_PAGE,
                  "Custom Tooltip",
                  action,
                  currentRecentChange);

          Assertions.assertThat(button.getStyle()).isEqualTo(DefaultStyles.TOOLBAR_ELEMENT);
          Assertions.assertThat(button.getTooltip()).isNotNull();
          Assertions.assertThat(button.getTooltip().getText()).isEqualTo("Custom Tooltip");
          Assertions.assertThat(button.isDisable()).isTrue();

          final FilteredRecentChange userPage =
              createRecentChange(CommonNamespaces.USER.id, "User:NicoV");
          Mockito.when(action.canApply(userPage)).thenReturn(true);

          currentRecentChange.set(userPage);
          Assertions.assertThat(button.isDisable()).isFalse();

          button.fire();
          Mockito.verify(action, Mockito.times(1)).apply(userPage);

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
          button.getOnMouseClicked().handle(rightClickEvent);
          Mockito.verify(action, Mockito.times(1)).configure();

          final FilteredRecentChange mainPage =
              createRecentChange(CommonNamespaces.MAIN.id, "Main Page");
          Mockito.when(action.canApply(mainPage)).thenReturn(false);

          currentRecentChange.set(mainPage);
          Assertions.assertThat(button.isDisable()).isTrue();

          currentRecentChange.set(null);
          Assertions.assertThat(button.isDisable()).isTrue();
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
