package org.wpcleaner.application.gui.javafx.recentchanges;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Window;
import org.jspecify.annotations.Nullable;
import org.wpcleaner.api.api.query.list.recentchanges.RecentChangesParameters;
import org.wpcleaner.api.api.query.list.tags.Tag;
import org.wpcleaner.api.repository.namespace.Namespace;
import org.wpcleaner.application.gui.javafx.JavaFxImageLoader;
import org.wpcleaner.application.gui.javafx.core.NamespaceCheckComboBox;
import org.wpcleaner.application.gui.javafx.core.TagCheckComboBox;

public final class RecentChangesFilterDialog extends Dialog<@Nullable RecentChangesFilter> {

  private final TextField nameField;
  private final NamespaceCheckComboBox namespaceCheckComboBox;
  private final TagCheckComboBox tagCheckComboBox;
  private final TypeCheckComboBox typeCheckComboBox;
  private final ComboBox<@Nullable Severity> severityComboBox;

  public RecentChangesFilterDialog(
      @Nullable final Window owner,
      final JavaFxImageLoader imageLoader,
      final List<Namespace> availableNamespaces,
      final List<Tag> availableTags,
      @Nullable final RecentChangesFilter initialFilter) {
    super();
    initOwner(owner);
    setTitle("Recent changes filter");

    final GridPane grid = new GridPane();
    grid.setHgap(10);
    grid.setVgap(10);
    grid.setPadding(new Insets(15, 15, 15, 15));

    nameField = new TextField();
    nameField.setPrefWidth(250);
    if (initialFilter != null) {
      nameField.setText(initialFilter.name());
    }
    grid.add(new Label("Name:"), 0, 0);
    grid.add(nameField, 1, 0);

    namespaceCheckComboBox = new NamespaceCheckComboBox();
    namespaceCheckComboBox.setup(
        availableNamespaces, initialFilter != null ? initialFilter.namespace() : Set.of());
    grid.add(new Label("Namespaces:"), 0, 1);
    grid.add(namespaceCheckComboBox, 1, 1);

    tagCheckComboBox = new TagCheckComboBox();
    tagCheckComboBox.setup(availableTags, initialFilter != null ? initialFilter.tag() : null);
    grid.add(new Label("Tags:"), 0, 2);
    grid.add(tagCheckComboBox, 1, 2);

    typeCheckComboBox = new TypeCheckComboBox();
    typeCheckComboBox.setup(initialFilter != null ? initialFilter.type() : Set.of());
    grid.add(new Label("Types:"), 0, 3);
    grid.add(typeCheckComboBox, 1, 3);

    severityComboBox = new ComboBox<>();
    severityComboBox.getItems().add(null);
    severityComboBox.getItems().addAll(Severity.values());
    severityComboBox.setCellFactory(_ -> new SeverityListCell(imageLoader));
    severityComboBox.setButtonCell(new SeverityListCell(imageLoader));
    severityComboBox.setMaxWidth(Double.MAX_VALUE);
    if (initialFilter != null) {
      severityComboBox.getSelectionModel().select(initialFilter.severity());
    } else {
      severityComboBox.getSelectionModel().select(null);
    }
    grid.add(new Label("Severity:"), 0, 4);
    grid.add(severityComboBox, 1, 4);

    getDialogPane().setContent(grid);

    final ButtonType okButtonType = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
    final ButtonType cancelButtonType = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
    getDialogPane().getButtonTypes().addAll(okButtonType, cancelButtonType);

    final Button okButton = (Button) getDialogPane().lookupButton(okButtonType);
    okButton.addEventFilter(
        javafx.event.ActionEvent.ACTION,
        event -> {
          if (nameField.getText().isBlank()) {
            event.consume();
            final Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.initOwner(getDialogPane().getScene().getWindow());
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("The filter name cannot be empty or blank.");
            alert.showAndWait();
          }
        });

    setResultConverter(
        dialogButton -> {
          if (Objects.equals(dialogButton, okButtonType)) {
            final String name = nameField.getText().trim();
            final Set<Integer> namespaceSet =
                namespaceCheckComboBox.getCheckModel().getCheckedItems().stream()
                    .map(Namespace::id)
                    .collect(Collectors.toUnmodifiableSet());
            final Set<String> tagSet =
                tagCheckComboBox.getCheckModel().getCheckedItems().stream()
                    .map(Tag::name)
                    .collect(Collectors.toUnmodifiableSet());
            final Set<RecentChangesParameters.Type> typeSet =
                typeCheckComboBox.getCheckModel().getCheckedItems().stream()
                    .collect(Collectors.toUnmodifiableSet());
            final Severity severity = severityComboBox.getSelectionModel().getSelectedItem();
            return new RecentChangesFilter(name, namespaceSet, severity, tagSet, typeSet);
          }
          return null;
        });
  }

  @Nullable Severity getSelectedSeverity() {
    return severityComboBox.getSelectionModel().getSelectedItem();
  }

  public static Optional<RecentChangesFilter> showDialog(
      final Window owner,
      final JavaFxImageLoader imageLoader,
      final List<Namespace> availableNamespaces,
      final List<Tag> availableTags,
      @Nullable final RecentChangesFilter initialFilter) {
    final RecentChangesFilterDialog dialog =
        new RecentChangesFilterDialog(
            owner, imageLoader, availableNamespaces, availableTags, initialFilter);
    return dialog.showAndWait();
  }
}
