package org.wpcleaner.application.gui.javafx.core.action.content.append;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.wpcleaner.api.utils.GT;

public final class ContentAppendActionControl extends TitledPane {

  private static final double LABEL_WIDTH = 120.0;

  private final List<CheckBox> checkBoxes = new ArrayList<>();
  private final TextField summaryField;

  public ContentAppendActionControl(
      final String title, final ContentAppendConfiguration initialConfig) {
    super();
    Objects.requireNonNull(initialConfig, "initialConfig");

    setCollapsible(false);
    setMaxHeight(Double.MAX_VALUE);
    setMaxWidth(Double.MAX_VALUE);
    setText(title);

    this.summaryField = new TextField(initialConfig.summary());

    final GridPane grid = new GridPane();
    grid.setHgap(10);
    grid.setVgap(10);
    grid.setMaxHeight(Double.MAX_VALUE);

    final Label summaryLabel = new Label(GT._T("Summary:"));
    summaryLabel.setMinWidth(LABEL_WIDTH);
    summaryLabel.setPrefWidth(LABEL_WIDTH);
    grid.add(summaryLabel, 0, 0);
    grid.add(summaryField, 1, 0);
    GridPane.setHgrow(summaryField, Priority.ALWAYS);

    final Label staticTextsLabel = new Label(GT._T("Additional texts:"));
    staticTextsLabel.setMinWidth(LABEL_WIDTH);
    staticTextsLabel.setPrefWidth(LABEL_WIDTH);

    final VBox checkboxesBox = new VBox(5);
    for (final ContentAppendConfigurationText textConfig : initialConfig.texts()) {
      final CheckBox cb = new CheckBox(textConfig.label());
      cb.setSelected(textConfig.addedByDefault());
      cb.setUserData(textConfig.text());
      checkBoxes.add(cb);
      checkboxesBox.getChildren().add(cb);
    }

    grid.add(staticTextsLabel, 0, 1);
    grid.add(checkboxesBox, 1, 1);
    GridPane.setHgrow(checkboxesBox, Priority.ALWAYS);
    GridPane.setVgrow(checkboxesBox, Priority.ALWAYS);

    setContent(grid);
  }

  public List<String> getSelectedTexts() {
    final List<String> selectedTexts = new ArrayList<>();
    for (final CheckBox cb : checkBoxes) {
      if (cb.isSelected()) {
        selectedTexts.add((String) cb.getUserData());
      }
    }
    return List.copyOf(selectedTexts);
  }

  public String getSummary() {
    return summaryField.getText();
  }

  public boolean isComplete() {
    return !summaryField.getText().isBlank();
  }
}
