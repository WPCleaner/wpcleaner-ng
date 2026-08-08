/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wpcleaner.application.gui.javafx.login;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import org.jspecify.annotations.Nullable;
import org.wpcleaner.api.language.Language;
import org.wpcleaner.api.utils.GT;
import org.wpcleaner.application.gui.javafx.JavaFxImageLoader;
import org.wpcleaner.application.gui.javafx.core.action.JavaFxActionServices;
import org.wpcleaner.application.gui.javafx.core.control.DefaultStyles;
import org.wpcleaner.lib.image.ImageCollection;
import org.wpcleaner.lib.image.ImageSize;

final class LanguageInput {

  final ComboBox<@Nullable Language> comboBox;
  final ImageView icon;
  final Label label;
  final ToolBar toolBar;

  LanguageInput(final JavaFxImageLoader imageLoader, final JavaFxActionServices actionServices) {
    icon =
        imageLoader
            .getImageView(ImageCollection.LANGUAGE, ImageSize.LABEL)
            .orElseGet(ImageView::new);

    comboBox = new ComboBox<>();
    comboBox.getItems().addAll(Language.values());
    comboBox.getSelectionModel().select(Language.DEFAULT);
    comboBox.setMaxWidth(Double.MAX_VALUE);

    comboBox.setCellFactory(_ -> new LanguageListCell(imageLoader));
    comboBox.setButtonCell(new LanguageListCell(imageLoader));

    label = new Label(GT._T("Language"));
    label.setMaxWidth(Double.MAX_VALUE);
    label.setAlignment(Pos.CENTER_RIGHT);

    final Button addLanguageButton = new Button();
    addLanguageButton.setStyle(DefaultStyles.TOOLBAR_ELEMENT);
    imageLoader
        .getImageView(ImageCollection.LANGUAGE_ADD, ImageSize.TOOLBAR)
        .ifPresent(addLanguageButton::setGraphic);
    addLanguageButton.setTooltip(new Tooltip(GT._T("Add language")));
    addLanguageButton.setOnAction(
        _ -> actionServices.browse("https://translatewiki.net/wiki/Translating:WPCleaner"));

    toolBar = new ToolBar();
    toolBar.setStyle("-fx-background-color: transparent; -fx-padding: 0; -fx-spacing: 1px;");
    toolBar.getItems().add(addLanguageButton);
  }
}
