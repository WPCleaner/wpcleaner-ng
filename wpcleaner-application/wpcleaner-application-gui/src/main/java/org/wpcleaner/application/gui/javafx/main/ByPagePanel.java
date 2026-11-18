package org.wpcleaner.application.gui.javafx.main;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.function.Consumer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import org.wpcleaner.api.utils.GT;
import org.wpcleaner.api.wiki.definition.WikiDefinition;
import org.wpcleaner.application.gui.javafx.JavaFxImageLoader;
import org.wpcleaner.application.gui.javafx.core.window.JavaFxWindow;
import org.wpcleaner.lib.image.ImageCollection;
import org.wpcleaner.lib.image.ImageSize;

final class ByPagePanel extends GridPane {

  static final String INTERNAL_LINKS_ID = "internal-links";
  static final String SEARCH_TITLES_ID = "search-titles";

  private final JavaFxImageLoader imageLoader;
  private final JavaFxWindow<?> owner;
  private final JavaFxMainWindowServices services;
  private final WikiDefinition wiki;

  ByPagePanel(
      final JavaFxWindow<?> owner,
      final WikiDefinition wiki,
      final JavaFxImageLoader imageLoader,
      final JavaFxMainWindowServices services) {
    this.imageLoader = imageLoader;
    this.owner = owner;
    this.services = services;
    this.wiki = wiki;
    initialize();
  }

  private void initialize() {
    setHgap(10);
    setVgap(8);
    setPadding(new Insets(10, 15, 10, 15));

    final ColumnConstraints colLabel = new ColumnConstraints();
    colLabel.setPrefWidth(40);
    colLabel.setMinWidth(40);
    colLabel.setHgrow(Priority.NEVER);

    final ColumnConstraints colIcon = new ColumnConstraints();
    colIcon.setPrefWidth(25);
    colIcon.setMinWidth(25);
    colIcon.setHgrow(Priority.NEVER);

    final ColumnConstraints colField = new ColumnConstraints();
    colField.setHgrow(Priority.ALWAYS);

    final ColumnConstraints colToolbar = new ColumnConstraints();
    colToolbar.setPrefWidth(70);
    colToolbar.setMinWidth(70);
    colToolbar.setHgrow(Priority.NEVER);

    getColumnConstraints().addAll(colLabel, colIcon, colField, colToolbar);

    final PageInput page =
        new PageInput(
            owner, wiki, services.interestingSettings(), imageLoader, services.apiRandom());
    setHgrow(page.comboBox, Priority.ALWAYS);

    final Button analysisButton = new Button(GT._T("Analysis"));
    analysisButton.setMaxWidth(Double.MAX_VALUE);
    imageLoader
        .getImageView(ImageCollection.ANALYSIS, ImageSize.BUTTON)
        .ifPresent(analysisButton::setGraphic);
    analysisButton.setOnAction(
        _ -> onPage(page, services.analysisWindowFactory()::displayAnalysisWindow));

    final Button searchTitlesButton = new Button(GT._T("Search in titles"));
    searchTitlesButton.setId(SEARCH_TITLES_ID);
    searchTitlesButton.setMaxWidth(Double.MAX_VALUE);
    imageLoader
        .getImageView(ImageCollection.SEARCH, ImageSize.BUTTON)
        .ifPresent(searchTitlesButton::setGraphic);
    searchTitlesButton.setOnAction(_ -> onPage(page, this::searchTitles));

    final MenuButton internalLinksButton = new MenuButton(GT._T("Internal links"));
    internalLinksButton.setId(INTERNAL_LINKS_ID);
    internalLinksButton.setMaxWidth(Double.MAX_VALUE);
    internalLinksButton.setAlignment(Pos.CENTER);
    imageLoader
        .getImageView(ImageCollection.INTERNAL_LINK, ImageSize.BUTTON)
        .ifPresent(internalLinksButton::setGraphic);
    internalLinksButton
        .getItems()
        .addAll(
            createInternalLinksItem(
                page, GT._T("Articles in main namespace"), InternalLinksMode.MAIN),
            createInternalLinksItem(
                page,
                GT._T("Articles associated to talk pages"),
                InternalLinksMode.TALK_PAGES_CONVERTED),
            createInternalLinksItem(page, GT._T("All articles"), InternalLinksMode.ALL));

    add(page.label, 0, 0);
    add(page.icon, 1, 0);
    add(page.comboBox, 2, 0);
    add(page.toolBar, 3, 0);
    add(analysisButton, 0, 1, 4, 1);
    add(searchTitlesButton, 0, 2, 4, 1);
    add(internalLinksButton, 0, 3, 4, 1);
  }

  private void onPage(final PageInput page, final Consumer<String> pageAction) {
    final String pageName = page.getPage();
    if (pageName.isEmpty()) {
      owner.showWarning(GT._T("Warning"), GT._T("The page name cannot be empty."));
    } else {
      pageAction.accept(pageName);
    }
  }

  private MenuItem createInternalLinksItem(
      final PageInput page, final String text, final InternalLinksMode mode) {
    final MenuItem item = new MenuItem(text);
    item.setOnAction(_ -> onPage(page, pageName -> listInternalLinks(pageName, mode)));
    return item;
  }

  private void listInternalLinks(final String pageName, final InternalLinksMode mode) {
    services
        .pageListWindowFactory()
        .displayPageListWindow(
            GT._T("Internal links in %s", pageName),
            new InternalLinksTask(
                services.apiLinks(),
                wiki,
                services.namespaceRepository().getNamespaces(wiki),
                pageName,
                mode),
            services.analysisWindowFactory()::displayAnalysisWindow);
  }

  private void searchTitles(final String pageName) {
    services
        .pageListWindowFactory()
        .displayPageListWindow(
            GT._T("Search results for %s", pageName),
            new SearchTitlesTask(
                services.apiSearch(),
                wiki,
                services.namespaceRepository().getNamespaces(wiki),
                pageName),
            services.analysisWindowFactory()::displayAnalysisWindow);
  }
}
