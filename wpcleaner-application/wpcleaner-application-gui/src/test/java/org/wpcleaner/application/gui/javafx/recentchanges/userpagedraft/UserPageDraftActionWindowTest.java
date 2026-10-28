package org.wpcleaner.application.gui.javafx.recentchanges.userpagedraft;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.wpcleaner.api.analysis.PageAnalysisFactory;
import org.wpcleaner.api.api.ConnectedUser;
import org.wpcleaner.api.api.CurrentUserService;
import org.wpcleaner.api.api.query.prop.revisions.ApiRevisions;
import org.wpcleaner.api.api.query.prop.revisions.Page;
import org.wpcleaner.api.api.query.prop.revisions.Revision;
import org.wpcleaner.api.api.query.prop.revisions.RevisionSlot;
import org.wpcleaner.api.repository.interwiki.InterwikiRepository;
import org.wpcleaner.api.repository.namespace.NamespaceRepository;
import org.wpcleaner.api.repository.protocol.ProtocolRepository;
import org.wpcleaner.api.wiki.definition.WikiDefinition;
import org.wpcleaner.api.wiki.definition.WikipediaDefinitions;
import org.wpcleaner.application.gui.core.style.StylePropertiesRegistry;
import org.wpcleaner.application.gui.javafx.JavaFxTest;
import org.wpcleaner.application.gui.javafx.JavaFxWindowsRegistry;
import org.wpcleaner.application.gui.javafx.core.action.JavaFxActionServices;
import org.wpcleaner.application.gui.javafx.core.action.content.append.ContentAppendActionControl;
import org.wpcleaner.application.gui.javafx.core.action.content.append.ContentAppendConfiguration;
import org.wpcleaner.application.gui.javafx.core.action.content.append.ContentAppendConfigurationText;
import org.wpcleaner.application.gui.javafx.core.action.content.movetodraft.MoveToDraftActionControl;
import org.wpcleaner.application.gui.javafx.core.action.content.movetodraft.MoveToDraftConfiguration;
import org.wpcleaner.application.gui.javafx.core.action.content.replace.ContentReplaceActionControl;
import org.wpcleaner.application.gui.javafx.core.action.content.replace.ContentReplaceConfiguration;
import org.wpcleaner.application.gui.javafx.core.pageanalysis.PageAnalysisScrollPane;
import org.wpcleaner.application.gui.javafx.core.pageanalysis.coloration.PageSyntaxColorizer;
import org.wpcleaner.application.gui.javafx.core.style.JavaFxStylePropertiesRegistry;
import org.wpcleaner.application.gui.javafx.recentchanges.JavaFxRecentChangesWindowServices;
import org.wpcleaner.lib.image.ImageLoader;

@SuppressWarnings("PMD.CouplingBetweenObjects")
class UserPageDraftActionWindowTest extends JavaFxTest {

  @DisplayName("UserPageDraftActionWindow initializes controls properly")
  @Test
  void testUserPageDraftActionWindowInitialization()
      throws InterruptedException, ExecutionException, TimeoutException {
    runOnJavaFx(
        () -> {
          final JavaFxRecentChangesWindowServices services =
              BDDMockito.mock(JavaFxRecentChangesWindowServices.class);
          final ImageLoader imageLoader = BDDMockito.mock(ImageLoader.class);
          final JavaFxActionServices actionServices = BDDMockito.mock(JavaFxActionServices.class);
          final JavaFxWindowsRegistry windowsRegistry =
              BDDMockito.mock(JavaFxWindowsRegistry.class);

          final PageSyntaxColorizer colorizer =
              new PageSyntaxColorizer(
                  List.of(),
                  new JavaFxStylePropertiesRegistry(new StylePropertiesRegistry(List.of())));
          final PageAnalysisFactory pageAnalysisFactory =
              new PageAnalysisFactory(
                  new InterwikiRepository(), new NamespaceRepository(), new ProtocolRepository());

          BDDMockito.given(services.imageLoader()).willReturn(imageLoader);
          BDDMockito.given(services.actionServices()).willReturn(actionServices);
          BDDMockito.given(services.windowsRegistry()).willReturn(windowsRegistry);
          BDDMockito.given(services.colorizer()).willReturn(colorizer);
          BDDMockito.given(services.pageAnalysisFactory()).willReturn(pageAnalysisFactory);

          final Stage ownerStage = new Stage();
          final UserPageDraftConfig config =
              new UserPageDraftConfig(
                  new MoveToDraftConfiguration("Draft", "moveSummary"),
                  new ContentReplaceConfiguration("userPageSummary", "userPageText"),
                  new ContentAppendConfiguration(
                      "userTalkPageSummary",
                      List.of(new ContentAppendConfigurationText(true, "label1", "text1"))));

          final UserPageDraftActionWindow window =
              new UserPageDraftActionWindow(
                  services,
                  ownerStage,
                  WikipediaDefinitions.EN,
                  config,
                  "User:Test",
                  "Initial user page",
                  "User talk:Test",
                  "Initial talk page",
                  "Draft",
                  "Initial draft");

          Assertions.assertThat(window.getStage().getTitle())
              .isEqualTo("Confirm User Page Draft Action");
          Assertions.assertThat(window.getUserName()).isEqualTo("Test");
          Assertions.assertThat(window.getName()).isEqualTo("userPageDraftAction");
          Assertions.assertThat(window.getStage().getScene()).isNotNull();
          Assertions.assertThat(window.getStage().isShowing()).isTrue();
          Assertions.assertThat(window.getStage().getModality()).isEqualTo(Modality.WINDOW_MODAL);
          Assertions.assertThat(window.getStage().getOwner()).isEqualTo(ownerStage);

          final StackPane root = (StackPane) window.getStage().getScene().getRoot();
          final VBox mainContainer = (VBox) root.getChildren().getFirst();
          Assertions.assertThat(mainContainer.getPrefWidth()).isEqualTo(1900.0);

          final GridPane matrix = (GridPane) mainContainer.getChildren().get(0);
          Assertions.assertThat(matrix.getChildren()).hasSize(6);
          Assertions.assertThat(matrix.getColumnConstraints()).hasSize(3);
          Assertions.assertThat(matrix.getColumnConstraints().get(0).getPercentWidth())
              .isEqualTo(100.0 / 3.0);
          Assertions.assertThat(matrix.getColumnConstraints().get(1).getPercentWidth())
              .isEqualTo(100.0 / 3.0);
          Assertions.assertThat(matrix.getColumnConstraints().get(2).getPercentWidth())
              .isEqualTo(100.0 / 3.0);

          final ContentReplaceActionControl userPageGroup =
              (ContentReplaceActionControl) matrix.getChildren().get(0);
          final ContentAppendActionControl userTalkPageGroup =
              (ContentAppendActionControl) matrix.getChildren().get(1);
          final MoveToDraftActionControl moveDraftGroup =
              (MoveToDraftActionControl) matrix.getChildren().get(2);

          final VBox userPageBox = (VBox) matrix.getChildren().get(3);
          final VBox userTalkPageBox = (VBox) matrix.getChildren().get(4);
          final VBox draftBox = (VBox) matrix.getChildren().get(5);

          Assertions.assertThat(GridPane.getColumnIndex(userPageGroup)).isEqualTo(0);
          Assertions.assertThat(GridPane.getRowIndex(userPageGroup)).isEqualTo(0);
          Assertions.assertThat(userPageGroup.getText()).isEqualTo("User page");
          Assertions.assertThat(userPageGroup.getMaxHeight()).isEqualTo(Double.MAX_VALUE);

          Assertions.assertThat(GridPane.getColumnIndex(userTalkPageGroup)).isEqualTo(1);
          Assertions.assertThat(GridPane.getRowIndex(userTalkPageGroup)).isEqualTo(0);
          Assertions.assertThat(userTalkPageGroup.getText()).isEqualTo("User talk page");
          Assertions.assertThat(userTalkPageGroup.getMaxHeight()).isEqualTo(Double.MAX_VALUE);

          Assertions.assertThat(GridPane.getColumnIndex(moveDraftGroup)).isEqualTo(2);
          Assertions.assertThat(GridPane.getRowIndex(moveDraftGroup)).isEqualTo(0);
          Assertions.assertThat(moveDraftGroup.getText()).isEqualTo("Move to draft");
          Assertions.assertThat(moveDraftGroup.getMaxHeight()).isEqualTo(Double.MAX_VALUE);
          final GridPane moveDraftGrid = (GridPane) moveDraftGroup.getContent();
          final TextField subPageNameField = (TextField) moveDraftGrid.getChildren().get(3);
          Assertions.assertThat(subPageNameField.getText()).isEqualTo("Draft");
          final Button refreshButton = (Button) moveDraftGrid.getChildren().get(4);
          Assertions.assertThat(refreshButton.getTooltip().getText())
              .isEqualTo("Refresh draft page content");

          Assertions.assertThat(GridPane.getColumnIndex(userPageBox)).isEqualTo(0);
          Assertions.assertThat(GridPane.getRowIndex(userPageBox)).isEqualTo(1);
          final Label userPageLabel = (Label) userPageBox.getChildren().getFirst();
          Assertions.assertThat(userPageLabel.getText()).isEqualTo("User:Test");
          final PageAnalysisScrollPane userPageScrollPane =
              (PageAnalysisScrollPane) userPageBox.getChildren().get(1);
          Assertions.assertThat(userPageScrollPane.getArea().getPrefHeight()).isEqualTo(700.0);
          Assertions.assertThat(userPageBox.getMaxHeight()).isEqualTo(Double.MAX_VALUE);

          Assertions.assertThat(GridPane.getColumnIndex(userTalkPageBox)).isEqualTo(1);
          Assertions.assertThat(GridPane.getRowIndex(userTalkPageBox)).isEqualTo(1);
          final Label userTalkPageLabel = (Label) userTalkPageBox.getChildren().getFirst();
          Assertions.assertThat(userTalkPageLabel.getText()).isEqualTo("User talk:Test");
          final PageAnalysisScrollPane userTalkPageScrollPane =
              (PageAnalysisScrollPane) userTalkPageBox.getChildren().get(1);
          Assertions.assertThat(userTalkPageScrollPane.getArea().getPrefHeight()).isEqualTo(700.0);
          Assertions.assertThat(userTalkPageBox.getMaxHeight()).isEqualTo(Double.MAX_VALUE);

          Assertions.assertThat(GridPane.getColumnIndex(draftBox)).isEqualTo(2);
          Assertions.assertThat(GridPane.getRowIndex(draftBox)).isEqualTo(1);
          final Label draftLabel = (Label) draftBox.getChildren().getFirst();
          Assertions.assertThat(draftLabel.getText()).isEqualTo("User:Test/Draft");

          final PageAnalysisScrollPane draftScrollPane =
              (PageAnalysisScrollPane) draftBox.getChildren().get(1);
          Assertions.assertThat(draftScrollPane.getArea().getPrefHeight()).isEqualTo(700.0);
          Assertions.assertThat(draftBox.getMaxHeight()).isEqualTo(Double.MAX_VALUE);

          Assertions.assertThat(userPageBox.getPrefWidth())
              .isEqualTo(userTalkPageBox.getPrefWidth());
          Assertions.assertThat(userPageBox.getPrefWidth()).isEqualTo(draftBox.getPrefWidth());
          Assertions.assertThat(HBox.getHgrow(userPageBox)).isEqualTo(Priority.ALWAYS);
          Assertions.assertThat(HBox.getHgrow(userTalkPageBox)).isEqualTo(Priority.ALWAYS);
          Assertions.assertThat(HBox.getHgrow(draftBox)).isEqualTo(Priority.ALWAYS);

          window.getStage().close();
          ownerStage.close();
        });
  }

  @DisplayName("Clicking refresh button updates draft preview area with new sub-page content")
  @Test
  void testRefreshDraftContentUpdatesPreview()
      throws InterruptedException, ExecutionException, TimeoutException {
    runOnJavaFx(
        () -> {
          final JavaFxRecentChangesWindowServices services =
              BDDMockito.mock(JavaFxRecentChangesWindowServices.class);
          final ImageLoader imageLoader = BDDMockito.mock(ImageLoader.class);
          final JavaFxActionServices actionServices = BDDMockito.mock(JavaFxActionServices.class);
          final JavaFxWindowsRegistry windowsRegistry =
              BDDMockito.mock(JavaFxWindowsRegistry.class);
          final CurrentUserService currentUserService = BDDMockito.mock(CurrentUserService.class);
          final ConnectedUser connectedUser = BDDMockito.mock(ConnectedUser.class);
          final WikiDefinition wiki = BDDMockito.mock(WikiDefinition.class);
          final ApiRevisions apiRevisions = BDDMockito.mock(ApiRevisions.class);

          final PageSyntaxColorizer colorizer =
              new PageSyntaxColorizer(
                  List.of(),
                  new JavaFxStylePropertiesRegistry(new StylePropertiesRegistry(List.of())));
          final PageAnalysisFactory pageAnalysisFactory =
              new PageAnalysisFactory(
                  new InterwikiRepository(), new NamespaceRepository(), new ProtocolRepository());

          BDDMockito.given(services.imageLoader()).willReturn(imageLoader);
          BDDMockito.given(services.actionServices()).willReturn(actionServices);
          BDDMockito.given(services.windowsRegistry()).willReturn(windowsRegistry);
          BDDMockito.given(services.colorizer()).willReturn(colorizer);
          BDDMockito.given(services.pageAnalysisFactory()).willReturn(pageAnalysisFactory);
          BDDMockito.given(services.user()).willReturn(currentUserService);
          BDDMockito.given(currentUserService.getCurrentUser()).willReturn(connectedUser);
          BDDMockito.given(connectedUser.wiki()).willReturn(wiki);
          BDDMockito.given(services.apiRevisions()).willReturn(apiRevisions);

          final Revision revision =
              new Revision(
                  null,
                  false,
                  null,
                  null,
                  null,
                  null,
                  null,
                  Map.of("main", new RevisionSlot("Updated draft content", null, null, null)),
                  List.of(),
                  null,
                  null,
                  null);
          final Page page = new Page(false, 2, 1, List.of(revision), "User:Test/CustomDraft");

          BDDMockito.given(
                  apiRevisions.retrieveRevisionsByTitle(
                      BDDMockito.eq(wiki),
                      BDDMockito.eq(List.of("User:Test/CustomDraft")),
                      BDDMockito.any()))
              .willReturn(List.of(page));

          final Stage ownerStage = new Stage();
          final UserPageDraftConfig config =
              new UserPageDraftConfig(
                  new MoveToDraftConfiguration("Draft", "moveSummary"),
                  new ContentReplaceConfiguration("userPageSummary", "userPageText"),
                  new ContentAppendConfiguration("userTalkPageSummary", List.of()));

          final UserPageDraftActionWindow window =
              new UserPageDraftActionWindow(
                  services,
                  ownerStage,
                  WikipediaDefinitions.EN,
                  config,
                  "User:Test",
                  "Initial user page",
                  "User talk:Test",
                  "Initial talk page",
                  "Draft",
                  "Initial draft");

          final StackPane root = (StackPane) window.getStage().getScene().getRoot();
          final VBox mainContainer = (VBox) root.getChildren().getFirst();
          final GridPane matrix = (GridPane) mainContainer.getChildren().get(0);
          final MoveToDraftActionControl moveDraftGroup =
              (MoveToDraftActionControl) matrix.getChildren().get(2);
          final GridPane moveDraftGrid = (GridPane) moveDraftGroup.getContent();
          final TextField subPageNameField = (TextField) moveDraftGrid.getChildren().get(3);
          final Button refreshButton = (Button) moveDraftGrid.getChildren().get(4);

          subPageNameField.setText("CustomDraft");
          refreshButton.fire();

          BDDMockito.then(apiRevisions)
              .should(BDDMockito.timeout(5000).times(1))
              .retrieveRevisionsByTitle(
                  BDDMockito.eq(WikipediaDefinitions.EN),
                  BDDMockito.eq(List.of("User:Test/CustomDraft")),
                  BDDMockito.any());

          window.getStage().close();
          ownerStage.close();
        });
  }
}
