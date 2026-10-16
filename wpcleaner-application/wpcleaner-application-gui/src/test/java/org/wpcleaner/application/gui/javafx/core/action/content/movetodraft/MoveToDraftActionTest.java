package org.wpcleaner.application.gui.javafx.core.action.content.movetodraft;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.wpcleaner.api.analysis.PageAnalysisFactory;
import org.wpcleaner.api.api.edit.ApiEditWrapper;
import org.wpcleaner.api.api.move.MoveQueryByTitle;
import org.wpcleaner.api.api.query.prop.revisions.ApiRevisions;
import org.wpcleaner.api.repository.interwiki.InterwikiRepository;
import org.wpcleaner.api.repository.namespace.NamespaceRepository;
import org.wpcleaner.api.repository.protocol.ProtocolRepository;
import org.wpcleaner.api.wiki.definition.WikipediaDefinitions;
import org.wpcleaner.application.gui.core.style.StylePropertiesRegistry;
import org.wpcleaner.application.gui.javafx.JavaFxTest;
import org.wpcleaner.application.gui.javafx.core.action.content.PageContentDisplay;
import org.wpcleaner.application.gui.javafx.core.action.content.RetrievePageContentTask;
import org.wpcleaner.application.gui.javafx.core.pageanalysis.coloration.PageSyntaxColorizer;
import org.wpcleaner.application.gui.javafx.core.style.JavaFxStylePropertiesRegistry;
import org.wpcleaner.application.gui.javafx.core.window.JavaFxWindow;
import org.wpcleaner.lib.image.ImageLoader;

class MoveToDraftActionTest extends JavaFxTest {

  @DisplayName("MoveToDraftAction initializes controls and completeness correctly")
  @Test
  void testInitializationAndProperties()
      throws InterruptedException, ExecutionException, TimeoutException {
    runOnJavaFx(
        () -> {
          final JavaFxWindow<?> window = Mockito.mock(JavaFxWindow.class);
          final ImageLoader imageLoader = Mockito.mock(ImageLoader.class);
          final PageSyntaxColorizer colorizer =
              new PageSyntaxColorizer(
                  List.of(),
                  new JavaFxStylePropertiesRegistry(new StylePropertiesRegistry(List.of())));
          final PageAnalysisFactory pageAnalysisFactory =
              new PageAnalysisFactory(
                  new InterwikiRepository(), new NamespaceRepository(), new ProtocolRepository());
          final ApiEditWrapper apiEditWrapper = Mockito.mock(ApiEditWrapper.class);
          final ApiRevisions apiRevisions = Mockito.mock(ApiRevisions.class);
          final MoveToDraftConfiguration config =
              new MoveToDraftConfiguration("Draft", "Move to draft");

          final MoveToDraftAction action =
              new MoveToDraftAction(
                  window,
                  imageLoader,
                  colorizer,
                  pageAnalysisFactory,
                  apiEditWrapper,
                  apiRevisions,
                  WikipediaDefinitions.EN,
                  "Move to draft",
                  config,
                  "User:Test",
                  "Draft",
                  "Initial draft content");

          Assertions.assertThat(action.getConfigurationControl()).isNotNull();
          Assertions.assertThat(action.getDisplayPane()).isNotNull();
          Assertions.assertThat(action.getDisplayPane()).isInstanceOf(PageContentDisplay.class);
          Assertions.assertThat(action.isComplete()).isTrue();

          final Label displayLabel = (Label) action.getDisplayPane().getChildren().getFirst();
          Assertions.assertThat(displayLabel.getText()).isEqualTo("User:Test/Draft");
          Assertions.assertThat(action.getSubPageName()).isEqualTo("Draft");
        });
  }

  @DisplayName("MoveToDraftAction getSubPageName returns trimmed sub-page name")
  @Test
  void testGetSubPageName() throws InterruptedException, ExecutionException, TimeoutException {
    runOnJavaFx(
        () -> {
          final JavaFxWindow<?> window = Mockito.mock(JavaFxWindow.class);
          final ImageLoader imageLoader = Mockito.mock(ImageLoader.class);
          final PageSyntaxColorizer colorizer =
              new PageSyntaxColorizer(
                  List.of(),
                  new JavaFxStylePropertiesRegistry(new StylePropertiesRegistry(List.of())));
          final PageAnalysisFactory pageAnalysisFactory =
              new PageAnalysisFactory(
                  new InterwikiRepository(), new NamespaceRepository(), new ProtocolRepository());
          final ApiEditWrapper apiEditWrapper = Mockito.mock(ApiEditWrapper.class);
          final ApiRevisions apiRevisions = Mockito.mock(ApiRevisions.class);
          final MoveToDraftConfiguration config =
              new MoveToDraftConfiguration("  DraftSubPage  ", "Summary text");

          final MoveToDraftAction action =
              new MoveToDraftAction(
                  window,
                  imageLoader,
                  colorizer,
                  pageAnalysisFactory,
                  apiEditWrapper,
                  apiRevisions,
                  WikipediaDefinitions.EN,
                  "Move to draft",
                  config,
                  "User:Author",
                  "DraftSubPage",
                  "Initial draft content");

          Assertions.assertThat(action.getSubPageName()).isEqualTo("DraftSubPage");
        });
  }

  @DisplayName("MoveToDraftAction execute calls apiEditWrapper with correct parameters")
  @Test
  void testExecute() throws InterruptedException, ExecutionException, TimeoutException {
    runOnJavaFx(
        () -> {
          final JavaFxWindow<?> window = Mockito.mock(JavaFxWindow.class);
          final ImageLoader imageLoader = Mockito.mock(ImageLoader.class);
          final PageSyntaxColorizer colorizer =
              new PageSyntaxColorizer(
                  List.of(),
                  new JavaFxStylePropertiesRegistry(new StylePropertiesRegistry(List.of())));
          final PageAnalysisFactory pageAnalysisFactory =
              new PageAnalysisFactory(
                  new InterwikiRepository(), new NamespaceRepository(), new ProtocolRepository());
          final ApiEditWrapper apiEditWrapper = Mockito.mock(ApiEditWrapper.class);
          final ApiRevisions apiRevisions = Mockito.mock(ApiRevisions.class);
          final MoveToDraftConfiguration config =
              new MoveToDraftConfiguration("CustomSubPage", "Summary text");

          final MoveToDraftAction action =
              new MoveToDraftAction(
                  window,
                  imageLoader,
                  colorizer,
                  pageAnalysisFactory,
                  apiEditWrapper,
                  apiRevisions,
                  WikipediaDefinitions.EN,
                  "Move to draft",
                  config,
                  "User:Author",
                  "CustomSubPage",
                  "Initial draft content");

          action.execute();

          final ArgumentCaptor<MoveQueryByTitle> captor =
              ArgumentCaptor.forClass(MoveQueryByTitle.class);
          Mockito.verify(apiEditWrapper)
              .move(Mockito.eq(WikipediaDefinitions.EN), captor.capture());

          final MoveQueryByTitle query = captor.getValue();
          Assertions.assertThat(query.from()).isEqualTo("User:Author");
          Assertions.assertThat(query.common().to()).isEqualTo("User:Author/CustomSubPage");
          Assertions.assertThat(query.common().reason()).isEqualTo("Summary text");
        });
  }

  @DisplayName("MoveToDraftAction refresh updates PageContentDisplay on success")
  @Test
  void testRefreshSuccess() throws InterruptedException, ExecutionException, TimeoutException {
    runOnJavaFx(
        () -> {
          final JavaFxWindow<?> window = Mockito.mock(JavaFxWindow.class);
          final ImageLoader imageLoader = Mockito.mock(ImageLoader.class);
          final PageSyntaxColorizer colorizer =
              new PageSyntaxColorizer(
                  List.of(),
                  new JavaFxStylePropertiesRegistry(new StylePropertiesRegistry(List.of())));
          final PageAnalysisFactory pageAnalysisFactory =
              new PageAnalysisFactory(
                  new InterwikiRepository(), new NamespaceRepository(), new ProtocolRepository());
          final ApiEditWrapper apiEditWrapper = Mockito.mock(ApiEditWrapper.class);
          final ApiRevisions apiRevisions = Mockito.mock(ApiRevisions.class);
          final MoveToDraftConfiguration config =
              new MoveToDraftConfiguration("Draft", "Summary text");

          final MoveToDraftAction action =
              new MoveToDraftAction(
                  window,
                  imageLoader,
                  colorizer,
                  pageAnalysisFactory,
                  apiEditWrapper,
                  apiRevisions,
                  WikipediaDefinitions.EN,
                  "Move to draft",
                  config,
                  "User:Author",
                  "Draft",
                  "Initial draft content");

          final MoveToDraftActionControl control =
              (MoveToDraftActionControl) action.getConfigurationControl();
          final GridPane grid = (GridPane) control.getContent();
          final TextField subPageNameField = (TextField) grid.getChildren().get(3);
          final Button refreshButton = (Button) grid.getChildren().get(4);

          subPageNameField.setText("NewDraft");

          refreshButton.fire();

          final ArgumentCaptor<RetrievePageContentTask> taskCaptor =
              ArgumentCaptor.forClass(RetrievePageContentTask.class);
          @SuppressWarnings("unchecked")
          final ArgumentCaptor<Consumer<String>> onSuccessCaptor =
              ArgumentCaptor.forClass(Consumer.class);

          Mockito.verify(window)
              .executeAsync(taskCaptor.capture(), onSuccessCaptor.capture(), Mockito.any());

          Assertions.assertThat(taskCaptor.getValue()).isNotNull();

          onSuccessCaptor.getValue().accept("Refreshed content");

          final Label displayLabel = (Label) action.getDisplayPane().getChildren().getFirst();
          Assertions.assertThat(displayLabel.getText()).isEqualTo("User:Author/NewDraft");
        });
  }

  @DisplayName("MoveToDraftAction refresh shows error when subpage name is blank")
  @Test
  void testRefreshBlankSubPage() throws InterruptedException, ExecutionException, TimeoutException {
    runOnJavaFx(
        () -> {
          final JavaFxWindow<?> window = Mockito.mock(JavaFxWindow.class);
          final ImageLoader imageLoader = Mockito.mock(ImageLoader.class);
          final PageSyntaxColorizer colorizer =
              new PageSyntaxColorizer(
                  List.of(),
                  new JavaFxStylePropertiesRegistry(new StylePropertiesRegistry(List.of())));
          final PageAnalysisFactory pageAnalysisFactory =
              new PageAnalysisFactory(
                  new InterwikiRepository(), new NamespaceRepository(), new ProtocolRepository());
          final ApiEditWrapper apiEditWrapper = Mockito.mock(ApiEditWrapper.class);
          final ApiRevisions apiRevisions = Mockito.mock(ApiRevisions.class);
          final MoveToDraftConfiguration config = new MoveToDraftConfiguration("", "Summary text");

          final MoveToDraftAction action =
              new MoveToDraftAction(
                  window,
                  imageLoader,
                  colorizer,
                  pageAnalysisFactory,
                  apiEditWrapper,
                  apiRevisions,
                  WikipediaDefinitions.EN,
                  "Move to draft",
                  config,
                  "User:Author",
                  "",
                  "Initial draft content");

          final MoveToDraftActionControl control =
              (MoveToDraftActionControl) action.getConfigurationControl();
          final GridPane grid = (GridPane) control.getContent();
          final Button refreshButton = (Button) grid.getChildren().get(4);

          refreshButton.fire();

          Mockito.verify(window).showError("Please enter a sub-page title");
          Mockito.verify(window, Mockito.never())
              .executeAsync(Mockito.any(), Mockito.any(), Mockito.any());
        });
  }

  @DisplayName("MoveToDraftAction refresh shows error when task fails")
  @Test
  void testRefreshError() throws InterruptedException, ExecutionException, TimeoutException {
    runOnJavaFx(
        () -> {
          final JavaFxWindow<?> window = Mockito.mock(JavaFxWindow.class);
          final ImageLoader imageLoader = Mockito.mock(ImageLoader.class);
          final PageSyntaxColorizer colorizer =
              new PageSyntaxColorizer(
                  List.of(),
                  new JavaFxStylePropertiesRegistry(new StylePropertiesRegistry(List.of())));
          final PageAnalysisFactory pageAnalysisFactory =
              new PageAnalysisFactory(
                  new InterwikiRepository(), new NamespaceRepository(), new ProtocolRepository());
          final ApiEditWrapper apiEditWrapper = Mockito.mock(ApiEditWrapper.class);
          final ApiRevisions apiRevisions = Mockito.mock(ApiRevisions.class);
          final MoveToDraftConfiguration config =
              new MoveToDraftConfiguration("Draft", "Summary text");

          final MoveToDraftAction action =
              new MoveToDraftAction(
                  window,
                  imageLoader,
                  colorizer,
                  pageAnalysisFactory,
                  apiEditWrapper,
                  apiRevisions,
                  WikipediaDefinitions.EN,
                  "Move to draft",
                  config,
                  "User:Author",
                  "Draft",
                  "Initial draft content");

          final MoveToDraftActionControl control =
              (MoveToDraftActionControl) action.getConfigurationControl();
          final GridPane grid = (GridPane) control.getContent();
          final Button refreshButton = (Button) grid.getChildren().get(4);

          refreshButton.fire();

          @SuppressWarnings("unchecked")
          final ArgumentCaptor<Consumer<Exception>> onErrorCaptor =
              ArgumentCaptor.forClass(Consumer.class);

          Mockito.verify(window)
              .executeAsync(Mockito.any(), Mockito.any(), onErrorCaptor.capture());

          onErrorCaptor.getValue().accept(new RuntimeException("Network error"));

          Mockito.verify(window).showError("Error loading page content: Network error");
        });
  }
}
