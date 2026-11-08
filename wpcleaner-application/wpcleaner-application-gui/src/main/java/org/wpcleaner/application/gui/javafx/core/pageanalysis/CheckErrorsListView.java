package org.wpcleaner.application.gui.javafx.core.pageanalysis;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.Map;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import org.jspecify.annotations.Nullable;

public final class CheckErrorsListView extends ListView<CheckAlgorithmErrors> {

  private static final int DOUBLE_CLICK_COUNT = 2;
  private static final double TOOLTIP_MAX_WIDTH = 400;
  private static final Pattern HTML_TAG = Pattern.compile("<[^>]*>");
  private static final Map<String, String> HTML_ENTITIES =
      Map.of("amp", "&", "lt", "<", "gt", ">", "quot", "\"", "#39", "'", "nbsp", " ");
  private static final Pattern HTML_ENTITY =
      Pattern.compile("&(" + String.join("|", HTML_ENTITIES.keySet()) + ");");
  private static final String FIXED_STYLE =
      "-fx-control-inner-background: #CCFFCC; -fx-control-inner-background-alt: #CCFFCC;";

  public CheckErrorsListView(final Consumer<CheckAlgorithmErrors> doubleClickAction) {
    super();
    setCellFactory(_ -> createCell(doubleClickAction));
  }

  private static ListCell<CheckAlgorithmErrors> createCell(
      final Consumer<CheckAlgorithmErrors> doubleClickAction) {
    final ListCell<CheckAlgorithmErrors> cell = new CheckAlgorithmErrorsCell();
    cell.setOnMouseClicked(event -> handleCellMouseClicked(event, cell, doubleClickAction));
    return cell;
  }

  private static void handleCellMouseClicked(
      final MouseEvent event,
      final ListCell<CheckAlgorithmErrors> cell,
      final Consumer<CheckAlgorithmErrors> doubleClickAction) {
    final CheckAlgorithmErrors item = cell.getItem();
    if (event.getButton() == MouseButton.PRIMARY
        && event.getClickCount() == DOUBLE_CLICK_COUNT
        && item != null) {
      doubleClickAction.accept(item);
    }
  }

  private static final class CheckAlgorithmErrorsCell extends ListCell<CheckAlgorithmErrors> {

    @Override
    protected void updateItem(@Nullable final CheckAlgorithmErrors item, final boolean empty) {
      super.updateItem(item, empty);
      setText(empty || item == null ? null : createText(item));
      setStyle(!empty && item != null && item.errors().isEmpty() ? FIXED_STYLE : "");
      final String description = empty || item == null ? null : item.description();
      setTooltip(description == null ? null : createTooltip(description));
    }

    private static Tooltip createTooltip(final String description) {
      final Tooltip tooltip = new Tooltip(toPlainText(description));
      tooltip.setWrapText(true);
      tooltip.setMaxWidth(TOOLTIP_MAX_WIDTH);
      return tooltip;
    }

    private static String toPlainText(final String html) {
      return HTML_ENTITY
          .matcher(HTML_TAG.matcher(html).replaceAll(""))
          .replaceAll(entity -> Matcher.quoteReplacement(HTML_ENTITIES.get(entity.group(1))));
    }

    private static String createText(final CheckAlgorithmErrors item) {
      final String name = item.name();
      final int errorsCount = item.errors().size();
      return errorsCount > 1 ? "%s (%d)".formatted(name, errorsCount) : name;
    }
  }
}
