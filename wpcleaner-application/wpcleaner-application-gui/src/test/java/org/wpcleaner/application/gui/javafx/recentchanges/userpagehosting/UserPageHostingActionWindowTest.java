package org.wpcleaner.application.gui.javafx.recentchanges.userpagehosting;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.wpcleaner.api.analysis.PageAnalysisFactory;
import org.wpcleaner.api.repository.interwiki.InterwikiRepository;
import org.wpcleaner.api.repository.namespace.NamespaceRepository;
import org.wpcleaner.api.repository.protocol.ProtocolRepository;
import org.wpcleaner.api.wiki.definition.WikipediaDefinitions;
import org.wpcleaner.application.gui.core.style.StylePropertiesRegistry;
import org.wpcleaner.application.gui.javafx.JavaFxTest;
import org.wpcleaner.application.gui.javafx.JavaFxWindowsRegistry;
import org.wpcleaner.application.gui.javafx.core.action.JavaFxActionServices;
import org.wpcleaner.application.gui.javafx.core.action.content.PageContentDisplay;
import org.wpcleaner.application.gui.javafx.core.action.content.append.ContentAppendActionControl;
import org.wpcleaner.application.gui.javafx.core.action.content.append.ContentAppendConfiguration;
import org.wpcleaner.application.gui.javafx.core.action.content.append.ContentAppendConfigurationText;
import org.wpcleaner.application.gui.javafx.core.action.content.replace.ContentReplaceActionControl;
import org.wpcleaner.application.gui.javafx.core.action.content.replace.ContentReplaceConfiguration;
import org.wpcleaner.application.gui.javafx.core.pageanalysis.PageAnalysisScrollPane;
import org.wpcleaner.application.gui.javafx.core.pageanalysis.coloration.PageSyntaxColorizer;
import org.wpcleaner.application.gui.javafx.core.style.JavaFxStylePropertiesRegistry;
import org.wpcleaner.application.gui.javafx.recentchanges.JavaFxRecentChangesWindowServices;
import org.wpcleaner.lib.image.ImageLoader;

class UserPageHostingActionWindowTest extends JavaFxTest {

  @DisplayName("UserPageHostingActionWindow initializes controls properly")
  @Test
  void testUserPageHostingActionWindowInitialization()
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
          final UserPageHostingConfig config =
              new UserPageHostingConfig(
                  new ContentReplaceConfiguration("userPageSummary", "userPageText"),
                  new ContentAppendConfiguration(
                      "userTalkPageSummary",
                      List.of(new ContentAppendConfigurationText(true, "label1", "text1"))));

          final UserPageHostingActionWindow window =
              new UserPageHostingActionWindow(
                  services,
                  ownerStage,
                  WikipediaDefinitions.EN,
                  config,
                  "User:Test",
                  "Initial user page",
                  "User talk:Test",
                  "Initial talk page");

          Assertions.assertThat(window.getStage().getTitle())
              .isEqualTo("Confirm User Page Hosting Action");
          Assertions.assertThat(window.getName()).isEqualTo("userPageHostingAction");
          Assertions.assertThat(window.getStage().getScene()).isNotNull();
          Assertions.assertThat(window.getStage().isShowing()).isTrue();
          Assertions.assertThat(window.getStage().getModality()).isEqualTo(Modality.WINDOW_MODAL);
          Assertions.assertThat(window.getStage().getOwner()).isEqualTo(ownerStage);

          final GridPane matrix =
              lookup(window, UserPageHostingActionWindow.MATRIX_ID, GridPane.class);
          Assertions.assertThat(((VBox) matrix.getParent()).getPrefWidth()).isEqualTo(1900.0);

          Assertions.assertThat(matrix.getChildren()).hasSize(4);
          Assertions.assertThat(matrix.getColumnConstraints()).hasSize(2);
          Assertions.assertThat(matrix.getColumnConstraints().get(0).getPercentWidth())
              .isEqualTo(50.0);
          Assertions.assertThat(matrix.getColumnConstraints().get(1).getPercentWidth())
              .isEqualTo(50.0);

          final ContentReplaceActionControl userPageGroup =
              lookup(
                  window,
                  UserPageHostingActionWindow.USER_PAGE_ACTION_ID,
                  ContentReplaceActionControl.class);
          final ContentAppendActionControl userTalkPageGroup =
              lookup(
                  window,
                  UserPageHostingActionWindow.USER_TALK_PAGE_ACTION_ID,
                  ContentAppendActionControl.class);
          final VBox userPageBox =
              lookup(window, UserPageHostingActionWindow.USER_PAGE_DISPLAY_ID, VBox.class);
          final VBox userTalkPageBox =
              lookup(window, UserPageHostingActionWindow.USER_TALK_PAGE_DISPLAY_ID, VBox.class);

          Assertions.assertThat(GridPane.getColumnIndex(userPageGroup)).isEqualTo(0);
          Assertions.assertThat(GridPane.getRowIndex(userPageGroup)).isEqualTo(0);
          Assertions.assertThat(userPageGroup.getText()).isEqualTo("User page");
          Assertions.assertThat(userPageGroup.getMaxHeight()).isEqualTo(Double.MAX_VALUE);

          Assertions.assertThat(GridPane.getColumnIndex(userTalkPageGroup)).isEqualTo(1);
          Assertions.assertThat(GridPane.getRowIndex(userTalkPageGroup)).isEqualTo(0);
          Assertions.assertThat(userTalkPageGroup.getText()).isEqualTo("User talk page");
          Assertions.assertThat(userTalkPageGroup.getMaxHeight()).isEqualTo(Double.MAX_VALUE);

          Assertions.assertThat(GridPane.getColumnIndex(userPageBox)).isEqualTo(0);
          Assertions.assertThat(GridPane.getRowIndex(userPageBox)).isEqualTo(1);
          final Label userPageLabel = lookup(userPageBox, PageContentDisplay.TITLE_ID, Label.class);
          Assertions.assertThat(userPageLabel.getText()).isEqualTo("User:Test");
          final PageAnalysisScrollPane userPageScrollPane =
              lookup(userPageBox, PageContentDisplay.CONTENT_ID, PageAnalysisScrollPane.class);
          Assertions.assertThat(userPageScrollPane.getArea().getPrefHeight()).isEqualTo(700.0);
          Assertions.assertThat(userPageBox.getMaxHeight()).isEqualTo(Double.MAX_VALUE);

          Assertions.assertThat(GridPane.getColumnIndex(userTalkPageBox)).isEqualTo(1);
          Assertions.assertThat(GridPane.getRowIndex(userTalkPageBox)).isEqualTo(1);
          final Label userTalkPageLabel =
              lookup(userTalkPageBox, PageContentDisplay.TITLE_ID, Label.class);
          Assertions.assertThat(userTalkPageLabel.getText()).isEqualTo("User talk:Test");
          final PageAnalysisScrollPane userTalkPageScrollPane =
              lookup(userTalkPageBox, PageContentDisplay.CONTENT_ID, PageAnalysisScrollPane.class);
          Assertions.assertThat(userTalkPageScrollPane.getArea().getPrefHeight()).isEqualTo(700.0);
          Assertions.assertThat(userTalkPageBox.getMaxHeight()).isEqualTo(Double.MAX_VALUE);

          window.getStage().close();
          ownerStage.close();
        });
  }
}
