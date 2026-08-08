package org.wpcleaner.application.gui.javafx.main;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import java.util.Set;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import org.wpcleaner.api.api.Limit;
import org.wpcleaner.api.api.query.list.random.ApiRandom;
import org.wpcleaner.api.api.query.list.random.RandomPage;
import org.wpcleaner.api.api.query.list.random.RandomQuery;
import org.wpcleaner.api.repository.namespace.Namespace;
import org.wpcleaner.api.utils.GT;
import org.wpcleaner.api.utils.StringUtils;
import org.wpcleaner.api.wiki.definition.WikiDefinition;
import org.wpcleaner.application.base.processor.ProgressStep;
import org.wpcleaner.application.gui.javafx.JavaFxImageLoader;
import org.wpcleaner.application.gui.javafx.JavaFxProgressTracker;
import org.wpcleaner.application.gui.javafx.core.control.DefaultStyles;
import org.wpcleaner.application.gui.settings.interesting.InterestingByWikiSettings;
import org.wpcleaner.application.gui.settings.interesting.InterestingSettingsManager;
import org.wpcleaner.lib.image.ImageCollection;
import org.wpcleaner.lib.image.ImageSize;

public class PageInput {

  final WikiDefinition wiki;
  final InterestingSettingsManager settingsManager;
  final ApiRandom apiRandom;
  final ComboBox<String> comboBox;
  final ImageView icon;
  final Label label;
  final ToolBar toolBar;

  PageInput(
      final WikiDefinition wiki,
      final InterestingSettingsManager settingsManager,
      final JavaFxImageLoader imageLoader,
      final ApiRandom apiRandom) {
    this.wiki = wiki;
    this.settingsManager = settingsManager;
    this.apiRandom = apiRandom;

    icon =
        imageLoader.getImageView(ImageCollection.PAGE, ImageSize.LABEL).orElseGet(ImageView::new);

    comboBox = new ComboBox<>();
    comboBox.setEditable(true);
    comboBox.setMaxWidth(Double.MAX_VALUE);

    settingsManager.getCurrentSettings().getByWikiSettings(wiki).stream()
        .flatMap(settings -> settings.pages().stream())
        .sorted(String.CASE_INSENSITIVE_ORDER)
        .forEach(comboBox.getItems()::add);

    label = new Label(GT._T("Page"));
    label.setMaxWidth(Double.MAX_VALUE);
    label.setAlignment(Pos.CENTER_RIGHT);

    final Button addPage = new Button();
    addPage.setStyle(DefaultStyles.TOOLBAR_ELEMENT);
    imageLoader
        .getImageView(ImageCollection.LIST_ADD, ImageSize.TOOLBAR)
        .ifPresent(addPage::setGraphic);
    addPage.setTooltip(new Tooltip(GT._T("Add page")));
    addPage.setOnAction(_ -> addPage());

    final Button removePage = new Button();
    removePage.setStyle(DefaultStyles.TOOLBAR_ELEMENT);
    imageLoader
        .getImageView(ImageCollection.LIST_REMOVE, ImageSize.TOOLBAR)
        .ifPresent(removePage::setGraphic);
    removePage.setTooltip(new Tooltip(GT._T("Forget page")));
    removePage.setOnAction(_ -> removePage());

    final Button randomPage = new Button();
    randomPage.setStyle(DefaultStyles.TOOLBAR_ELEMENT);
    imageLoader
        .getImageView(ImageCollection.RANDOM, ImageSize.TOOLBAR)
        .ifPresent(randomPage::setGraphic);
    randomPage.setTooltip(new Tooltip(GT._T("Random page")));
    randomPage.setOnAction(_ -> retrieveRandomPage());

    toolBar = new ToolBar();
    toolBar.setStyle("-fx-background-color: transparent; -fx-padding: 0; -fx-spacing: 1px;");
    toolBar.getItems().addAll(addPage, removePage, randomPage);
  }

  private void addPage() {
    final String value = StringUtils.trim(comboBox.getValue());
    if (!value.isEmpty()) {
      final String page = value.trim();
      if (!page.isEmpty() && !comboBox.getItems().contains(page)) {
        comboBox.getItems().add(page);
        comboBox.getItems().sort(String.CASE_INSENSITIVE_ORDER);
        saveSettings();
      }
    }
  }

  private void removePage() {
    final String value = StringUtils.trim(comboBox.getValue());
    if (!value.isEmpty()) {
      final String page = value.trim();
      if (comboBox.getItems().remove(page)) {
        saveSettings();
      }
    }
  }

  private void saveSettings() {
    final List<String> pages = comboBox.getItems().stream().map(StringUtils::trim).toList();
    settingsManager.updateSettings(
        settingsManager
            .getCurrentSettings()
            .withByWikiSettings(wiki, new InterestingByWikiSettings(pages)));
  }

  public String getPage() {
    return StringUtils.trim(comboBox.getValue());
  }

  private void retrieveRandomPage() {
    if (comboBox.getScene() == null) {
      return;
    }
    final StackPane root = (StackPane) comboBox.getScene().getRoot();
    final BooleanProperty loading = new SimpleBooleanProperty(true);
    final JavaFxProgressTracker progressTracker = JavaFxProgressTracker.forObservable(loading);

    Platform.runLater(() -> root.getChildren().add(progressTracker.getProgressOverlay()));

    final Thread thread = new Thread(() -> doRetrieveRandomPage(root, loading, progressTracker));
    thread.setDaemon(true);
    thread.start();
  }

  @SuppressWarnings("PMD.AvoidCatchingGenericException")
  private void doRetrieveRandomPage(
      final StackPane root,
      final BooleanProperty loading,
      final JavaFxProgressTracker progressTracker) {
    try (ProgressStep _ = progressTracker.start(GT._T("Retrieving random page"))) {
      final RandomQuery query =
          RandomQuery.emptyBuilder()
              .limit(Limit.of(1))
              .namespace(Set.of(new Namespace(0, "Main", "Main")))
              .build();
      final List<RandomPage> pages = apiRandom.retrieveRandomPages(wiki, query);
      Platform.runLater(
          () -> {
            if (!pages.isEmpty()) {
              comboBox.setValue(pages.getFirst().title());
            }
            root.getChildren().remove(progressTracker.getProgressOverlay());
            loading.set(false);
          });
    } catch (final Exception _) {
      Platform.runLater(
          () -> {
            root.getChildren().remove(progressTracker.getProgressOverlay());
            loading.set(false);
          });
    }
  }
}
