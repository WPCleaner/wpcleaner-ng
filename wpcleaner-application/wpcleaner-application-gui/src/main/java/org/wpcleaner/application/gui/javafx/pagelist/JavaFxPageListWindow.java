package org.wpcleaner.application.gui.javafx.pagelist;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import java.util.function.Consumer;
import javafx.beans.binding.Bindings;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import org.wpcleaner.api.progress.LongRunningTask;
import org.wpcleaner.api.utils.GT;
import org.wpcleaner.application.gui.javafx.core.control.DefaultStyles;
import org.wpcleaner.application.gui.javafx.core.window.JavaFxWindow;
import org.wpcleaner.lib.image.ImageCollection;
import org.wpcleaner.lib.image.ImageSize;

public final class JavaFxPageListWindow extends JavaFxWindow<JavaFxPageListWindowServices> {

  public static final String ANALYZE_ID = "analyze";
  public static final String PAGES_ID = "pages";

  private static final int DOUBLE_CLICK_COUNT = 2;

  private final ListView<String> pagesListView;
  private final Consumer<String> pageAction;

  public JavaFxPageListWindow(
      final JavaFxPageListWindowServices services,
      final String title,
      final LongRunningTask<List<String>> pagesTask,
      final Consumer<String> pageAction) {
    super(services);
    this.pagesListView = new ListView<>();
    this.pageAction = pageAction;
    initialize();
    stage.setTitle(title);
    stage.show();
    executeAsync(pagesTask, pagesListView.getItems()::setAll, this::showPagesError);
  }

  @Override
  public String getName() {
    return "pageList";
  }

  @Override
  protected Scene createScene() {
    pagesListView.setId(PAGES_ID);
    pagesListView.setOnMouseClicked(
        event -> {
          if (event.getButton() == MouseButton.PRIMARY
              && event.getClickCount() == DOUBLE_CLICK_COUNT) {
            openSelectedPages();
          }
        });
    pagesListView.setOnKeyPressed(
        event -> {
          if (event.getCode() == KeyCode.ENTER) {
            openSelectedPages();
          }
        });
    pagesListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    pagesListView.disableProperty().bind(loading);
    final BorderPane content = new BorderPane(pagesListView);
    content.setBottom(new ToolBar(createAnalyzeButton()));
    return new Scene(new StackPane(content, progressOverlay), 500, 600);
  }

  private Button createAnalyzeButton() {
    final Button button = new Button();
    button.setId(ANALYZE_ID);
    button.setStyle(DefaultStyles.TOOLBAR_ELEMENT);
    imageLoader
        .getImageView(ImageCollection.ANALYSIS, ImageSize.BUTTON)
        .ifPresent(button::setGraphic);
    button.setTooltip(new Tooltip(GT._T("Analyze the selected pages")));
    button
        .disableProperty()
        .bind(Bindings.isEmpty(pagesListView.getSelectionModel().getSelectedItems()).or(loading));
    button.setOnAction(_ -> openSelectedPages());
    return button;
  }

  private void openSelectedPages() {
    List.copyOf(pagesListView.getSelectionModel().getSelectedItems()).forEach(pageAction);
  }

  private void showPagesError(final Exception e) {
    showWarning(
        GT._T("Error retrieving the pages"),
        GT._T("Error retrieving the pages: %s", String.valueOf(e.getMessage())));
  }
}
