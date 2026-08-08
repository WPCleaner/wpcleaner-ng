package org.wpcleaner.application.gui.javafx.login;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import java.util.Objects;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Window;
import org.jspecify.annotations.Nullable;
import org.wpcleaner.api.api.query.meta.siteinfo.ApiSiteInfo;
import org.wpcleaner.api.utils.GT;
import org.wpcleaner.api.wiki.builder.FandomBuilder;
import org.wpcleaner.api.wiki.builder.WikiBuilder;
import org.wpcleaner.api.wiki.builder.WikibooksBuilder;
import org.wpcleaner.api.wiki.builder.WikipediaBuilder;
import org.wpcleaner.api.wiki.builder.WikiquoteBuilder;
import org.wpcleaner.api.wiki.builder.WikisourceBuilder;
import org.wpcleaner.api.wiki.builder.WikiversityBuilder;
import org.wpcleaner.api.wiki.builder.WikivoyageBuilder;
import org.wpcleaner.api.wiki.builder.WiktionaryBuilder;
import org.wpcleaner.api.wiki.definition.WikiDefinition;
import org.wpcleaner.application.gui.javafx.JavaFxImageLoader;

final class WikiDefinitionDialog extends Dialog<@Nullable WikiDefinition> {

  private final ApiSiteInfo apiSiteInfo;
  private final ComboBox<WikiBuilderType> builderTypeComboBox;
  private final TextField nameField;
  private final TextField languageField;
  private final TextField subdomainField;
  private final TextField mainHostField;
  private final TextField apiPathField;
  private final TextField indexPathField;
  private final TextField wikiPathField;
  private final TextField codeField;

  public WikiDefinitionDialog(
      @Nullable final Window owner,
      final JavaFxImageLoader imageLoader,
      final ApiSiteInfo apiSiteInfo) {
    super();
    this.apiSiteInfo = apiSiteInfo;
    initOwner(owner);
    setTitle(GT._T("Add wiki"));

    builderTypeComboBox = new ComboBox<>();
    builderTypeComboBox.getItems().addAll(WikiBuilderType.values());
    builderTypeComboBox.getSelectionModel().select(WikiBuilderType.WIKIPEDIA);
    builderTypeComboBox.setCellFactory(_ -> new WikiBuilderTypeListCell(imageLoader));
    builderTypeComboBox.setButtonCell(new WikiBuilderTypeListCell(imageLoader));

    nameField = new TextField();
    languageField = new TextField();
    subdomainField = new TextField();
    mainHostField = new TextField();
    apiPathField = new TextField("/w/api.php");
    indexPathField = new TextField("/w/index.php");
    wikiPathField = new TextField("/wiki");
    codeField = new TextField();

    setupLayout();

    final ButtonType okButtonType = new ButtonType(GT._T("OK"), ButtonBar.ButtonData.OK_DONE);
    final ButtonType cancelButtonType =
        new ButtonType(GT._T("Cancel"), ButtonBar.ButtonData.CANCEL_CLOSE);
    getDialogPane().getButtonTypes().addAll(okButtonType, cancelButtonType);

    setupValidation(okButtonType);
    setupResultConverter(okButtonType);
  }

  private void setupLayout() {
    final VBox content = new VBox(10);
    content.setPadding(new Insets(15, 15, 15, 15));
    content.setPrefWidth(400);

    content.getChildren().add(createRow(GT._T("Wiki Family:"), builderTypeComboBox));
    content.getChildren().add(createRow(GT._T("Name:"), nameField));
    content.getChildren().add(createRow(GT._T("Language:"), languageField));
    content.getChildren().add(createRow(GT._T("Subdomain:"), subdomainField));
    content.getChildren().add(createRow(GT._T("Main Host:"), mainHostField));
    content.getChildren().add(createRow(GT._T("API Path:"), apiPathField));
    content.getChildren().add(createRow(GT._T("Index Path:"), indexPathField));
    content.getChildren().add(createRow(GT._T("Wiki Path:"), wikiPathField));
    content.getChildren().add(createRow(GT._T("Code:"), codeField));

    final BooleanBinding fandom =
        Bindings.createBooleanBinding(
            () -> builderTypeComboBox.getValue() == WikiBuilderType.FANDOM,
            builderTypeComboBox.valueProperty());

    final BooleanBinding generic =
        Bindings.createBooleanBinding(
            () -> builderTypeComboBox.getValue() == WikiBuilderType.GENERIC,
            builderTypeComboBox.valueProperty());

    subdomainField.visibleProperty().bind(fandom);
    subdomainField.managedProperty().bind(fandom);

    mainHostField.visibleProperty().bind(generic);
    mainHostField.managedProperty().bind(generic);

    apiPathField.visibleProperty().bind(generic);
    apiPathField.managedProperty().bind(generic);

    indexPathField.visibleProperty().bind(generic);
    indexPathField.managedProperty().bind(generic);

    wikiPathField.visibleProperty().bind(generic);
    wikiPathField.managedProperty().bind(generic);

    codeField.visibleProperty().bind(generic);
    codeField.managedProperty().bind(generic);

    builderTypeComboBox
        .valueProperty()
        .addListener(
            (_, _, _) ->
                Platform.runLater(
                    () -> {
                      if (getDialogPane().getScene() != null
                          && getDialogPane().getScene().getWindow() != null) {
                        getDialogPane().getScene().getWindow().sizeToScene();
                      }
                    }));

    getDialogPane().setContent(content);
  }

  @SuppressWarnings("PMD.AvoidCatchingGenericException")
  private void setupValidation(final ButtonType okButtonType) {
    final Button okButton = (Button) getDialogPane().lookupButton(okButtonType);
    okButton.addEventFilter(
        ActionEvent.ACTION,
        event -> {
          final String name = nameField.getText().trim();

          if (isInputInvalid(name)) {
            event.consume();
            return;
          }

          final WikiDefinition candidate = buildWikiDefinition();
          try {
            apiSiteInfo.requestSiteInfo(candidate, List.of(), null, null, null);
          } catch (final Exception e) {
            event.consume();
            final String message =
                Objects.requireNonNullElseGet(e.getMessage(), () -> e.getClass().getSimpleName());
            showError(GT._T("The wiki could not be reached or is invalid: %s", message));
          }
        });
  }

  private boolean isInputInvalid(final String name) {
    if (name.isEmpty()) {
      showError(GT._T("The name cannot be empty."));
      return true;
    }

    if (subdomainField.isVisible()) {
      final String subdomain = subdomainField.getText().trim();
      if (subdomain.isEmpty()) {
        showError(GT._T("The subdomain cannot be empty."));
        return true;
      }
    }
    if (mainHostField.isVisible()) {
      final String mainHost = mainHostField.getText().trim();
      if (mainHost.isEmpty()) {
        showError(GT._T("The main host cannot be empty."));
        return true;
      }
    }
    if (languageField.isVisible()) {
      final String language = languageField.getText().trim();
      if (language.isEmpty()) {
        showError(GT._T("The language cannot be empty."));
        return true;
      }
    }
    return false;
  }

  private void setupResultConverter(final ButtonType okButtonType) {
    setResultConverter(
        dialogButton -> {
          if (Objects.equals(dialogButton, okButtonType)) {
            return buildWikiDefinition();
          }
          return null;
        });
  }

  private WikiDefinition buildWikiDefinition() {
    final WikiBuilderType type = builderTypeComboBox.getValue();
    final String name = nameField.getText().trim();
    final String language = languageField.getText().trim();
    return switch (type) {
      case FANDOM -> buildFandom(name, language);
      case GENERIC -> buildGeneric(name, language);
      default -> buildWikimedia(type, name, language);
    };
  }

  private WikiDefinition buildFandom(final String name, final String language) {
    final String subdomain = subdomainField.getText().trim();
    if (language.isEmpty()) {
      return FandomBuilder.build(subdomain, name);
    } else {
      return FandomBuilder.build(subdomain, language, name);
    }
  }

  private WikiDefinition buildGeneric(final String name, final String language) {
    final String mainHost = mainHostField.getText().trim();
    final String apiPath = apiPathField.getText().trim();
    final String indexPath = indexPathField.getText().trim();
    final String wikiPath = wikiPathField.getText().trim();
    final String codeInput = codeField.getText().trim();

    final WikiBuilder builder = WikiBuilder.ltr(language, name, mainHost);
    if (!apiPath.isEmpty()) {
      builder.withApiPath(apiPath);
    }
    if (!indexPath.isEmpty()) {
      builder.withIndexPath(indexPath);
    }
    if (!wikiPath.isEmpty()) {
      builder.withWikiPath(wikiPath);
    }
    if (!codeInput.isEmpty()) {
      builder.withCode(codeInput);
    }
    return builder.build();
  }

  private WikiDefinition buildWikimedia(
      final WikiBuilderType type, final String name, final String language) {
    return switch (type) {
      case WIKIPEDIA -> WikipediaBuilder.ltr(language, name);
      case WIKIBOOKS -> WikibooksBuilder.ltr(language, name);
      case WIKIQUOTE -> WikiquoteBuilder.ltr(language, name);
      case WIKISOURCE -> WikisourceBuilder.ltr(language, name);
      case WIKIVERSITY -> WikiversityBuilder.ltr(language, name);
      case WIKIVOYAGE -> WikivoyageBuilder.ltr(language, name);
      case WIKTIONARY -> WiktionaryBuilder.ltr(language, name);
      default -> throw new IllegalStateException("Unexpected type: " + type);
    };
  }

  private HBox createRow(final String labelText, final Control control) {
    final Label label = new Label(labelText);
    label.setPrefWidth(120);
    label.setMinWidth(120);
    final HBox row = new HBox(10, label, control);
    HBox.setHgrow(control, Priority.ALWAYS);
    row.visibleProperty().bind(control.visibleProperty());
    row.managedProperty().bind(control.managedProperty());
    return row;
  }

  private void showError(final String message) {
    final Alert alert = new Alert(Alert.AlertType.ERROR);
    alert.initOwner(getDialogPane().getScene().getWindow());
    alert.setTitle(GT._T("Error"));
    alert.setHeaderText(null);
    alert.setContentText(message);
    alert.showAndWait();
  }
}
