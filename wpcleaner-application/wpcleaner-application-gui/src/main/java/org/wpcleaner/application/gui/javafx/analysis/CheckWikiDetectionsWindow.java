package org.wpcleaner.application.gui.javafx.analysis;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.jspecify.annotations.Nullable;
import org.wpcleaner.api.utils.GT;
import org.wpcleaner.api.wiki.definition.WikiDefinition;
import org.wpcleaner.application.gui.javafx.core.control.TextTableColumn;
import org.wpcleaner.application.gui.javafx.core.pageanalysis.PageAnalysisArea;
import org.wpcleaner.application.gui.javafx.core.window.JavaFxWindow;
import org.wpcleaner.lib.check.server.CheckWikiDetection;

final class CheckWikiDetectionsWindow extends JavaFxWindow<JavaFxAnalysisWindowServices> {

  static final String CLOSE_ID = "close";
  static final String DETECTIONS_ID = "detections";

  private final WikiDefinition wiki;
  private final List<CheckWikiDetection> detections;
  private final PageAnalysisArea area;

  CheckWikiDetectionsWindow(
      final JavaFxAnalysisWindowServices services,
      @Nullable final Stage owner,
      final String pageName,
      final List<CheckWikiDetection> detections,
      final PageAnalysisArea area) {
    super(services, owner);
    this.wiki = services.user().getCurrentUser().wiki();
    this.detections = List.copyOf(detections);
    this.area = area;
    stage.setTitle(GT._T("Errors detected by Check Wiki in %s", pageName));
    initialize();
    stage.show();
  }

  @Override
  public String getHelpPage() {
    return "Analysis";
  }

  @Override
  public String getName() {
    return "checkWikiDetections";
  }

  @Override
  protected Scene createScene() {
    final Label message =
        new Label(
            GT._T(
                "The following errors are detected by Check Wiki in the page saved on the wiki:"));
    message.setWrapText(true);

    final TableView<CheckWikiDetection> table = createTable();
    VBox.setVgrow(table, Priority.ALWAYS);

    final HBox buttons = new HBox(10, createCloseButton());
    buttons.setAlignment(Pos.CENTER_RIGHT);

    final VBox mainContainer = new VBox(10, message, table, buttons);
    mainContainer.setPadding(new Insets(15));
    final StackPane root = new StackPane(mainContainer, progressOverlay);
    return new Scene(root, 650, 400);
  }

  private TableView<CheckWikiDetection> createTable() {
    final TableView<CheckWikiDetection> table = new TableView<>();
    table.setId(DETECTIONS_ID);
    table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
    table
        .getColumns()
        .addAll(
            List.of(
                new TextTableColumn<>(
                    GT._T("Error"), detection -> Integer.toString(detection.errorNumber())),
                new TextTableColumn<>(GT._T("Name"), this::getAlgorithmName),
                new TextTableColumn<>(GT._T("Location"), CheckWikiDetectionsWindow::getLocation),
                new TextTableColumn<>(GT._T("Notice"), CheckWikiDetectionsWindow::getNotice)));
    table.getItems().setAll(detections);
    table
        .getSelectionModel()
        .selectedItemProperty()
        .addListener(
            (_, _, detection) -> {
              if (detection != null) {
                selectDetection(detection);
              }
            });
    return table;
  }

  private String getAlgorithmName(final CheckWikiDetection detection) {
    return services
        .checkAlgorithmRegistry()
        .findAlgorithm(detection.errorNumber())
        .map(algorithm -> services.checkAlgorithmRegistry().getName(wiki, algorithm))
        .orElse("");
  }

  private static String getLocation(final CheckWikiDetection detection) {
    return detection.location() < 0 ? "" : Integer.toString(detection.location());
  }

  private static String getNotice(final CheckWikiDetection detection) {
    return detection.whitelisted() ? GT._T("In the whitelist") : detection.notice();
  }

  private void selectDetection(final CheckWikiDetection detection) {
    final String text = area.getText();
    final String notice = detection.notice();
    final int noticeIndex = findIgnoringCase(text, notice);
    if (noticeIndex >= 0) {
      area.selectRange(noticeIndex, noticeIndex + notice.length());
    } else if (detection.location() >= 0 && detection.location() <= text.length()) {
      area.selectRange(detection.location(), detection.location());
    } else {
      return;
    }
    area.requestFollowCaret();
  }

  private static int findIgnoringCase(final String text, final String searched) {
    if (searched.isEmpty()) {
      return -1;
    }
    for (int index = 0; index + searched.length() <= text.length(); index++) {
      if (text.regionMatches(true, index, searched, 0, searched.length())) {
        return index;
      }
    }
    return -1;
  }

  private Button createCloseButton() {
    final Button closeButton = new Button(GT._T("Close"));
    closeButton.setId(CLOSE_ID);
    closeButton.setCancelButton(true);
    closeButton.setOnAction(_ -> stage.close());
    return closeButton;
  }
}
