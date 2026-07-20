package org.wpcleaner.application.gui.swing.recentchanges;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import javax.swing.AbstractButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import org.wpcleaner.api.api.query.list.recentchanges.RecentChangesParameters;
import org.wpcleaner.application.gui.swing.core.component.FunctionListCellRenderer;
import org.wpcleaner.lib.image.ImageCollection;
import org.wpcleaner.lib.image.ImageSize;

public final class RecentChangesOptionsInput {

  public static final RecentChangesOptions DEFAULT_OPTIONS =
      new RecentChangesOptions(
          "Default options",
          Set.of(),
          Set.of(),
          null,
          Set.of(RecentChangesParameters.Type.EDIT, RecentChangesParameters.Type.NEW),
          false);

  private final JComboBox<RecentChangesOptions> comboBox;
  private final AbstractButton editOptions;
  private final AbstractButton addOptions;
  private final AbstractButton removeOptions;

  public RecentChangesOptionsInput(
      final JFrame owner, final SwingRecentChangesWindowServices services) {
    this.comboBox = new JComboBox<>();
    this.comboBox.setRenderer(new FunctionListCellRenderer<>(RecentChangesOptions::name));
    this.comboBox.addItem(DEFAULT_OPTIONS);

    this.editOptions =
        services
            .swing()
            .component()
            .buttons()
            .builder("Edit options", false)
            .withIcon(ImageCollection.EDIT, ImageSize.BUTTON)
            .withAction(() -> editOptionsAction(owner, services))
            .build();

    this.addOptions =
        services
            .swing()
            .component()
            .buttons()
            .builder("Add options", false)
            .withIcon(ImageCollection.LIST_ADD, ImageSize.BUTTON)
            .withAction(() -> addOptionsAction(owner, services))
            .build();

    this.removeOptions =
        services
            .swing()
            .component()
            .buttons()
            .builder("Remove options", false)
            .withIcon(ImageCollection.LIST_REMOVE, ImageSize.BUTTON)
            .withAction(() -> removeOptionsAction(owner))
            .build();

    this.comboBox.addActionListener(_ -> updateButtonsState());
    updateButtonsState();
  }

  public JComboBox<RecentChangesOptions> getComboBox() {
    return comboBox;
  }

  public AbstractButton getEditButton() {
    return editOptions;
  }

  public AbstractButton getAddButton() {
    return addOptions;
  }

  public AbstractButton getRemoveButton() {
    return removeOptions;
  }

  public RecentChangesOptions getSelectedOptions() {
    if (!(comboBox.getSelectedItem() instanceof RecentChangesOptions selected)) {
      return DEFAULT_OPTIONS;
    }
    return selected;
  }

  private void addOptionsAction(
      final JFrame owner, final SwingRecentChangesWindowServices services) {
    final Optional<RecentChangesOptions> result =
        RecentChangesOptionsDialog.showDialog(
            owner,
            services.swing(),
            services.namespaceRepository().getNamespaces(),
            services.tagRepository().getTags(),
            getSelectedOptions());
    result.ifPresent(
        newOptions -> {
          comboBox.addItem(newOptions);
          comboBox.setSelectedItem(newOptions);
        });
  }

  private void editOptionsAction(
      final JFrame owner, final SwingRecentChangesWindowServices services) {
    final RecentChangesOptions selected = getSelectedOptions();
    if (Objects.equals(selected, DEFAULT_OPTIONS)) {
      JOptionPane.showMessageDialog(
          owner, "The default options cannot be edited.", "Error", JOptionPane.ERROR_MESSAGE);
      return;
    }
    final Optional<RecentChangesOptions> result =
        RecentChangesOptionsDialog.showDialog(
            owner,
            services.swing(),
            services.namespaceRepository().getNamespaces(),
            services.tagRepository().getTags(),
            selected);
    result.ifPresent(
        newOptions -> {
          final int index = comboBox.getSelectedIndex();
          if (index >= 0) {
            comboBox.removeItemAt(index);
            comboBox.insertItemAt(newOptions, index);
            comboBox.setSelectedIndex(index);
          }
        });
  }

  private void removeOptionsAction(final JFrame owner) {
    final RecentChangesOptions selected = getSelectedOptions();
    if (Objects.equals(selected, DEFAULT_OPTIONS)) {
      JOptionPane.showMessageDialog(
          owner, "The default options cannot be deleted.", "Error", JOptionPane.ERROR_MESSAGE);
      return;
    }
    final int response =
        JOptionPane.showConfirmDialog(
            owner,
            "Are you sure you want to delete the options \"%s\"?".formatted(selected.name()),
            "Confirm deletion",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);
    if (response == JOptionPane.YES_OPTION) {
      comboBox.removeItem(selected);
    }
  }

  private void updateButtonsState() {
    final RecentChangesOptions selected = getSelectedOptions();
    final boolean isDefault = Objects.equals(selected, DEFAULT_OPTIONS);
    editOptions.setEnabled(!isDefault);
    removeOptions.setEnabled(!isDefault);
    if (isDefault) {
      editOptions.setToolTipText("Default options cannot be edited");
      removeOptions.setToolTipText("Default options cannot be deleted");
    } else {
      editOptions.setToolTipText("Edit selected options");
      removeOptions.setToolTipText("Remove selected options");
    }
  }
}
