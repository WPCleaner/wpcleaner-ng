package org.wpcleaner.application.gui.javafx.recentchanges;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import java.util.Optional;
import java.util.Set;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Window;
import org.jspecify.annotations.Nullable;
import org.wpcleaner.api.api.query.list.recentchanges.RecentChangesParameters;
import org.wpcleaner.api.api.query.list.tags.Tag;
import org.wpcleaner.api.repository.namespace.Namespace;
import org.wpcleaner.api.utils.GT;
import org.wpcleaner.application.gui.javafx.JavaFxImageLoader;
import org.wpcleaner.application.gui.javafx.core.control.NamespaceCheckComboBox;
import org.wpcleaner.application.gui.javafx.core.control.TagCheckComboBox;

@SuppressWarnings("PMD.CouplingBetweenObjects")
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
    setTitle(GT._T("Recent changes filter"));

    final GridPane grid = new GridPane();
    grid.setHgap(10);
    grid.setVgap(10);
    grid.setPadding(new Insets(15, 15, 15, 15));

    nameField = createNameField(initialFilter);
    grid.add(new Label(GT._T("Name:")), 0, 0);
    grid.add(nameField, 1, 0);

    namespaceCheckComboBox = createNamespaceCheckComboBox(availableNamespaces, initialFilter);
    grid.add(new Label(GT._T("Namespaces:")), 0, 1);
    grid.add(namespaceCheckComboBox, 1, 1);

    tagCheckComboBox = createTagCheckComboBox(availableTags, initialFilter);
    grid.add(new Label(GT._T("Tags:")), 0, 2);
    grid.add(tagCheckComboBox, 1, 2);

    typeCheckComboBox = createTypeCheckComboBox(initialFilter);
    grid.add(new Label(GT._T("Types:")), 0, 3);
    grid.add(typeCheckComboBox, 1, 3);

    severityComboBox = createSeverityComboBox(imageLoader, initialFilter);
    grid.add(new Label(GT._T("Severity:")), 0, 4);
    grid.add(severityComboBox, 1, 4);

    final ToggleGroup subPagesGroup = setupSubPagesGroup(grid, initialFilter);

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
            alert.setContentText(GT._T("The filter name cannot be empty or blank."));
            alert.showAndWait();
          }
        });

    setResultConverter(
        dialogButton -> {
          if (okButtonType.equals(dialogButton)) {
            final String name = nameField.getText().trim();
            final Set<Integer> namespaceSet =
                Set.copyOf(
                    namespaceCheckComboBox.getCheckModel().getCheckedItems().stream()
                        .map(Namespace::id)
                        .toList());
            final Set<String> tagSet =
                Set.copyOf(
                    tagCheckComboBox.getCheckModel().getCheckedItems().stream()
                        .map(Tag::name)
                        .toList());
            final Set<RecentChangesParameters.Type> typeSet =
                Set.copyOf(typeCheckComboBox.getCheckModel().getCheckedItems().stream().toList());
            final Severity severity = severityComboBox.getSelectionModel().getSelectedItem();
            final RecentChangesFilter.SubPages subPages =
                subPagesGroup.getSelectedToggle() != null
                    ? (RecentChangesFilter.SubPages) subPagesGroup.getSelectedToggle().getUserData()
                    : RecentChangesFilter.SubPages.BOTH;
            return new RecentChangesFilter(name, namespaceSet, severity, tagSet, typeSet, subPages);
          }
          return null;
        });
  }

  private TextField createNameField(@Nullable final RecentChangesFilter initialFilter) {
    final TextField field = new TextField();
    field.setPrefWidth(250);
    if (initialFilter != null) {
      field.setText(initialFilter.name());
    }
    return field;
  }

  private NamespaceCheckComboBox createNamespaceCheckComboBox(
      final List<Namespace> availableNamespaces,
      @Nullable final RecentChangesFilter initialFilter) {
    final NamespaceCheckComboBox comboBox = new NamespaceCheckComboBox();
    comboBox.setup(
        availableNamespaces, initialFilter != null ? initialFilter.namespace() : Set.of());
    return comboBox;
  }

  private TagCheckComboBox createTagCheckComboBox(
      final List<Tag> availableTags, @Nullable final RecentChangesFilter initialFilter) {
    final TagCheckComboBox comboBox = new TagCheckComboBox();
    comboBox.setup(availableTags, initialFilter != null ? initialFilter.tag() : null);
    return comboBox;
  }

  private TypeCheckComboBox createTypeCheckComboBox(
      @Nullable final RecentChangesFilter initialFilter) {
    final TypeCheckComboBox comboBox = new TypeCheckComboBox();
    comboBox.setup(initialFilter != null ? initialFilter.type() : Set.of());
    return comboBox;
  }

  private ComboBox<@Nullable Severity> createSeverityComboBox(
      final JavaFxImageLoader imageLoader, @Nullable final RecentChangesFilter initialFilter) {
    final ComboBox<@Nullable Severity> comboBox = new ComboBox<>();
    comboBox.getItems().add(null);
    comboBox.getItems().addAll(Severity.values());
    comboBox.setCellFactory(_ -> new SeverityListCell(imageLoader));
    comboBox.setButtonCell(new SeverityListCell(imageLoader));
    comboBox.setMaxWidth(Double.MAX_VALUE);
    if (initialFilter != null) {
      comboBox.getSelectionModel().select(initialFilter.severity());
    } else {
      comboBox.getSelectionModel().select(null);
    }
    return comboBox;
  }

  private ToggleGroup setupSubPagesGroup(
      final GridPane grid, @Nullable final RecentChangesFilter initialFilter) {
    final ToggleGroup group = new ToggleGroup();

    final RadioButton bothRadio = new RadioButton(GT._T("Both"));
    bothRadio.setToggleGroup(group);
    bothRadio.setUserData(RecentChangesFilter.SubPages.BOTH);

    final RadioButton topPagesRadio = new RadioButton(GT._T("Top pages"));
    topPagesRadio.setToggleGroup(group);
    topPagesRadio.setUserData(RecentChangesFilter.SubPages.TOP_PAGES);

    final RadioButton subPagesRadio = new RadioButton(GT._T("Sub-pages"));
    subPagesRadio.setToggleGroup(group);
    subPagesRadio.setUserData(RecentChangesFilter.SubPages.SUB_PAGES);

    final RecentChangesFilter.SubPages initialSubPages =
        initialFilter != null ? initialFilter.subPages() : RecentChangesFilter.SubPages.BOTH;
    switch (initialSubPages) {
      case BOTH -> bothRadio.setSelected(true);
      case TOP_PAGES -> topPagesRadio.setSelected(true);
      case SUB_PAGES -> subPagesRadio.setSelected(true);
    }

    grid.add(new Label(GT._T("Sub-pages:")), 0, 5);
    grid.add(new HBox(bothRadio, topPagesRadio, subPagesRadio), 1, 5);
    return group;
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
