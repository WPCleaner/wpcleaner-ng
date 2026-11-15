package org.wpcleaner.application.gui.javafx.analysis;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
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
import org.wpcleaner.api.repository.linter.LinterPriority;
import org.wpcleaner.api.rest.lint.LintError;
import org.wpcleaner.api.utils.GT;
import org.wpcleaner.application.gui.javafx.core.control.TextTableColumn;
import org.wpcleaner.application.gui.javafx.core.pageanalysis.PageAnalysisArea;
import org.wpcleaner.application.gui.javafx.core.window.JavaFxWindow;

final class LinterErrorsWindow extends JavaFxWindow<JavaFxAnalysisWindowServices> {

  static final String CLOSE_ID = "close";
  static final String ERRORS_ID = "errors";

  private static final String PARAMETER_SEPARATOR = ";";

  private final List<LinterError> errors;
  private final PageAnalysisArea area;

  LinterErrorsWindow(
      final JavaFxAnalysisWindowServices services,
      @Nullable final Stage owner,
      final String pageName,
      final List<LinterError> errors,
      final PageAnalysisArea area) {
    super(services, owner);
    this.errors = List.copyOf(errors);
    this.area = area;
    stage.setTitle(GT._T("Errors detected by Linter in %s", pageName));
    initialize();
    stage.show();
  }

  @Override
  public String getHelpPage() {
    return "Analysis";
  }

  @Override
  public String getName() {
    return "linterErrors";
  }

  @Override
  protected Scene createScene() {
    final Label message =
        new Label(GT._T("The following errors are detected by Linter in the analyzed text:"));
    message.setWrapText(true);

    final TableView<LinterError> table = createTable();
    VBox.setVgrow(table, Priority.ALWAYS);

    final HBox buttons = new HBox(10, createCloseButton());
    buttons.setAlignment(Pos.CENTER_RIGHT);

    final VBox mainContainer = new VBox(10, message, table, buttons);
    mainContainer.setPadding(new Insets(15));
    final StackPane root = new StackPane(mainContainer, progressOverlay);
    return new Scene(root, 800, 400);
  }

  private TableView<LinterError> createTable() {
    final TableView<LinterError> table = new TableView<>();
    table.setId(ERRORS_ID);
    table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
    table
        .getColumns()
        .addAll(
            List.of(
                new TextTableColumn<>(GT._T("Type"), error -> error.error().type()),
                new TextTableColumn<>(GT._T("Priority"), error -> getPriority(error.priority())),
                new TextTableColumn<>(GT._T("Start"), error -> getPosition(error.error().begin())),
                new TextTableColumn<>(GT._T("End"), error -> getPosition(error.error().end())),
                new TextTableColumn<>(GT._T("Parameters"), error -> getParameters(error.error())),
                new TextTableColumn<>(GT._T("Template"), error -> getTemplate(error.error()))));
    table.getItems().setAll(errors);
    table
        .getSelectionModel()
        .selectedItemProperty()
        .addListener(
            (_, _, error) -> {
              if (error != null) {
                selectError(error.error());
              }
            });
    return table;
  }

  private static String getPriority(final LinterPriority priority) {
    return switch (priority) {
      case HIGH -> GT._T("High");
      case MEDIUM -> GT._T("Medium");
      case LOW -> GT._T("Low");
    };
  }

  private static String getPosition(final int position) {
    return position < 0 ? "" : Integer.toString(position);
  }

  private static String getParameters(final LintError error) {
    return error.parameters().entrySet().stream()
        .sorted(Map.Entry.comparingByKey())
        .map(entry -> entry.getKey() + "=" + entry.getValue())
        .collect(Collectors.joining(PARAMETER_SEPARATOR));
  }

  private static String getTemplate(final LintError error) {
    final LintError.TemplateInfo templateInfo = error.templateInfo();
    if (templateInfo == null) {
      return "";
    }
    if (templateInfo.name() != null) {
      return templateInfo.name();
    }
    return templateInfo.isMultiPartTemplateBlock() ? GT._T("Multiple templates") : "";
  }

  private void selectError(final LintError error) {
    final int length = area.getLength();
    final int begin = error.begin();
    if (begin < 0 || begin > length) {
      return;
    }
    final int end = error.end();
    area.selectRange(begin, end >= begin && end <= length ? end : begin);
    area.requestFollowCaret();
  }

  private Button createCloseButton() {
    final Button closeButton = new Button(GT._T("Close"));
    closeButton.setId(CLOSE_ID);
    closeButton.setCancelButton(true);
    closeButton.setOnAction(_ -> stage.close());
    return closeButton;
  }
}
