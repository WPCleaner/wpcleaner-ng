package org.wpcleaner.application.gui.javafx;

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
import org.wpcleaner.application.base.utils.url.UrlService;
import org.wpcleaner.application.gui.core.desktop.DesktopService;
import org.wpcleaner.application.gui.swing.core.window.SaveWindowsPositionAction;
import org.wpcleaner.lib.image.ImageCollection;
import org.wpcleaner.lib.image.ImageSize;

public final class FeedbacksToolBar extends ToolBar {

  private final JavaFxImageLoader imageLoader;
  private final DesktopService desktopService;
  private final UrlService urlService;
  private final SaveWindowsPositionAction saveWindowsPositionAction;

  public FeedbacksToolBar(
      final JavaFxImageLoader imageLoader,
      final DesktopService desktopService,
      final UrlService urlService,
      final SaveWindowsPositionAction saveWindowsPositionAction) {
    super();
    this.imageLoader = imageLoader;
    this.desktopService = desktopService;
    this.urlService = urlService;
    this.saveWindowsPositionAction = saveWindowsPositionAction;
    initialize();
  }

  private void initialize() {
    setStyle("-fx-background-color: transparent; -fx-padding: 0; -fx-spacing: 1px;");

    final MenuButton feedbackButton = new MenuButton();
    feedbackButton.setTooltip(new Tooltip("Feedback"));
    feedbackButton.setStyle("-fx-background-color: transparent; -fx-padding: 1px;");
    imageLoader
        .getImageView(ImageCollection.HELP_FAQ, ImageSize.BUTTON)
        .ifPresent(feedbackButton::setGraphic);

    final MenuItem helpItem = new MenuItem("Help");
    imageLoader.getImageView(ImageCollection.HELP, ImageSize.MENU).ifPresent(helpItem::setGraphic);
    helpItem.setOnAction(_ -> JavaFxInitializer.browse(desktopService, UrlService.HELP));

    final MenuItem reportBugItem = new MenuItem("Report bug");
    imageLoader
        .getImageView(ImageCollection.LOGO_PHABRICATOR, ImageSize.MENU)
        .ifPresent(reportBugItem::setGraphic);
    reportBugItem.setOnAction(
        _ -> JavaFxInitializer.browse(desktopService, urlService.reportBug()));

    final MenuItem requestFeatureItem = new MenuItem("Request new feature");
    imageLoader
        .getImageView(ImageCollection.LOGO_PHABRICATOR, ImageSize.MENU)
        .ifPresent(requestFeatureItem::setGraphic);
    requestFeatureItem.setOnAction(
        _ -> JavaFxInitializer.browse(desktopService, UrlService.REQUEST_FEATURE));

    final MenuItem askQuestionItem = new MenuItem("Ask a question");
    imageLoader
        .getImageView(ImageCollection.HELP, ImageSize.MENU)
        .ifPresent(askQuestionItem::setGraphic);
    askQuestionItem.setOnAction(
        _ -> JavaFxInitializer.browse(desktopService, UrlService.ASK_QUESTION));

    feedbackButton.getItems().addAll(helpItem, reportBugItem, requestFeatureItem, askQuestionItem);

    final MenuButton optionsButton = new MenuButton();
    optionsButton.setTooltip(new Tooltip("Options"));
    optionsButton.setStyle("-fx-background-color: transparent; -fx-padding: 1px;");
    imageLoader
        .getImageView(ImageCollection.OPTIONS, ImageSize.BUTTON)
        .ifPresent(optionsButton::setGraphic);

    final MenuItem savePosItem = new MenuItem("Save windows position");
    imageLoader
        .getImageView(ImageCollection.DOCUMENT_SAVE, ImageSize.MENU)
        .ifPresent(savePosItem::setGraphic);
    savePosItem.setOnAction(_ -> saveWindowsPositionAction.execute());

    optionsButton.getItems().add(savePosItem);

    final Button aboutButton = new Button();
    aboutButton.setTooltip(new Tooltip("About"));
    aboutButton.setStyle("-fx-background-color: transparent; -fx-padding: 1px;");
    imageLoader
        .getImageView(ImageCollection.HELP_ABOUT, ImageSize.BUTTON)
        .ifPresent(aboutButton::setGraphic);
    aboutButton.setOnAction(
        _ -> {
          final Alert alert = new Alert(Alert.AlertType.INFORMATION);
          alert.setTitle("About");
          alert.setHeaderText("WPCleaner-NG");
          alert.setContentText("WPCleaner Next Generation Prototype in JavaFX.");
          alert.showAndWait();
        });

    getItems().addAll(feedbackButton, optionsButton, aboutButton);
  }
}
