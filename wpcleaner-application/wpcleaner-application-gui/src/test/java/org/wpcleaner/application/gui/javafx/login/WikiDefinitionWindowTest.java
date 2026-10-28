package org.wpcleaner.application.gui.javafx.login;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.wpcleaner.api.api.query.meta.siteinfo.ApiSiteInfo;
import org.wpcleaner.api.api.query.meta.siteinfo.SiteInfo;
import org.wpcleaner.api.wiki.definition.KnownDefinitions;
import org.wpcleaner.api.wiki.definition.WikiDefinition;
import org.wpcleaner.application.gui.javafx.JavaFxTest;
import org.wpcleaner.application.gui.javafx.JavaFxWindowsRegistry;
import org.wpcleaner.application.gui.javafx.core.action.JavaFxActionServices;
import org.wpcleaner.lib.image.ImageLoader;

class WikiDefinitionWindowTest extends JavaFxTest {

  @DisplayName("WikiDefinitionWindow initializes correctly")
  @Test
  void testWindowInitialization()
      throws InterruptedException, ExecutionException, TimeoutException {
    runOnJavaFx(
        () -> {
          final JavaFxLoginWindowServices services =
              BDDMockito.mock(JavaFxLoginWindowServices.class);
          final ImageLoader imageLoader = BDDMockito.mock(ImageLoader.class);
          final JavaFxActionServices actionServices = BDDMockito.mock(JavaFxActionServices.class);
          final JavaFxWindowsRegistry windowsRegistry =
              BDDMockito.mock(JavaFxWindowsRegistry.class);
          final ApiSiteInfo apiSiteInfo = BDDMockito.mock(ApiSiteInfo.class);
          final KnownDefinitions knownDefinitions = BDDMockito.mock(KnownDefinitions.class);

          BDDMockito.given(services.imageLoader()).willReturn(imageLoader);
          BDDMockito.given(services.actionServices()).willReturn(actionServices);
          BDDMockito.given(services.windowsRegistry()).willReturn(windowsRegistry);
          BDDMockito.given(services.apiSiteInfo()).willReturn(apiSiteInfo);
          BDDMockito.given(services.knownDefinitions()).willReturn(knownDefinitions);

          final AtomicBoolean added = new AtomicBoolean(false);
          final WikiDefinitionWindow window =
              new WikiDefinitionWindow(services, null, _ -> added.set(true));

          Assertions.assertThat(window.getStage().getTitle()).isEqualTo("Add wiki");
          Assertions.assertThat(window.getName()).isEqualTo("wikiDefinition");
          Assertions.assertThat(window.getStage().getScene()).isNotNull();
          Assertions.assertThat(window.getStage().isShowing()).isTrue();

          final Parent root = window.getStage().getScene().getRoot();
          Assertions.assertThat(root).isInstanceOf(StackPane.class);

          final StackPane stackPane = (StackPane) root;
          final VBox mainContainer = (VBox) stackPane.getChildren().getFirst();
          final HBox buttons = (HBox) mainContainer.getChildren().get(1);
          final Button cancelButton = (Button) buttons.getChildren().get(1);

          Assertions.assertThat(cancelButton.getText()).isEqualTo("Cancel");
          Assertions.assertThat(added).isFalse();

          window.getStage().close();
        });
  }

  @DisplayName("WikiDefinitionWindow is modal relative to JavaFxLoginWindow")
  @Test
  void testWindowModalRelativelyToLoginWindow()
      throws InterruptedException, ExecutionException, TimeoutException {
    runOnJavaFx(
        () -> {
          final JavaFxLoginWindowServices services =
              BDDMockito.mock(JavaFxLoginWindowServices.class);
          final ImageLoader imageLoader = BDDMockito.mock(ImageLoader.class);
          final JavaFxActionServices actionServices = BDDMockito.mock(JavaFxActionServices.class);
          final JavaFxWindowsRegistry windowsRegistry =
              BDDMockito.mock(JavaFxWindowsRegistry.class);
          final ApiSiteInfo apiSiteInfo = BDDMockito.mock(ApiSiteInfo.class);
          final KnownDefinitions knownDefinitions = BDDMockito.mock(KnownDefinitions.class);

          BDDMockito.given(services.imageLoader()).willReturn(imageLoader);
          BDDMockito.given(services.actionServices()).willReturn(actionServices);
          BDDMockito.given(services.windowsRegistry()).willReturn(windowsRegistry);
          BDDMockito.given(services.apiSiteInfo()).willReturn(apiSiteInfo);
          BDDMockito.given(services.knownDefinitions()).willReturn(knownDefinitions);

          final Stage ownerStage = new Stage();
          final WikiDefinitionWindow window =
              new WikiDefinitionWindow(services, ownerStage, _ -> {});

          Assertions.assertThat(window.getStage().getModality()).isEqualTo(Modality.WINDOW_MODAL);
          Assertions.assertThat(window.getStage().getOwner()).isEqualTo(ownerStage);

          window.getStage().close();
          ownerStage.close();
        });
  }

  @DisplayName("handleOk validates wiki candidate via ValidateWikiTask and adds it on success")
  @Test
  void testHandleOkValidatesWiki()
      throws InterruptedException, ExecutionException, TimeoutException {
    final CompletableFuture<WikiDefinition> addedFuture = new CompletableFuture<>();

    runOnJavaFx(
        () -> {
          final JavaFxLoginWindowServices services =
              BDDMockito.mock(JavaFxLoginWindowServices.class);
          final ImageLoader imageLoader = BDDMockito.mock(ImageLoader.class);
          final JavaFxActionServices actionServices = BDDMockito.mock(JavaFxActionServices.class);
          final JavaFxWindowsRegistry windowsRegistry =
              BDDMockito.mock(JavaFxWindowsRegistry.class);
          final ApiSiteInfo apiSiteInfo = BDDMockito.mock(ApiSiteInfo.class);
          final KnownDefinitions knownDefinitions = BDDMockito.mock(KnownDefinitions.class);

          BDDMockito.given(services.imageLoader()).willReturn(imageLoader);
          BDDMockito.given(services.actionServices()).willReturn(actionServices);
          BDDMockito.given(services.windowsRegistry()).willReturn(windowsRegistry);
          BDDMockito.given(services.apiSiteInfo()).willReturn(apiSiteInfo);
          BDDMockito.given(services.knownDefinitions()).willReturn(knownDefinitions);

          final SiteInfo siteInfo = BDDMockito.mock(SiteInfo.class);
          BDDMockito.given(
                  apiSiteInfo.requestSiteInfo(
                      BDDMockito.any(),
                      BDDMockito.any(),
                      BDDMockito.isNull(),
                      BDDMockito.isNull(),
                      BDDMockito.isNull()))
              .willReturn(siteInfo);

          final WikiDefinitionWindow window =
              new WikiDefinitionWindow(services, null, addedFuture::complete);

          final StackPane stackPane = (StackPane) window.getStage().getScene().getRoot();
          final VBox mainContainer = (VBox) stackPane.getChildren().getFirst();
          final VBox form = (VBox) mainContainer.getChildren().get(0);
          final HBox nameRow = (HBox) form.getChildren().get(1);
          final TextField nameField = (TextField) nameRow.getChildren().get(1);
          nameField.setText("CustomWiki");

          final HBox langRow = (HBox) form.getChildren().get(2);
          final TextField langField = (TextField) langRow.getChildren().get(1);
          langField.setText("fr");

          final HBox buttons = (HBox) mainContainer.getChildren().get(1);
          final Button okButton = (Button) buttons.getChildren().get(0);

          okButton.fire();
        });

    final WikiDefinition added = addedFuture.get(5, TimeUnit.SECONDS);
    Assertions.assertThat(added).isNotNull();
    Assertions.assertThat(added.name()).isEqualTo("CustomWiki");
  }

  @DisplayName("handleOk adds a Fandom wiki without language")
  @Test
  void testHandleOkAddsFandomWikiWithoutLanguage()
      throws InterruptedException, ExecutionException, TimeoutException {
    final CompletableFuture<WikiDefinition> addedFuture = new CompletableFuture<>();

    runOnJavaFx(
        () -> {
          final JavaFxLoginWindowServices services =
              BDDMockito.mock(JavaFxLoginWindowServices.class);
          final ApiSiteInfo apiSiteInfo = BDDMockito.mock(ApiSiteInfo.class);

          BDDMockito.given(services.imageLoader()).willReturn(BDDMockito.mock(ImageLoader.class));
          BDDMockito.given(services.actionServices())
              .willReturn(BDDMockito.mock(JavaFxActionServices.class));
          BDDMockito.given(services.windowsRegistry())
              .willReturn(BDDMockito.mock(JavaFxWindowsRegistry.class));
          BDDMockito.given(services.apiSiteInfo()).willReturn(apiSiteInfo);
          BDDMockito.given(services.knownDefinitions())
              .willReturn(BDDMockito.mock(KnownDefinitions.class));
          BDDMockito.given(
                  apiSiteInfo.requestSiteInfo(
                      BDDMockito.any(),
                      BDDMockito.any(),
                      BDDMockito.isNull(),
                      BDDMockito.isNull(),
                      BDDMockito.isNull()))
              .willReturn(BDDMockito.mock(SiteInfo.class));

          final WikiDefinitionWindow window =
              new WikiDefinitionWindow(services, null, addedFuture::complete);

          final StackPane stackPane = (StackPane) window.getStage().getScene().getRoot();
          final VBox mainContainer = (VBox) stackPane.getChildren().getFirst();
          final VBox form = (VBox) mainContainer.getChildren().get(0);
          final HBox familyRow = (HBox) form.getChildren().get(0);
          @SuppressWarnings("unchecked")
          final ComboBox<WikiBuilderType> familyComboBox =
              (ComboBox<WikiBuilderType>) familyRow.getChildren().get(1);
          familyComboBox.getSelectionModel().select(WikiBuilderType.FANDOM);

          final HBox nameRow = (HBox) form.getChildren().get(1);
          final TextField nameField = (TextField) nameRow.getChildren().get(1);
          nameField.setText("Memory Alpha");

          final HBox subdomainRow = (HBox) form.getChildren().get(3);
          final TextField subdomainField = (TextField) subdomainRow.getChildren().get(1);
          subdomainField.setText("memory-alpha");

          final HBox buttons = (HBox) mainContainer.getChildren().get(1);
          final Button okButton = (Button) buttons.getChildren().get(0);

          okButton.fire();
        });

    final WikiDefinition added = addedFuture.get(5, TimeUnit.SECONDS);
    Assertions.assertThat(added.mainHost()).isEqualTo("memory-alpha.fandom.com");
    Assertions.assertThat(added.apiPath()).isEqualTo("/api.php");
    Assertions.assertThat(added.code()).isEqualTo("fandom:memory-alpha");
  }
}
