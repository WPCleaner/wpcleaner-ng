package org.wpcleaner.application.gui.javafx.recentchanges;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.ArrayList;
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
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Window;
import org.jspecify.annotations.Nullable;
import org.wpcleaner.api.api.query.list.recentchanges.RecentChangesParameters;
import org.wpcleaner.api.api.query.list.tags.Tag;
import org.wpcleaner.api.repository.namespace.Namespace;

@SuppressWarnings("PMD.CouplingBetweenObjects")
public final class RecentChangesOptionsDialog extends Dialog<@Nullable RecentChangesOptions> {

  private final TextField nameField;
  private final ListView<@Nullable Namespace> namespaceListView;
  private final ListView<RecentChangesParameters.@Nullable Show> showListView;
  private final ComboBox<@Nullable String> tagField;
  private final ListView<RecentChangesParameters.@Nullable Type> typeListView;
  private final CheckBox topOnlyCheckbox;

  public RecentChangesOptionsDialog(
      final Window owner,
      final List<Namespace> availableNamespaces,
      final List<Tag> availableTags,
      @Nullable final RecentChangesOptions initialOptions) {
    super();
    initOwner(owner);
    setTitle("Recent changes options");

    final GridPane grid = new GridPane();
    grid.setHgap(10);
    grid.setVgap(10);
    grid.setPadding(new Insets(15, 15, 15, 15));

    nameField = new TextField();
    nameField.setPrefWidth(250);
    if (initialOptions != null) {
      nameField.setText(initialOptions.name());
    }
    grid.add(new Label("Name:"), 0, 0);
    grid.add(nameField, 1, 0);

    namespaceListView = new ListView<>();
    setupNamespaceList(availableNamespaces, initialOptions);
    grid.add(new Label("Namespace:"), 0, 1);
    grid.add(namespaceListView, 1, 1);

    showListView = new ListView<>();
    setupShowList(initialOptions);
    grid.add(new Label("Show:"), 0, 2);
    grid.add(showListView, 1, 2);

    final List<String> tagNames = new ArrayList<>();
    tagNames.add("");
    for (final Tag tag : availableTags) {
      tagNames.add(tag.name());
    }
    tagField = new ComboBox<>();
    tagField.getItems().addAll(tagNames);
    tagField.getSelectionModel().select("");
    if (initialOptions != null && initialOptions.tag() != null) {
      tagField.getSelectionModel().select(initialOptions.tag());
    }
    grid.add(new Label("Tag:"), 0, 3);
    grid.add(tagField, 1, 3);

    typeListView = new ListView<>();
    setupTypeList(initialOptions);
    grid.add(new Label("Type:"), 0, 4);
    grid.add(typeListView, 1, 4);

    topOnlyCheckbox = new CheckBox();
    if (initialOptions != null) {
      topOnlyCheckbox.setSelected(initialOptions.topOnly());
    }
    grid.add(new Label("Top only:"), 0, 5);
    grid.add(topOnlyCheckbox, 1, 5);

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
            alert.setContentText("The option name cannot be empty or blank.");
            alert.showAndWait();
          }
        });

    setResultConverter(
        dialogButton -> {
          if (Objects.equals(dialogButton, okButtonType)) {
            final String name = nameField.getText().trim();
            final Set<Integer> namespaceSet =
                namespaceListView.getSelectionModel().getSelectedItems().stream()
                    .filter(Objects::nonNull)
                    .map(Namespace::id)
                    .collect(Collectors.toUnmodifiableSet());
            final Set<RecentChangesParameters.Show> showSet =
                showListView.getSelectionModel().getSelectedItems().stream()
                    .collect(Collectors.toUnmodifiableSet());
            final String selectedTag = tagField.getSelectionModel().getSelectedItem();
            final String tag = (selectedTag == null || selectedTag.isEmpty()) ? null : selectedTag;
            final Set<RecentChangesParameters.Type> typeSet =
                typeListView.getSelectionModel().getSelectedItems().stream()
                    .collect(Collectors.toUnmodifiableSet());
            final boolean topOnly = topOnlyCheckbox.isSelected();
            return new RecentChangesOptions(name, namespaceSet, showSet, tag, typeSet, topOnly);
          }
          return null;
        });
  }

  private void setupNamespaceList(
      final List<Namespace> availableNamespaces,
      @Nullable final RecentChangesOptions initialOptions) {
    namespaceListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    namespaceListView.setPrefHeight(100);
    namespaceListView.getItems().addAll(availableNamespaces);
    namespaceListView.setCellFactory(
        _ ->
            new ListCell<>() {
              @Override
              protected void updateItem(@Nullable final Namespace item, final boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                  setText(null);
                } else {
                  setText(item.name());
                }
              }
            });
    if (initialOptions != null) {
      for (final Namespace ns : availableNamespaces) {
        if (initialOptions.namespace().contains(ns.id())) {
          namespaceListView.getSelectionModel().select(ns);
        }
      }
    }
  }

  private void setupShowList(@Nullable final RecentChangesOptions initialOptions) {
    showListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    showListView.setPrefHeight(100);
    showListView.getItems().addAll(RecentChangesParameters.Show.values());
    showListView.setCellFactory(
        _ ->
            new ListCell<>() {
              @Override
              protected void updateItem(
                  final RecentChangesParameters.@Nullable Show item, final boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                  setText(null);
                } else {
                  setText(item.value);
                }
              }
            });
    if (initialOptions != null) {
      for (final RecentChangesParameters.Show show : RecentChangesParameters.Show.values()) {
        if (initialOptions.show().contains(show)) {
          showListView.getSelectionModel().select(show);
        }
      }
    }
  }

  private void setupTypeList(@Nullable final RecentChangesOptions initialOptions) {
    typeListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    typeListView.setPrefHeight(100);
    typeListView.getItems().addAll(RecentChangesParameters.Type.values());
    typeListView.setCellFactory(
        _ ->
            new ListCell<>() {
              @Override
              protected void updateItem(
                  final RecentChangesParameters.@Nullable Type item, final boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                  setText(null);
                } else {
                  setText(item.value);
                }
              }
            });
    if (initialOptions != null) {
      for (final RecentChangesParameters.Type type : RecentChangesParameters.Type.values()) {
        if (initialOptions.type().contains(type)) {
          typeListView.getSelectionModel().select(type);
        }
      }
    }
  }

  public static Optional<RecentChangesOptions> showDialog(
      final Window owner,
      final List<Namespace> availableNamespaces,
      final List<Tag> availableTags,
      @Nullable final RecentChangesOptions initialOptions) {
    final RecentChangesOptionsDialog dialog =
        new RecentChangesOptionsDialog(owner, availableNamespaces, availableTags, initialOptions);
    return dialog.showAndWait();
  }
}
