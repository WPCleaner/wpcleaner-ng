package org.wpcleaner.application.gui.javafx.recentchanges.options;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.jspecify.annotations.Nullable;
import org.wpcleaner.api.api.query.list.recentchanges.RecentChangesParameters;
import org.wpcleaner.api.api.query.list.tags.Tag;
import org.wpcleaner.api.repository.namespace.Namespace;
import org.wpcleaner.api.utils.GT;
import org.wpcleaner.application.gui.javafx.core.control.NamespaceCheckComboBox;
import org.wpcleaner.application.gui.javafx.core.window.JavaFxWindow;
import org.wpcleaner.application.gui.javafx.recentchanges.JavaFxRecentChangesWindowServices;

@SuppressWarnings("PMD.CouplingBetweenObjects")
public final class RecentChangesOptionsWindow
    extends JavaFxWindow<JavaFxRecentChangesWindowServices> {

  private final TextField nameField;
  private final NamespaceCheckComboBox namespaceComboBox;
  private final ShowCheckComboBox showComboBox;
  private final ComboBox<@Nullable String> tagField;
  private final TypeCheckComboBox typeComboBox;
  private final CheckBox topOnlyCheckbox;
  private final RecentChangesFilterListView filtersListView;
  private final ToolBar filtersToolBar;
  private final Consumer<RecentChangesOptions> onOptionsValidated;

  public RecentChangesOptionsWindow(
      final JavaFxRecentChangesWindowServices services,
      final Stage owner,
      final RecentChangesOptions initialOptions,
      final Consumer<RecentChangesOptions> onOptionsValidated) {
    super(services, owner);
    stage.initModality(Modality.WINDOW_MODAL);
    this.onOptionsValidated = Objects.requireNonNull(onOptionsValidated);
    stage.setTitle(GT._T("Recent changes options"));

    final List<Namespace> availableNamespaces = services.namespaceRepository().getNamespaces();
    final List<Tag> availableTags = services.tagRepository().getTags();

    nameField = createNameField(initialOptions);
    namespaceComboBox = createNamespaceComboBox(availableNamespaces, initialOptions);
    showComboBox = createShowComboBox(initialOptions);
    tagField = createTagComboBox(availableTags, initialOptions);
    typeComboBox = createTypeComboBox(initialOptions);
    topOnlyCheckbox = createTopOnlyCheckbox(initialOptions);

    filtersToolBar = new ToolBar();
    filtersListView = new RecentChangesFilterListView(services, stage, filtersToolBar);
    filtersListView.getItems().addAll(initialOptions.filters());

    initialize();
    stage.show();
  }

  @Override
  public String getName() {
    return "recentChangesOptions";
  }

  @Override
  protected Scene createScene() {
    final StackPane root = new StackPane();
    final VBox mainContainer = new VBox(15);
    mainContainer.setPadding(new Insets(15, 15, 15, 15));

    final GridPane grid = new GridPane();
    grid.setHgap(10);
    grid.setVgap(10);

    grid.add(new Label(GT._T("Name:")), 0, 0);
    grid.add(nameField, 1, 0);

    grid.add(new Label(GT._T("Namespace:")), 0, 1);
    grid.add(namespaceComboBox, 1, 1);

    grid.add(new Label(GT._T("Show:")), 0, 2);
    grid.add(showComboBox, 1, 2);

    grid.add(new Label(GT._T("Tag:")), 0, 3);
    grid.add(tagField, 1, 3);

    grid.add(new Label(GT._T("Type:")), 0, 4);
    grid.add(typeComboBox, 1, 4);

    grid.add(new Label(GT._T("Top only:")), 0, 5);
    grid.add(topOnlyCheckbox, 1, 5);

    final VBox filtersBox = new VBox(5, filtersListView, filtersToolBar);
    grid.add(new Label(GT._T("Filters:")), 0, 6);
    grid.add(filtersBox, 1, 6);

    final Button okButton = new Button(GT._T("OK"));
    okButton.setDefaultButton(true);
    okButton.setOnAction(_ -> handleOk());

    final Button cancelButton = new Button(GT._T("Cancel"));
    cancelButton.setCancelButton(true);
    cancelButton.setOnAction(_ -> stage.close());

    final HBox buttons = new HBox(10, okButton, cancelButton);
    buttons.setAlignment(Pos.CENTER_RIGHT);

    mainContainer.getChildren().addAll(grid, buttons);

    grid.disableProperty().bind(loading);
    buttons.disableProperty().bind(loading);

    root.getChildren().addAll(mainContainer, progressTracker.getProgressOverlay());
    return new Scene(root);
  }

  private void handleOk() {
    if (nameField.getText().isBlank()) {
      showError(GT._T("Error"), GT._T("The option name cannot be empty or blank."));
      return;
    }
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
    final RecentChangesOptions options =
        new RecentChangesOptions(
            name, namespaceSet, showSet, tag, typeSet, topOnly, List.copyOf(filterList));
    onOptionsValidated.accept(options);
    stage.close();
  }

  private TextField createNameField(final RecentChangesOptions initialOptions) {
    final TextField field = new TextField();
    field.setPrefWidth(250);
    field.setText(initialOptions.name());
    return field;
  }

  private NamespaceCheckComboBox createNamespaceComboBox(
      final List<Namespace> availableNamespaces, final RecentChangesOptions initialOptions) {
    final NamespaceCheckComboBox comboBox = new NamespaceCheckComboBox();
    comboBox.setup(availableNamespaces, initialOptions.namespace());
    return comboBox;
  }

  private ShowCheckComboBox createShowComboBox(final RecentChangesOptions initialOptions) {
    final ShowCheckComboBox comboBox = new ShowCheckComboBox();
    comboBox.setup(initialOptions.show());
    return comboBox;
  }

  private ComboBox<@Nullable String> createTagComboBox(
      final List<Tag> availableTags, final RecentChangesOptions initialOptions) {
    final ComboBox<@Nullable String> comboBox = new ComboBox<>();
    final List<String> tagNames = new ArrayList<>();
    tagNames.add("");
    for (final Tag tag : availableTags) {
      tagNames.add(tag.name());
    }
    comboBox.getItems().addAll(tagNames);
    comboBox.getSelectionModel().select("");
    if (initialOptions.tag() != null) {
      comboBox.getSelectionModel().select(initialOptions.tag());
    }
    return comboBox;
  }

  private TypeCheckComboBox createTypeComboBox(final RecentChangesOptions initialOptions) {
    final TypeCheckComboBox comboBox = new TypeCheckComboBox();
    comboBox.setup(initialOptions.type());
    return comboBox;
  }

  private CheckBox createTopOnlyCheckbox(final RecentChangesOptions initialOptions) {
    final CheckBox checkBox = new CheckBox();
    checkBox.setSelected(initialOptions.topOnly());
    return checkBox;
  }
}
