package org.wpcleaner.application.gui.javafx.recentchanges.userpagehosting;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import javafx.beans.binding.Bindings;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseButton;
import org.jspecify.annotations.Nullable;
import org.wpcleaner.api.utils.GT;
import org.wpcleaner.application.gui.javafx.JavaFxImageLoader;
import org.wpcleaner.application.gui.javafx.core.control.DefaultStyles;
import org.wpcleaner.application.gui.javafx.core.window.JavaFxWindow;
import org.wpcleaner.application.gui.javafx.recentchanges.FilteredRecentChange;
import org.wpcleaner.application.gui.javafx.recentchanges.JavaFxRecentChangesWindowServices;
import org.wpcleaner.lib.image.ImageCollection;
import org.wpcleaner.lib.image.ImageSize;

public final class UserPageHostingButton extends Button {

  public UserPageHostingButton(
      final JavaFxRecentChangesWindowServices services,
      final JavaFxWindow<?> owner,
      final ObservableValue<@Nullable FilteredRecentChange> currentRecentChange) {
    this(services, new UserPageHostingAction(services, owner), currentRecentChange);
  }

  UserPageHostingButton(
      final JavaFxRecentChangesWindowServices services,
      final UserPageHostingAction action,
      final ObservableValue<@Nullable FilteredRecentChange> currentRecentChange) {
    super();
    final JavaFxImageLoader imageLoader = new JavaFxImageLoader(services.imageLoader());
    setStyle(DefaultStyles.TOOLBAR_ELEMENT);
    imageLoader.getImageView(ImageCollection.HOSTING, ImageSize.BUTTON).ifPresent(this::setGraphic);
    setTooltip(new Tooltip(GT._T("Prevent user page being used as hosting")));
    disableProperty()
        .bind(
            Bindings.createBooleanBinding(
                () -> {
                  final FilteredRecentChange rc = currentRecentChange.getValue();
                  return rc == null || !action.canApply(rc);
                },
                currentRecentChange));
    setOnAction(
        _ -> {
          final FilteredRecentChange rc = currentRecentChange.getValue();
          if (rc != null && action.canApply(rc)) {
            action.apply(rc);
          }
        });
    setOnMouseClicked(
        event -> {
          if (event.getButton() == MouseButton.SECONDARY) {
            action.configure();
          }
        });
  }
}
