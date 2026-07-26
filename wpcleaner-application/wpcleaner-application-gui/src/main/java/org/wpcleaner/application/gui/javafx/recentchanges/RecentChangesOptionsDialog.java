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
import javafx.scene.control.TextField;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Window;
import org.jspecify.annotations.Nullable;
import org.wpcleaner.api.api.query.list.recentchanges.RecentChangesParameters;
import org.wpcleaner.api.api.query.list.tags.Tag;
import org.wpcleaner.api.repository.namespace.Namespace;
import org.wpcleaner.api.utils.GT;
import org.wpcleaner.application.gui.javafx.JavaFxImageLoader;
import org.wpcleaner.application.gui.javafx.core.control.NamespaceCheckComboBox;

@SuppressWarnings("PMD.CouplingBetweenObjects")
public final class RecentChangesOptionsDialog extends Dialog<@Nullable RecentChangesOptions> {

  private final TextField nameField;
  private final NamespaceCheckComboBox namespaceComboBox;
  private final ShowCheckComboBox showComboBox;
  private final ComboBox<@Nullable String> tagField;
  private final TypeCheckComboBox typeComboBox;
  private final CheckBox topOnlyCheckbox;

  public RecentChangesOptionsDialog(
      @Nullable final Window owner,
      final JavaFxImageLoader imageLoader,
      final List<Namespace> availableNamespaces,
      final List<Tag> availableTags,
      final RecentChangesOptions initialOptions) {
    super();
    initOwner(owner);
    setTitle(GT._T("Recent changes options"));

    final GridPane grid = new GridPane();
    grid.setHgap(10);
    grid.setVgap(10);
    grid.setPadding(new Insets(15, 15, 15, 15));

    nameField = new TextField();
    setupNameField(initialOptions);
    grid.add(new Label(GT._T("Name:")), 0, 0);
    grid.add(nameField, 1, 0);

    namespaceComboBox = new NamespaceCheckComboBox();
    namespaceComboBox.setup(availableNamespaces, initialOptions.namespace());
    grid.add(new Label(GT._T("Namespace:")), 0, 1);
    grid.add(namespaceComboBox, 1, 1);

    showComboBox = new ShowCheckComboBox();
    showComboBox.setup(initialOptions.show());
    grid.add(new Label(GT._T("Show:")), 0, 2);
    grid.add(showComboBox, 1, 2);

    tagField = new ComboBox<>();
    setupTagField(availableTags, initialOptions);
    grid.add(new Label(GT._T("Tag:")), 0, 3);
    grid.add(tagField, 1, 3);

    typeComboBox = new TypeCheckComboBox();
    typeComboBox.setup(initialOptions.type());
    grid.add(new Label(GT._T("Type:")), 0, 4);
    grid.add(typeComboBox, 1, 4);

    topOnlyCheckbox = new CheckBox();
    setupTopOnlyCheckbox(initialOptions);
    grid.add(new Label(GT._T("Top only:")), 0, 5);
    grid.add(topOnlyCheckbox, 1, 5);

    final RecentChangesFilterListView filtersListView =
        setupFilters(grid, imageLoader, availableNamespaces, availableTags, initialOptions);

    getDialogPane().setContent(grid);

    final ButtonType okButtonType = new ButtonType(GT._T("OK"), ButtonBar.ButtonData.OK_DONE);
    final ButtonType cancelButtonType =
        new ButtonType(GT._T("Cancel"), ButtonBar.ButtonData.CANCEL_CLOSE);
    getDialogPane().getButtonTypes().addAll(okButtonType, cancelButtonType);

    final Button okButton = (Button) getDialogPane().lookupButton(okButtonType);
    okButton.addEventFilter(
        javafx.event.ActionEvent.ACTION,
        event -> {
          if (nameField.getText().isBlank()) {
            event.consume();
            final Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.initOwner(getDialogPane().getScene().getWindow());
            alert.setTitle(GT._T("Error"));
            alert.setHeaderText(null);
            alert.setContentText(GT._T("The option name cannot be empty or blank."));
            alert.showAndWait();
          }
        });

    setResultConverter(
        dialogButton -> {
          if (Objects.equals(dialogButton, okButtonType)) {
            final String name = nameField.getText().trim();
            final Set<Integer> namespaceSet =
                namespaceComboBox.getCheckModel().getCheckedItems().stream()
                    .map(Namespace::id)
                    .collect(Collectors.toUnmodifiableSet());
            final Set<RecentChangesParameters.Show> showSet =
                showComboBox.getCheckModel().getCheckedItems().stream()
                    .collect(Collectors.toUnmodifiableSet());
            final String selectedTag = tagField.getSelectionModel().getSelectedItem();
            final String tag = (selectedTag == null || selectedTag.isEmpty()) ? null : selectedTag;
            final Set<RecentChangesParameters.Type> typeSet =
                typeComboBox.getCheckModel().getCheckedItems().stream()
                    .collect(Collectors.toUnmodifiableSet());
            final boolean topOnly = topOnlyCheckbox.isSelected();
            final List<@Nullable RecentChangesFilter> filterList =
                new ArrayList<>(filtersListView.getItems());
            return new RecentChangesOptions(
                name, namespaceSet, showSet, tag, typeSet, topOnly, List.copyOf(filterList));
          }
          return null;
        });
  }

  private void setupNameField(@Nullable final RecentChangesOptions initialOptions) {
    nameField.setPrefWidth(250);
    if (initialOptions != null) {
      nameField.setText(initialOptions.name());
    }
  }

  private void setupTagField(
      final List<Tag> availableTags, @Nullable final RecentChangesOptions initialOptions) {
    final List<String> tagNames = new ArrayList<>();
    tagNames.add("");
    for (final Tag tag : availableTags) {
      tagNames.add(tag.name());
    }
    tagField.getItems().addAll(tagNames);
    tagField.getSelectionModel().select("");
    if (initialOptions != null && initialOptions.tag() != null) {
      tagField.getSelectionModel().select(initialOptions.tag());
    }
  }

  private void setupTopOnlyCheckbox(@Nullable final RecentChangesOptions initialOptions) {
    if (initialOptions != null) {
      topOnlyCheckbox.setSelected(initialOptions.topOnly());
    }
  }

  private RecentChangesFilterListView setupFilters(
      final GridPane grid,
      final JavaFxImageLoader imageLoader,
      final List<Namespace> availableNamespaces,
      final List<Tag> availableTags,
      final RecentChangesOptions initialOptions) {
    final ToolBar filtersToolBar = new ToolBar();
    final RecentChangesFilterListView filtersListView =
        new RecentChangesFilterListView(
            imageLoader, availableNamespaces, availableTags, filtersToolBar);
    filtersListView.getItems().addAll(initialOptions.filters());

    final VBox filtersBox = new VBox(5);
    filtersBox.getChildren().addAll(filtersListView, filtersToolBar);

    grid.add(new Label(GT._T("Filters:")), 0, 6);
    grid.add(filtersBox, 1, 6);
    return filtersListView;
  }

  public static Optional<RecentChangesOptions> showDialog(
      final Window owner,
      final JavaFxImageLoader imageLoader,
      final List<Namespace> availableNamespaces,
      final List<Tag> availableTags,
      final RecentChangesOptions initialOptions) {
    final RecentChangesOptionsDialog dialog =
        new RecentChangesOptionsDialog(
            owner, imageLoader, availableNamespaces, availableTags, initialOptions);
    return dialog.showAndWait();
  }
}
