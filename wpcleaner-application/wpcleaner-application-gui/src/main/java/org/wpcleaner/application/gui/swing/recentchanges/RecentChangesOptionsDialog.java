package org.wpcleaner.application.gui.swing.recentchanges;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.Serial;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import org.jspecify.annotations.Nullable;
import org.wpcleaner.api.api.query.list.recentchanges.RecentChangesParameters;
import org.wpcleaner.api.repository.namespace.Namespace;
import org.wpcleaner.application.gui.swing.core.SwingCoreServices;
import org.wpcleaner.application.gui.swing.core.component.FunctionListCellRenderer;
import org.wpcleaner.application.gui.swing.core.layout.GridBagComponent;

public final class RecentChangesOptionsDialog extends JDialog {

  @Serial private static final long serialVersionUID = 1L;

  private final JTextField nameField;
  private final JList<Namespace> namespaceList;
  private final JList<RecentChangesParameters.Show> showList;
  private final JTextField tagField;
  private final JList<RecentChangesParameters.Type> typeList;
  private final JCheckBox topOnlyCheckbox;
  private boolean confirmed;

  private RecentChangesOptionsDialog(
      final Frame owner,
      final SwingCoreServices swing,
      final List<Namespace> availableNamespaces,
      @Nullable final RecentChangesOptions initialOptions) {
    super(owner, "Recent changes options", true);
    this.confirmed = false;

    final JPanel panel = new JPanel(new GridBagLayout());
    final GridBagConstraints constraints = swing.layout().initializeConstraints();

    nameField = new JTextField(20);
    if (initialOptions != null) {
      nameField.setText(initialOptions.name());
    }
    swing
        .layout()
        .addRow(
            panel,
            constraints,
            GridBagComponent.of(new JLabel("Name:")),
            GridBagComponent.of(nameField));

    namespaceList = new JList<>(availableNamespaces.toArray(new Namespace[0]));
    namespaceList.setCellRenderer(new FunctionListCellRenderer<>(Namespace::name));
    namespaceList.setVisibleRowCount(5);
    initNamespaceList(availableNamespaces, initialOptions);
    final JScrollPane namespaceScroll = new JScrollPane(namespaceList);
    swing
        .layout()
        .addRow(
            panel,
            constraints,
            GridBagComponent.of(new JLabel("Namespace:")),
            GridBagComponent.of(namespaceScroll));

    showList = new JList<>(RecentChangesParameters.Show.values());
    showList.setCellRenderer(new FunctionListCellRenderer<>(show -> show.value));
    showList.setVisibleRowCount(5);
    initShowList(initialOptions);
    final JScrollPane showScroll = new JScrollPane(showList);
    swing
        .layout()
        .addRow(
            panel,
            constraints,
            GridBagComponent.of(new JLabel("Show:")),
            GridBagComponent.of(showScroll));

    tagField = new JTextField(20);
    if (initialOptions != null && initialOptions.tag() != null) {
      tagField.setText(initialOptions.tag());
    }
    swing
        .layout()
        .addRow(
            panel,
            constraints,
            GridBagComponent.of(new JLabel("Tag:")),
            GridBagComponent.of(tagField));

    typeList = new JList<>(RecentChangesParameters.Type.values());
    typeList.setCellRenderer(new FunctionListCellRenderer<>(type -> type.value));
    typeList.setVisibleRowCount(5);
    initTypeList(initialOptions);
    final JScrollPane typeScroll = new JScrollPane(typeList);
    swing
        .layout()
        .addRow(
            panel,
            constraints,
            GridBagComponent.of(new JLabel("Type:")),
            GridBagComponent.of(typeScroll));

    topOnlyCheckbox = new JCheckBox();
    if (initialOptions != null) {
      topOnlyCheckbox.setSelected(initialOptions.topOnly());
    }
    swing
        .layout()
        .addRow(
            panel,
            constraints,
            GridBagComponent.of(new JLabel("Top only:")),
            GridBagComponent.of(topOnlyCheckbox));

    final JPanel buttonsPanel = new JPanel();
    buttonsPanel.add(
        swing.component().buttons().builder("OK", true).withAction(this::onOk).build());
    buttonsPanel.add(
        swing.component().buttons().builder("Cancel", true).withAction(this::dispose).build());
    swing.layout().addRowSpanningAllColumns(panel, constraints, buttonsPanel);

    getContentPane().add(panel);
    pack();
    setLocationRelativeTo(owner);
  }

  private void initNamespaceList(
      final List<Namespace> availableNamespaces,
      @Nullable final RecentChangesOptions initialOptions) {
    if (initialOptions != null) {
      final List<Integer> namespaceIndices = new ArrayList<>();
      for (int i = 0; i < availableNamespaces.size(); i++) {
        if (initialOptions.namespace().contains(availableNamespaces.get(i).id())) {
          namespaceIndices.add(i);
        }
      }
      namespaceList.setSelectedIndices(
          namespaceIndices.stream().mapToInt(Integer::intValue).toArray());
    }
  }

  private void initShowList(@Nullable final RecentChangesOptions initialOptions) {
    if (initialOptions != null) {
      final RecentChangesParameters.Show[] showValues = RecentChangesParameters.Show.values();
      final List<Integer> showIndices = new ArrayList<>();
      for (int i = 0; i < showValues.length; i++) {
        if (initialOptions.show().contains(showValues[i])) {
          showIndices.add(i);
        }
      }
      showList.setSelectedIndices(showIndices.stream().mapToInt(Integer::intValue).toArray());
    }
  }

  private void initTypeList(@Nullable final RecentChangesOptions initialOptions) {
    if (initialOptions != null) {
      final RecentChangesParameters.Type[] typeValues = RecentChangesParameters.Type.values();
      final List<Integer> typeIndices = new ArrayList<>();
      for (int i = 0; i < typeValues.length; i++) {
        if (initialOptions.type().contains(typeValues[i])) {
          typeIndices.add(i);
        }
      }
      typeList.setSelectedIndices(typeIndices.stream().mapToInt(Integer::intValue).toArray());
    }
  }

  public static Optional<RecentChangesOptions> showDialog(
      final Frame owner,
      final SwingCoreServices swing,
      final List<Namespace> availableNamespaces,
      @Nullable final RecentChangesOptions initialOptions) {
    final RecentChangesOptionsDialog dialog =
        new RecentChangesOptionsDialog(owner, swing, availableNamespaces, initialOptions);
    dialog.setVisible(true);
    return dialog.getOptions();
  }

  private void onOk() {
    final String name = nameField.getText();
    if (name.isBlank()) {
      JOptionPane.showMessageDialog(
          this, "The option name cannot be empty or blank.", "Error", JOptionPane.ERROR_MESSAGE);
      return;
    }
    confirmed = true;
    dispose();
  }

  private Optional<RecentChangesOptions> getOptions() {
    if (!confirmed) {
      return Optional.empty();
    }
    final String name = nameField.getText().trim();
    final Set<Integer> namespaceSet =
        Set.copyOf(namespaceList.getSelectedValuesList().stream().map(Namespace::id).toList());
    final Set<RecentChangesParameters.Show> showSet = Set.copyOf(showList.getSelectedValuesList());
    final String tagText = tagField.getText().trim();
    final String tag = tagText.isEmpty() ? null : tagText;
    final Set<RecentChangesParameters.Type> typeSet = Set.copyOf(typeList.getSelectedValuesList());
    final boolean topOnly = topOnlyCheckbox.isSelected();
    return Optional.of(
        new RecentChangesOptions(name, namespaceSet, showSet, tag, typeSet, topOnly));
  }
}
