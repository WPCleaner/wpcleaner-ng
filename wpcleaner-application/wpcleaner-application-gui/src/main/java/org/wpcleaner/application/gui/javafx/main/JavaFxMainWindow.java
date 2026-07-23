package org.wpcleaner.application.gui.javafx.main;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.wpcleaner.api.api.ConnectedUser;
import org.wpcleaner.api.utils.StringUtils;
import org.wpcleaner.application.gui.javafx.FeedbacksToolBar;
import org.wpcleaner.application.gui.javafx.JavaFxImageLoader;
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

    final StackPane root = new StackPane();
    final VBox mainContainer = new VBox(15);
    mainContainer.setPadding(new Insets(10, 15, 10, 15));
    mainContainer.setAlignment(Pos.CENTER);

    final VBox welcomeContainer = new VBox(5);
    welcomeContainer.setAlignment(Pos.CENTER);

    final Label welcomeLabel =
        new Label("Welcome %s on WPCleaner Next Generation!".formatted(user.username()));
    welcomeLabel.setAlignment(Pos.CENTER);

    final Label connectedLabel =
        new Label("You are currently connected to %s".formatted(user.wiki()));
    connectedLabel.setAlignment(Pos.CENTER);

    welcomeContainer.getChildren().addAll(welcomeLabel, connectedLabel);

    if (user.demo()) {
      final Label demoLabel =
          new Label("You are currently in demo mode, you won't be able to save your modifications");
      demoLabel.setAlignment(Pos.CENTER);
      welcomeContainer.getChildren().add(demoLabel);
    }

    final Label groupsLabel =
        new Label("Your groups: %s".formatted(StringUtils.joinWithEllipsis(user.groups(), 3)));
    groupsLabel.setAlignment(Pos.CENTER);

    final Label rightsLabel =
        new Label("Your rights: %s".formatted(StringUtils.joinWithEllipsis(user.rights(), 3)));
    rightsLabel.setAlignment(Pos.CENTER);

    welcomeContainer.getChildren().addAll(groupsLabel, rightsLabel);

    final TabPane tabPane = new TabPane();
    tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
    VBox.setVgrow(tabPane, Priority.ALWAYS);

    final Tab byPageTab = new Tab("By page");
    imageLoader
        .getImageView(ImageCollection.PAGE, ImageSize.BUTTON)
        .ifPresent(byPageTab::setGraphic);
    byPageTab.setContent(new ByPagePanel(user.wiki(), services.interestingSettings(), imageLoader));

    final Tab projectsTab = new Tab("Projects");
    projectsTab.setContent(new ProjectsPanel(services));

    tabPane.getTabs().addAll(byPageTab, projectsTab);

    final ToolBar feedbacks = createFeedbacksToolbar();

    mainContainer.getChildren().addAll(welcomeContainer, tabPane, feedbacks);
    root.getChildren().add(mainContainer);

    final Scene scene = new Scene(root, 650, 450);
    setScene(scene);
    sizeToScene();
  }

  private ToolBar createFeedbacksToolbar() {
    return new FeedbacksToolBar(
        imageLoader,
        services.desktopService(),
        services.urlService(),
        services.saveWindowsPositionAction());
  }
}
