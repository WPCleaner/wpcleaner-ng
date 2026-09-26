package org.wpcleaner.application.gui.javafx.recentchanges.options;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.jspecify.annotations.Nullable;
import org.wpcleaner.api.api.query.list.tags.Tag;
import org.wpcleaner.api.repository.namespace.Namespace;
import org.wpcleaner.api.utils.GT;
import org.wpcleaner.application.gui.javafx.JavaFxImageLoader;
import org.wpcleaner.application.gui.javafx.core.control.NamespaceCheckComboBox;
import org.wpcleaner.application.gui.javafx.core.control.TagCheckComboBox;
import org.wpcleaner.application.gui.javafx.core.window.JavaFxWindow;
import org.wpcleaner.application.gui.javafx.recentchanges.JavaFxRecentChangesWindowServices;

@SuppressWarnings("PMD.CouplingBetweenObjects")
public final class RecentChangesFilterWindow
    extends JavaFxWindow<JavaFxRecentChangesWindowServices> {

  private final TextField nameField;
  private final NamespaceCheckComboBox namespaceCheckComboBox;
  private final TagCheckComboBox tagCheckComboBox;
  private final FilterTypeCheckComboBox typeCheckComboBox;
  private final ComboBox<@Nullable Severity> severityComboBox;
  private final ToggleGroup subPagesGroup;
  private final HBox subPagesBox;
  private final Consumer<RecentChangesFilter> onFilterValidated;

  public RecentChangesFilterWindow(
      final JavaFxRecentChangesWindowServices services,
      final Stage owner,
      @Nullable final RecentChangesFilter initialFilter,
      final Consumer<RecentChangesFilter> onFilterValidated) {
    super(services, owner);
    stage.initModality(Modality.WINDOW_MODAL);
    this.onFilterValidated = Objects.requireNonNull(onFilterValidated);
    stage.setTitle(GT._T("Recent changes filter"));

    final List<Namespace> availableNamespaces = services.namespaceRepository().getNamespaces();
    final List<Tag> availableTags = services.tagRepository().getTags();

    nameField = createNameField(initialFilter);
    namespaceCheckComboBox = createNamespaceCheckComboBox(availableNamespaces, initialFilter);
    tagCheckComboBox = createTagCheckComboBox(availableTags, initialFilter);
    typeCheckComboBox = createTypeCheckComboBox(initialFilter);
    severityComboBox = createSeverityComboBox(imageLoader, initialFilter);
    subPagesGroup = new ToggleGroup();
    subPagesBox = createSubPagesBox(subPagesGroup, initialFilter);

    initialize();
    stage.show();
  }

  @Override
  public String getName() {
    return "recentChangesFilter";
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

    grid.add(new Label(GT._T("Namespaces:")), 0, 1);
    grid.add(namespaceCheckComboBox, 1, 1);

    grid.add(new Label(GT._T("Tags:")), 0, 2);
    grid.add(tagCheckComboBox, 1, 2);

    grid.add(new Label(GT._T("Types:")), 0, 3);
    grid.add(typeCheckComboBox, 1, 3);

    grid.add(new Label(GT._T("Severity:")), 0, 4);
    grid.add(severityComboBox, 1, 4);

    grid.add(new Label(GT._T("Sub-pages:")), 0, 5);
    grid.add(subPagesBox, 1, 5);

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
      showError(GT._T("Error"), GT._T("The filter name cannot be empty or blank."));
      return;
    }
    final String name = nameField.getText().trim();
    final Set<Integer> namespaceSet =
        Set.copyOf(
            namespaceCheckComboBox.getCheckModel().getCheckedItems().stream()
                .map(Namespace::id)
                .toList());
    final Set<String> tagSet =
        Set.copyOf(
            tagCheckComboBox.getCheckModel().getCheckedItems().stream().map(Tag::name).toList());
    final Set<RecentChangesFilter.Type> typeSet =
        Set.copyOf(typeCheckComboBox.getCheckModel().getCheckedItems().stream().toList());
    final Severity severity = severityComboBox.getSelectionModel().getSelectedItem();
    final RecentChangesFilter.SubPages subPages =
        subPagesGroup.getSelectedToggle() != null
            ? (RecentChangesFilter.SubPages) subPagesGroup.getSelectedToggle().getUserData()
            : RecentChangesFilter.SubPages.BOTH;
    final RecentChangesFilter filter =
        new RecentChangesFilter(name, namespaceSet, severity, tagSet, typeSet, subPages);
    onFilterValidated.accept(filter);
    stage.close();
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

  private FilterTypeCheckComboBox createTypeCheckComboBox(
      @Nullable final RecentChangesFilter initialFilter) {
    final FilterTypeCheckComboBox comboBox = new FilterTypeCheckComboBox();
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

  private HBox createSubPagesBox(
      final ToggleGroup group, @Nullable final RecentChangesFilter initialFilter) {
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
    return new HBox(10, bothRadio, topPagesRadio, subPagesRadio);
  }

  @Nullable Severity getSelectedSeverity() {
    return severityComboBox.getSelectionModel().getSelectedItem();
  }
}
