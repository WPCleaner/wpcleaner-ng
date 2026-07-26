package org.wpcleaner.application.gui.javafx.recentchanges;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.Tooltip;
import javafx.stage.Window;
import org.jspecify.annotations.Nullable;
import org.wpcleaner.api.utils.GT;
import org.wpcleaner.application.gui.javafx.JavaFxImageLoader;
import org.wpcleaner.application.gui.settings.recentchanges.RecentChangesSettings;
import org.wpcleaner.lib.image.ImageCollection;
import org.wpcleaner.lib.image.ImageSize;

public final class RecentChangesOptionsInput {

  private final ComboBox<RecentChangesOptions> comboBox;
  private final Button editOptions;
  private final Button addOptions;
  private final Button removeOptions;
  private final JavaFxImageLoader imageLoader;

  public RecentChangesOptionsInput(
      final Window owner,
      final JavaFxRecentChangesWindowServices services,
      final JavaFxImageLoader imageLoader) {
    this.imageLoader = imageLoader;
    this.comboBox = new ComboBox<>();
    this.comboBox.setCellFactory(
        _ ->
            new ListCell<>() {
              @Override
              protected void updateItem(final RecentChangesOptions item, final boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                  setText(null);
                } else {
                  setText(item.name());
                }
              }
            });
    this.comboBox.setButtonCell(
        new ListCell<>() {
          @Override
          protected void updateItem(final RecentChangesOptions item, final boolean empty) {
            super.updateItem(item, empty);
            if (empty) {
              setText(null);
            } else {
              setText(item.name());
            }
          }
        });
    this.comboBox.getItems().add(RecentChangesOptions.DEFAULT_OPTIONS);
    final RecentChangesSettings currentSettings =
        services.recentChangesSettingsManager().getCurrentSettings();
    currentSettings.options().forEach(this.comboBox.getItems()::add);

    final String lastSelectedName = currentSettings.selectedOption();
    RecentChangesOptions toSelect = RecentChangesOptions.DEFAULT_OPTIONS;
    if (lastSelectedName != null) {
      toSelect =
          this.comboBox.getItems().stream()
              .filter(option -> lastSelectedName.equals(option.name()))
              .findFirst()
              .orElse(RecentChangesOptions.DEFAULT_OPTIONS);
    }
    this.comboBox.getSelectionModel().select(toSelect);

    this.editOptions = new Button();
    editOptions.setStyle("-fx-background-color: transparent; -fx-padding: 1px;");
    imageLoader
        .getImageView(ImageCollection.EDIT, ImageSize.BUTTON)
        .ifPresent(editOptions::setGraphic);
    editOptions.setTooltip(new Tooltip(GT._T("Edit options")));
    editOptions.setOnAction(_ -> editOptionsAction(owner, services));

    this.addOptions = new Button();
    addOptions.setStyle("-fx-background-color: transparent; -fx-padding: 1px;");
    imageLoader
        .getImageView(ImageCollection.LIST_ADD, ImageSize.BUTTON)
        .ifPresent(addOptions::setGraphic);
    addOptions.setTooltip(new Tooltip(GT._T("Add options")));
    addOptions.setOnAction(_ -> addOptionsAction(owner, services));

    this.removeOptions = new Button();
    removeOptions.setStyle("-fx-background-color: transparent; -fx-padding: 1px;");
    imageLoader
        .getImageView(ImageCollection.LIST_REMOVE, ImageSize.BUTTON)
        .ifPresent(removeOptions::setGraphic);
    removeOptions.setTooltip(new Tooltip(GT._T("Remove options")));
    removeOptions.setOnAction(_ -> removeOptionsAction(owner, services));

    this.comboBox
        .getSelectionModel()
        .selectedItemProperty()
        .addListener(
            (_, _, _) -> {
              updateButtonsState();
              saveOptions(services);
            });
    updateButtonsState();
  }

  public ComboBox<@Nullable RecentChangesOptions> getComboBox() {
    return comboBox;
  }

  public Button getEditButton() {
    return editOptions;
  }

  public Button getAddButton() {
    return addOptions;
  }

  public Button getRemoveButton() {
    return removeOptions;
  }

  public RecentChangesOptions getSelectedOptions() {
    return comboBox.getSelectionModel().getSelectedItem();
  }

  private void addOptionsAction(
      final Window owner, final JavaFxRecentChangesWindowServices services) {
    final Optional<RecentChangesOptions> result =
        RecentChangesOptionsDialog.showDialog(
            owner,
            imageLoader,
            services.namespaceRepository().getNamespaces(),
            services.tagRepository().getTags(),
            getSelectedOptions());
    result.ifPresent(
        newOptions -> {
          comboBox.getItems().add(newOptions);
          comboBox.getSelectionModel().select(newOptions);
          saveOptions(services);
        });
  }

  private void editOptionsAction(
      final Window owner, final JavaFxRecentChangesWindowServices services) {
    final RecentChangesOptions selected = getSelectedOptions();
    if (Objects.equals(selected, RecentChangesOptions.DEFAULT_OPTIONS)) {
      showErrorAlert(owner, GT._T("The default options cannot be edited."));
      return;
    }
    final Optional<RecentChangesOptions> result =
        RecentChangesOptionsDialog.showDialog(
            owner,
            imageLoader,
            services.namespaceRepository().getNamespaces(),
            services.tagRepository().getTags(),
            selected);
    result.ifPresent(
        newOptions -> {
          final int index = comboBox.getSelectionModel().getSelectedIndex();
          if (index >= 0) {
            comboBox.getItems().remove(index);
            comboBox.getItems().add(index, newOptions);
            comboBox.getSelectionModel().select(index);
            saveOptions(services);
          }
        });
  }

  private void removeOptionsAction(
      final Window owner, final JavaFxRecentChangesWindowServices services) {
    final RecentChangesOptions selected = getSelectedOptions();
    if (Objects.equals(selected, RecentChangesOptions.DEFAULT_OPTIONS)) {
      showErrorAlert(owner, GT._T("The default options cannot be deleted."));
      return;
    }
    final Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
    alert.initOwner(owner);
    alert.setTitle(GT._T("Confirm deletion"));
    alert.setHeaderText(null);
    alert.setContentText(
        GT._T("Are you sure you want to delete the options \"%s\"?", selected.name()));
    final Optional<ButtonType> result = alert.showAndWait();
    if (result.isPresent() && result.get() == ButtonType.OK) {
      comboBox.getItems().remove(selected);
      saveOptions(services);
    }
  }

  private void saveOptions(final JavaFxRecentChangesWindowServices services) {
    final List<RecentChangesOptions> userOptions =
        comboBox.getItems().stream()
            .filter(options -> !Objects.equals(options, RecentChangesOptions.DEFAULT_OPTIONS))
            .toList();
    final RecentChangesOptions selected = getSelectedOptions();
    final RecentChangesSettings settings =
        new RecentChangesSettings(
            services.recentChangesSettingsManager().getCurrentSettings().version(),
            userOptions,
            selected.name());
    services.recentChangesSettingsManager().updateSettings(settings);
  }

  private void updateButtonsState() {
    final RecentChangesOptions selected = getSelectedOptions();
    final boolean isDefault = Objects.equals(selected, RecentChangesOptions.DEFAULT_OPTIONS);
    editOptions.setDisable(isDefault);
    removeOptions.setDisable(isDefault);
    if (isDefault) {
      editOptions.setTooltip(new Tooltip(GT._T("Default options cannot be edited")));
      removeOptions.setTooltip(new Tooltip(GT._T("Default options cannot be deleted")));
    } else {
      editOptions.setTooltip(new Tooltip(GT._T("Edit selected options")));
      removeOptions.setTooltip(new Tooltip(GT._T("Remove selected options")));
    }
  }

  private void showErrorAlert(final Window owner, final String message) {
    final Alert alert = new Alert(Alert.AlertType.ERROR);
    alert.initOwner(owner);
    alert.setTitle(GT._T("Error"));
    alert.setHeaderText(null);
    alert.setContentText(message);
    alert.showAndWait();
  }
}
