package org.wpcleaner.application.gui.javafx.main;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.wpcleaner.api.api.ConnectedUser;
import org.wpcleaner.api.utils.GT;
import org.wpcleaner.application.gui.javafx.JavaFxImageLoader;
import org.wpcleaner.application.gui.javafx.core.control.FeedbacksToolBar;
import org.wpcleaner.lib.image.ImageCollection;
import org.wpcleaner.lib.image.ImageSize;

public final class JavaFxMainWindow extends Stage {

  private final JavaFxMainWindowServices services;
  private final JavaFxImageLoader imageLoader;
  private final ConnectedUser user;

  public JavaFxMainWindow(final JavaFxMainWindowServices services) {
    super();
    this.services = services;
    this.imageLoader = new JavaFxImageLoader(services.imageLoader());
    this.user = services.user().getCurrentUser();
    initialize();
  }

  private void initialize() {
    setTitle("WPCleaner");
    imageLoader.setWindowIcon(this);
    services.windowsRegistry().register(this);

    final StackPane root = new StackPane();
    final VBox mainContainer = new VBox(15);
    mainContainer.setPadding(new Insets(10, 15, 10, 15));
    mainContainer.setAlignment(Pos.CENTER);

    final VBox welcomeContainer = new VBox(5);
    welcomeContainer.setAlignment(Pos.CENTER);

    final Label welcomeLabel =
        new Label(GT._T("Welcome %s on WPCleaner Next Generation!", user.username()));
    welcomeLabel.setAlignment(Pos.CENTER);

    welcomeContainer.getChildren().add(welcomeLabel);

    if (user.demo()) {
      final Label demoLabel =
          new Label(
              GT._T(
                  "You are currently in demo mode, you won't be able to save your modifications"));
      demoLabel.setAlignment(Pos.CENTER);
      welcomeContainer.getChildren().add(demoLabel);
    }

    final TabPane tabPane = new TabPane();
    tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
    VBox.setVgrow(tabPane, Priority.ALWAYS);

    final Tab byPageTab = new Tab(GT._T("By page"));
    imageLoader
        .getImageView(ImageCollection.PAGE, ImageSize.BUTTON)
        .ifPresent(byPageTab::setGraphic);
    byPageTab.setContent(
        new ByPagePanel(
            user.wiki(), services.interestingSettings(), imageLoader, services.actionServices()));

    final Tab projectsTab = new Tab(GT._T("Projects"));
    projectsTab.setContent(new ProjectsPanel(services));

    tabPane.getTabs().addAll(byPageTab, projectsTab);

    final ToolBar feedbacks = createFeedbacksToolbar();

    mainContainer.getChildren().addAll(welcomeContainer, tabPane, feedbacks);
    root.getChildren().add(mainContainer);

    final Scene scene = new Scene(root, 650, 450);
    setScene(scene);
    services.actionServices().positionWindow(this, "main");
  }

  private ToolBar createFeedbacksToolbar() {
    final FeedbacksToolBar feedbacks =
        new FeedbacksToolBar(services.actionServices(), imageLoader, services.urlService());

    final Button userButton = new Button();
    userButton.setTooltip(new Tooltip(GT._T("User information")));
    userButton.setStyle("-fx-background-color: transparent; -fx-padding: 1px;");
    imageLoader
        .getImageView(ImageCollection.USER, ImageSize.BUTTON)
        .ifPresent(userButton::setGraphic);
    userButton.setOnAction(_ -> new UserInformationDialog(this, user).showAndWait());

    feedbacks.getItems().add(userButton);
    return feedbacks;
  }
}
