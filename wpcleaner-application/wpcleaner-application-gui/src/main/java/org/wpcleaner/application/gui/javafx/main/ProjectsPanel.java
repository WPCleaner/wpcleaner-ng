package org.wpcleaner.application.gui.javafx.main;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.VBox;
import org.wpcleaner.api.progress.LongRunningTask;
import org.wpcleaner.api.progress.ProgressStep;
import org.wpcleaner.api.progress.ProgressTracker;
import org.wpcleaner.api.utils.GT;
import org.wpcleaner.api.wiki.definition.WikiDefinition;
import org.wpcleaner.application.gui.javafx.JavaFxImageLoader;
import org.wpcleaner.lib.check.CheckAlgorithm;
import org.wpcleaner.lib.check.server.CheckWikiServer;
import org.wpcleaner.lib.image.ImageCollection;
import org.wpcleaner.lib.image.ImageSize;

final class ProjectsPanel extends VBox {

  static final String CHECK_WIKI_ID = "check-wiki";

  private static final int MAX_CHECK_WIKI_PAGES = 100;

  private final JavaFxMainWindowServices services;
  private final JavaFxImageLoader imageLoader;

  ProjectsPanel(final JavaFxMainWindowServices services) {
    super(10);
    this.services = services;
    this.imageLoader = new JavaFxImageLoader(services.imageLoader());
    initialize();
  }

  private void initialize() {
    setPadding(new Insets(10, 15, 10, 15));
    setAlignment(Pos.TOP_LEFT);

    final Button button = new Button(GT._T("Recent changes"));
    button.setMaxWidth(Double.MAX_VALUE);
    imageLoader
        .getImageView(ImageCollection.RECENT_CHANGES, ImageSize.BUTTON)
        .ifPresent(button::setGraphic);
    button.setOnAction(_ -> services.recentChangesWindowFactory().displayRecentChangesWindow());

    getChildren().addAll(button, createCheckWikiButton());
  }

  private MenuButton createCheckWikiButton() {
    final MenuButton button = new MenuButton(GT._T("Check Wiki"));
    button.setId(CHECK_WIKI_ID);
    button.setMaxWidth(Double.MAX_VALUE);
    button.setAlignment(Pos.CENTER);
    imageLoader
        .getImageView(ImageCollection.LOGO_WPCLEANER, ImageSize.BUTTON)
        .ifPresent(button::setGraphic);
    final WikiDefinition wiki = services.user().getCurrentUser().wiki();
    services.checkAlgorithmRegistry().getActiveAlgorithms(wiki).stream()
        .map(algorithm -> createCheckWikiItem(wiki, algorithm))
        .forEach(button.getItems()::add);
    button.setDisable(button.getItems().isEmpty());
    return button;
  }

  private MenuItem createCheckWikiItem(final WikiDefinition wiki, final CheckAlgorithm algorithm) {
    final String name = services.checkAlgorithmRegistry().getName(wiki, algorithm);
    final MenuItem item = new MenuItem("%03d - %s".formatted(algorithm.number(), name));
    item.setOnAction(
        _ ->
            services
                .pageListWindowFactory()
                .displayPageListWindow(
                    GT._T("Check Wiki: %s", name),
                    new CheckWikiPagesTask(services.checkWikiServer(), wiki, algorithm, name),
                    page ->
                        services
                            .analysisWindowFactory()
                            .displayAnalysisWindow(page, algorithm.number())));
    return item;
  }

  private record CheckWikiPagesTask(
      CheckWikiServer checkWikiServer, WikiDefinition wiki, CheckAlgorithm algorithm, String name)
      implements LongRunningTask<List<String>> {

    @Override
    public List<String> call(final ProgressTracker tracker) {
      try (ProgressStep _ = tracker.start(GT._T("Retrieving pages for %s", name))) {
        return checkWikiServer.retrievePages(wiki, algorithm.number(), MAX_CHECK_WIKI_PAGES);
      }
    }
  }
}
