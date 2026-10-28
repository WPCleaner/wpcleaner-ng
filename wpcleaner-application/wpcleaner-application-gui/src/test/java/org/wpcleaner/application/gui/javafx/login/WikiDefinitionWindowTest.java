package org.wpcleaner.application.gui.javafx.login;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.wpcleaner.api.api.query.meta.siteinfo.MockApiSiteInfo;
import org.wpcleaner.api.api.query.meta.siteinfo.SiteInfo;
import org.wpcleaner.api.wiki.builder.FandomBuilder;
import org.wpcleaner.api.wiki.builder.WikipediaBuilder;
import org.wpcleaner.api.wiki.definition.KnownDefinitions;
import org.wpcleaner.api.wiki.definition.WikiDefinition;
import org.wpcleaner.application.gui.GuiSpringBootTest;
import org.wpcleaner.application.gui.javafx.JavaFxTest;
import org.wpcleaner.application.gui.javafx.JavaFxWindowsRegistry;

@GuiSpringBootTest
class WikiDefinitionWindowTest extends JavaFxTest {

  private static final WikiDefinition CUSTOM_WIKI = WikipediaBuilder.ltr("fr", "CustomWiki");
  private static final WikiDefinition FANDOM_WIKI =
      FandomBuilder.build("memory-alpha", "Memory Alpha");

  @Autowired private JavaFxLoginWindowServices services;
  @Autowired private JavaFxWindowsRegistry windowsRegistry;
  @Autowired private KnownDefinitions knownDefinitions;
  @Autowired private MockApiSiteInfo mockApiSiteInfo;

  @AfterEach
  void tearDown() throws InterruptedException, ExecutionException, TimeoutException {
    runOnJavaFx(windowsRegistry::closeAllWindows);
    knownDefinitions.removeDefinition(CUSTOM_WIKI);
    knownDefinitions.removeDefinition(FANDOM_WIKI);
  }

  @DisplayName("WikiDefinitionWindow initializes correctly")
  @Test
  void windowInitialization() throws InterruptedException, ExecutionException, TimeoutException {
    runOnJavaFx(
        () -> {
          final AtomicBoolean added = new AtomicBoolean(false);
          final WikiDefinitionWindow window =
              new WikiDefinitionWindow(services, null, _ -> added.set(true));

          Assertions.assertThat(window.getStage().getTitle()).isEqualTo("Add wiki");
          Assertions.assertThat(window.getName()).isEqualTo("wikiDefinition");
          Assertions.assertThat(window.getStage().getScene()).isNotNull();
          Assertions.assertThat(window.getStage().isShowing()).isTrue();

          final Parent root = window.getStage().getScene().getRoot();
          Assertions.assertThat(root).isInstanceOf(StackPane.class);

          final Button cancelButton = lookup(window, WikiDefinitionWindow.CANCEL_ID, Button.class);

          Assertions.assertThat(cancelButton.getText()).isEqualTo("Cancel");
          Assertions.assertThat(added).isFalse();
        });
  }

  @DisplayName("WikiDefinitionWindow is modal relative to JavaFxLoginWindow")
  @Test
  void windowModalRelativelyToLoginWindow()
      throws InterruptedException, ExecutionException, TimeoutException {
    runOnJavaFx(
        () -> {
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
  void handleOkValidatesWiki() throws InterruptedException, ExecutionException, TimeoutException {
    mockApiSiteInfo
        .givenRequestSiteInfo(CUSTOM_WIKI, List.of(), null, null, null)
        .willReturn(SiteInfo.ofEmpty());
    final CompletableFuture<WikiDefinition> addedFuture = new CompletableFuture<>();

    runOnJavaFx(
        () -> {
          final WikiDefinitionWindow window =
              new WikiDefinitionWindow(services, null, addedFuture::complete);
          lookup(window, WikiDefinitionWindow.NAME_ID, TextField.class).setText("CustomWiki");
          lookup(window, WikiDefinitionWindow.LANGUAGE_ID, TextField.class).setText("fr");
          lookup(window, WikiDefinitionWindow.OK_ID, Button.class).fire();
        });

    final WikiDefinition added = addedFuture.get(5, TimeUnit.SECONDS);
    Assertions.assertThat(added).isEqualTo(CUSTOM_WIKI);
    Assertions.assertThat(knownDefinitions.getDefinitions()).contains(CUSTOM_WIKI);
    Assertions.assertThat(knownDefinitions.isUserAdded(CUSTOM_WIKI)).isTrue();
  }

  @DisplayName("handleOk adds a Fandom wiki without language")
  @Test
  void handleOkAddsFandomWikiWithoutLanguage()
      throws InterruptedException, ExecutionException, TimeoutException {
    mockApiSiteInfo
        .givenRequestSiteInfo(FANDOM_WIKI, List.of(), null, null, null)
        .willReturn(SiteInfo.ofEmpty());
    final CompletableFuture<WikiDefinition> addedFuture = new CompletableFuture<>();

    runOnJavaFx(
        () -> {
          final WikiDefinitionWindow window =
              new WikiDefinitionWindow(services, null, addedFuture::complete);
          @SuppressWarnings("unchecked")
          final ComboBox<WikiBuilderType> familyComboBox =
              lookup(window, WikiDefinitionWindow.FAMILY_ID, ComboBox.class);
          familyComboBox.getSelectionModel().select(WikiBuilderType.FANDOM);
          lookup(window, WikiDefinitionWindow.NAME_ID, TextField.class).setText("Memory Alpha");
          lookup(window, WikiDefinitionWindow.SUBDOMAIN_ID, TextField.class)
              .setText("memory-alpha");
          lookup(window, WikiDefinitionWindow.OK_ID, Button.class).fire();
        });

    final WikiDefinition added = addedFuture.get(5, TimeUnit.SECONDS);
    Assertions.assertThat(added.mainHost()).isEqualTo("memory-alpha.fandom.com");
    Assertions.assertThat(added.apiPath()).isEqualTo("/api.php");
    Assertions.assertThat(added.code()).isEqualTo("fandom:memory-alpha");
  }

  @DisplayName("handleOk shows an error and keeps the window open when the wiki cannot be reached")
  @Test
  void handleOkShowsErrorWhenWikiCannotBeReached()
      throws InterruptedException, ExecutionException, TimeoutException {
    mockApiSiteInfo.givenRequestSiteInfo(CUSTOM_WIKI, List.of(), null, null, null).willFail();
    final AtomicBoolean added = new AtomicBoolean(false);

    final WikiDefinitionWindow window =
        callOnJavaFx(
            () -> {
              final WikiDefinitionWindow created =
                  new WikiDefinitionWindow(services, null, _ -> added.set(true));
              lookup(created, WikiDefinitionWindow.NAME_ID, TextField.class).setText("CustomWiki");
              lookup(created, WikiDefinitionWindow.LANGUAGE_ID, TextField.class).setText("fr");
              lookup(created, WikiDefinitionWindow.OK_ID, Button.class).fire();
              return created;
            });

    Assertions.assertThat(closeAlert(window))
        .startsWith("The wiki could not be reached or is invalid: ")
        .contains("500");
    Assertions.assertThat(callOnJavaFx(() -> window.getStage().isShowing())).isTrue();
    Assertions.assertThat(added).isFalse();
    Assertions.assertThat(knownDefinitions.getDefinitions()).doesNotContain(CUSTOM_WIKI);
  }
}
