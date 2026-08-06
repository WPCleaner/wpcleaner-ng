package org.wpcleaner.application.gui.javafx.main;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import java.util.Objects;
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
import org.jspecify.annotations.Nullable;
import org.wpcleaner.api.api.query.list.random.ApiRandom;
import org.wpcleaner.api.api.query.list.random.RandomPage;
import org.wpcleaner.api.api.query.list.random.RandomQuery;
import org.wpcleaner.api.repository.namespace.Namespace;
import org.wpcleaner.api.utils.GT;
import org.wpcleaner.api.wiki.definition.WikiDefinition;
import org.wpcleaner.application.base.processor.ProgressStep;
import org.wpcleaner.application.gui.javafx.JavaFxImageLoader;
import org.wpcleaner.application.gui.javafx.JavaFxProgressTracker;
import org.wpcleaner.application.gui.javafx.core.action.JavaFxActionServices;
import org.wpcleaner.application.gui.settings.interesting.InterestingSettingsManager;
import org.wpcleaner.lib.image.ImageCollection;
import org.wpcleaner.lib.image.ImageSize;

public class PageInput {

  final ApiRandom apiRandom;
  final ComboBox<@Nullable String> comboBox;
  final ImageView icon;
  final Label label;
  final ToolBar toolBar;

  PageInput(
      final WikiDefinition wiki,
      final InterestingSettingsManager settingsManager,
      final JavaFxImageLoader imageLoader,
      final JavaFxActionServices actionServices,
      final ApiRandom apiRandom) {
    this.apiRandom = apiRandom;

    icon =
        imageLoader.getImageView(ImageCollection.PAGE, ImageSize.LABEL).orElseGet(ImageView::new);

    comboBox = new ComboBox<>();
    comboBox.setEditable(true);
    comboBox.setMaxWidth(Double.MAX_VALUE);

    settingsManager.getCurrentSettings().getByWikiSettings(wiki).stream()
        .flatMap(settings -> settings.pages().stream())
        .forEach(comboBox.getItems()::add);

    label = new Label(GT._T("Page"));
    label.setMaxWidth(Double.MAX_VALUE);
    label.setAlignment(Pos.CENTER_RIGHT);

    final Button addPage = new Button();
    addPage.setStyle("-fx-background-color: transparent; -fx-padding: 1px;");
    imageLoader
        .getImageView(ImageCollection.LIST_ADD, ImageSize.TOOLBAR)
        .ifPresent(addPage::setGraphic);
    addPage.setTooltip(new Tooltip(GT._T("Add page")));
    addPage.setOnAction(_ -> actionServices.notImplemented().run());

    final Button removePage = new Button();
    removePage.setStyle("-fx-background-color: transparent; -fx-padding: 1px;");
    imageLoader
        .getImageView(ImageCollection.LIST_REMOVE, ImageSize.TOOLBAR)
        .ifPresent(removePage::setGraphic);
    removePage.setTooltip(new Tooltip(GT._T("Forget page")));
    removePage.setOnAction(_ -> actionServices.notImplemented().run());

    final Button randomPage = new Button();
    randomPage.setStyle("-fx-background-color: transparent; -fx-padding: 1px;");
    imageLoader
        .getImageView(ImageCollection.RANDOM, ImageSize.TOOLBAR)
        .ifPresent(randomPage::setGraphic);
    randomPage.setTooltip(new Tooltip(GT._T("Random page")));
    randomPage.setOnAction(_ -> retrieveRandomPage(wiki));

    toolBar = new ToolBar();
    toolBar.setStyle("-fx-background-color: transparent; -fx-padding: 0; -fx-spacing: 1px;");
    toolBar.getItems().addAll(addPage, removePage, randomPage);
  }

  public String getPage() {
    return Objects.requireNonNullElse(comboBox.getValue(), "");
  }

  @SuppressWarnings("PMD.AvoidCatchingGenericException")
  private void retrieveRandomPage(final WikiDefinition wiki) {
    if (comboBox.getScene() == null) {
      return;
    }
    final StackPane root = (StackPane) comboBox.getScene().getRoot();
    final BooleanProperty loading = new SimpleBooleanProperty(true);
    final JavaFxProgressTracker progressTracker = JavaFxProgressTracker.forObservable(loading);

    Platform.runLater(() -> root.getChildren().add(progressTracker.getProgressOverlay()));

    final Thread thread =
        new Thread(
            () -> {
              try (ProgressStep _ = progressTracker.start(GT._T("Retrieving random page"))) {
                final RandomQuery query =
                    RandomQuery.emptyBuilder()
                        .limit(1)
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
              } catch (final Exception e) {
                Platform.runLater(
                    () -> {
                      root.getChildren().remove(progressTracker.getProgressOverlay());
                      loading.set(false);
                    });
              }
            });
    thread.setDaemon(true);
    thread.start();
  }
}
