package org.wpcleaner.application.gui.javafx.core.control;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import org.wpcleaner.api.utils.GT;
import org.wpcleaner.application.base.utils.url.UrlService;
import org.wpcleaner.application.gui.javafx.JavaFxImageLoader;
import org.wpcleaner.application.gui.javafx.core.action.JavaFxActionServices;
import org.wpcleaner.lib.image.ImageCollection;
import org.wpcleaner.lib.image.ImageSize;

public final class FeedbacksToolBar extends ToolBar {

  private final JavaFxActionServices actionServices;
  private final JavaFxImageLoader imageLoader;
  private final UrlService urlService;

  public FeedbacksToolBar(
      final JavaFxActionServices actionServices,
      final JavaFxImageLoader imageLoader,
      final UrlService urlService) {
    super();
    this.actionServices = actionServices;
    this.imageLoader = imageLoader;
    this.urlService = urlService;
    internalInitialize();
  }

  private void internalInitialize() {
    setStyle("-fx-background-color: transparent; -fx-padding: 0; -fx-spacing: 1px;");

    final MenuButton feedbackButton = new MenuButton();
    feedbackButton.setTooltip(new Tooltip(GT._T("Feedback")));
    feedbackButton.setStyle(DefaultStyles.TOOLBAR_ELEMENT);
    imageLoader
        .getImageView(ImageCollection.HELP_FAQ, ImageSize.BUTTON)
        .ifPresent(feedbackButton::setGraphic);

    final MenuItem helpItem = new MenuItem(GT._T("Help"));
    imageLoader.getImageView(ImageCollection.HELP, ImageSize.MENU).ifPresent(helpItem::setGraphic);
    helpItem.setOnAction(_ -> actionServices.browse(UrlService.HELP));

    final MenuItem reportBugItem = new MenuItem(GT._T("Report bug"));
    imageLoader
        .getImageView(ImageCollection.LOGO_PHABRICATOR, ImageSize.MENU)
        .ifPresent(reportBugItem::setGraphic);
    reportBugItem.setOnAction(_ -> actionServices.browse(urlService.reportBug()));

    final MenuItem requestFeatureItem = new MenuItem(GT._T("Request new feature"));
    imageLoader
        .getImageView(ImageCollection.LOGO_PHABRICATOR, ImageSize.MENU)
        .ifPresent(requestFeatureItem::setGraphic);
    requestFeatureItem.setOnAction(_ -> actionServices.browse(UrlService.REQUEST_FEATURE));

    final MenuItem askQuestionItem = new MenuItem(GT._T("Ask a question"));
    imageLoader
        .getImageView(ImageCollection.HELP, ImageSize.MENU)
        .ifPresent(askQuestionItem::setGraphic);
    askQuestionItem.setOnAction(_ -> actionServices.browse(UrlService.ASK_QUESTION));

    feedbackButton.getItems().addAll(helpItem, reportBugItem, requestFeatureItem, askQuestionItem);

    final MenuButton optionsButton = new MenuButton();
    optionsButton.setTooltip(new Tooltip(GT._T("Options")));
    optionsButton.setStyle(DefaultStyles.TOOLBAR_ELEMENT);
    imageLoader
        .getImageView(ImageCollection.OPTIONS, ImageSize.BUTTON)
        .ifPresent(optionsButton::setGraphic);

    final MenuItem savePosItem = new MenuItem(GT._T("Save windows position"));
    imageLoader
        .getImageView(ImageCollection.DOCUMENT_SAVE, ImageSize.MENU)
        .ifPresent(savePosItem::setGraphic);
    savePosItem.setOnAction(_ -> actionServices.saveWindowsPosition().execute());

    optionsButton.getItems().add(savePosItem);

    final Button aboutButton = new Button();
    aboutButton.setTooltip(new Tooltip("About"));
    aboutButton.setStyle(DefaultStyles.TOOLBAR_ELEMENT);
    imageLoader
        .getImageView(ImageCollection.HELP_ABOUT, ImageSize.BUTTON)
        .ifPresent(aboutButton::setGraphic);
    aboutButton.setOnAction(
        _ -> {
          final Alert alert = new Alert(Alert.AlertType.INFORMATION);
          alert.setTitle(GT._T("About"));
          alert.setHeaderText("WPCleaner-NG");
          alert.setContentText(GT._T("WPCleaner Next Generation Prototype in JavaFX."));
          alert.showAndWait();
        });

    getItems().addAll(feedbackButton, optionsButton, aboutButton);
  }
}
