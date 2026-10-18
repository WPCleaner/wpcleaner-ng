package org.wpcleaner.application.gui.javafx.core.action.content.append;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.wpcleaner.api.analysis.PageAnalysisFactory;
import org.wpcleaner.api.api.edit.ApiEditWrapper;
import org.wpcleaner.api.api.edit.EditQueryByTitle;
import org.wpcleaner.api.repository.interwiki.InterwikiRepository;
import org.wpcleaner.api.repository.namespace.NamespaceRepository;
import org.wpcleaner.api.repository.protocol.ProtocolRepository;
import org.wpcleaner.api.wiki.definition.WikipediaDefinitions;
import org.wpcleaner.application.gui.core.style.StylePropertiesRegistry;
import org.wpcleaner.application.gui.javafx.JavaFxTest;
import org.wpcleaner.application.gui.javafx.core.action.content.PageContentDisplay;
import org.wpcleaner.application.gui.javafx.core.pageanalysis.coloration.PageSyntaxColorizer;
import org.wpcleaner.application.gui.javafx.core.style.JavaFxStylePropertiesRegistry;

class ContentAppendActionTest extends JavaFxTest {

  @DisplayName("ContentAppendAction initializes controls and properties properly")
  @Test
  void testInitializationAndProperties()
      throws InterruptedException, ExecutionException, TimeoutException {
    runOnJavaFx(
        () -> {
          final PageSyntaxColorizer colorizer =
              new PageSyntaxColorizer(
                  List.of(),
                  new JavaFxStylePropertiesRegistry(new StylePropertiesRegistry(List.of())));
          final PageAnalysisFactory pageAnalysisFactory =
              new PageAnalysisFactory(
                  new InterwikiRepository(), new NamespaceRepository(), new ProtocolRepository());
          final ApiEditWrapper apiEditWrapper = Mockito.mock(ApiEditWrapper.class);
          final ContentAppendConfiguration config =
              new ContentAppendConfiguration(
                  "Summary", List.of(new ContentAppendConfigurationText(true, "Label", "Text")));

          final ContentAppendAction action =
              new ContentAppendAction(
                  colorizer,
                  pageAnalysisFactory,
                  apiEditWrapper,
                  config,
                  new ContentAppendContext(
                      "Append Action",
                      WikipediaDefinitions.EN,
                      "User talk:Test",
                      "Existing content"));

          Assertions.assertThat(action.getConfigurationControl()).isNotNull();
          Assertions.assertThat(action.getConfigurationControl())
              .isInstanceOf(ContentAppendActionControl.class);
          Assertions.assertThat(action.getDisplayPane()).isNotNull();
          Assertions.assertThat(action.getDisplayPane()).isInstanceOf(PageContentDisplay.class);
          Assertions.assertThat(action.isComplete()).isTrue();
        });
  }

  @DisplayName("ContentAppendAction execute replaces variable placeholders in selected texts")
  @Test
  void testExecuteReplacesVariables()
      throws InterruptedException, ExecutionException, TimeoutException {
    runOnJavaFx(
        () -> {
          final PageSyntaxColorizer colorizer =
              new PageSyntaxColorizer(
                  List.of(),
                  new JavaFxStylePropertiesRegistry(new StylePropertiesRegistry(List.of())));
          final PageAnalysisFactory pageAnalysisFactory =
              new PageAnalysisFactory(
                  new InterwikiRepository(), new NamespaceRepository(), new ProtocolRepository());
          final ApiEditWrapper apiEditWrapper = Mockito.mock(ApiEditWrapper.class);
          final ContentAppendConfiguration config =
              new ContentAppendConfiguration(
                  "Append summary",
                  List.of(
                      new ContentAppendConfigurationText(
                          true,
                          "Notice",
                          "Hello __USER__, your draft is at __DRAFT__. Please check __DRAFT__!")));

          final ContentAppendAction action =
              new ContentAppendAction(
                  colorizer,
                  pageAnalysisFactory,
                  apiEditWrapper,
                  config,
                  new ContentAppendContext(
                      "Append Action", WikipediaDefinitions.EN, "User talk:Alice", ""));

          action.setVariable("__USER__", "Alice");
          action.setVariable("__DRAFT__", "User:Alice/Draft");

          action.execute();

          final ArgumentCaptor<EditQueryByTitle> captor =
              ArgumentCaptor.forClass(EditQueryByTitle.class);
          Mockito.verify(apiEditWrapper)
              .edit(Mockito.eq(WikipediaDefinitions.EN), captor.capture());

          final EditQueryByTitle query = captor.getValue();
          Assertions.assertThat(query.title()).isEqualTo("User talk:Alice");
          Assertions.assertThat(query.common().appendText())
              .isEqualTo(
                  "\n\nHello Alice, your draft is at User:Alice/Draft. Please check User:Alice/Draft!");
          Assertions.assertThat(query.common().summary()).isEqualTo("Append summary");
        });
  }

  @DisplayName(
      "ContentAppendAction execute joins multiple selected texts with variable replacement")
  @Test
  void testExecuteWithMultipleSelectedTexts()
      throws InterruptedException, ExecutionException, TimeoutException {
    runOnJavaFx(
        () -> {
          final PageSyntaxColorizer colorizer =
              new PageSyntaxColorizer(
                  List.of(),
                  new JavaFxStylePropertiesRegistry(new StylePropertiesRegistry(List.of())));
          final PageAnalysisFactory pageAnalysisFactory =
              new PageAnalysisFactory(
                  new InterwikiRepository(), new NamespaceRepository(), new ProtocolRepository());
          final ApiEditWrapper apiEditWrapper = Mockito.mock(ApiEditWrapper.class);
          final ContentAppendConfiguration config =
              new ContentAppendConfiguration(
                  "Multi summary",
                  List.of(
                      new ContentAppendConfigurationText(true, "First", "Header for __USER__"),
                      new ContentAppendConfigurationText(true, "Second", "Draft link: __DRAFT__")));

          final ContentAppendAction action =
              new ContentAppendAction(
                  colorizer,
                  pageAnalysisFactory,
                  apiEditWrapper,
                  config,
                  new ContentAppendContext(
                      "Append Action", WikipediaDefinitions.EN, "User talk:Bob", ""));

          action.setVariable("__USER__", "Bob");
          action.setVariable("__DRAFT__", "Draft/1");

          action.execute();

          final ArgumentCaptor<EditQueryByTitle> captor =
              ArgumentCaptor.forClass(EditQueryByTitle.class);
          Mockito.verify(apiEditWrapper)
              .edit(Mockito.eq(WikipediaDefinitions.EN), captor.capture());

          final EditQueryByTitle query = captor.getValue();
          Assertions.assertThat(query.title()).isEqualTo("User talk:Bob");
          Assertions.assertThat(query.common().appendText())
              .isEqualTo("\n\nHeader for Bob\n\nDraft link: Draft/1");
        });
  }

  @DisplayName("ContentAppendAction execute does nothing when selected texts is empty")
  @Test
  void testExecuteWithEmptySelectedTexts()
      throws InterruptedException, ExecutionException, TimeoutException {
    runOnJavaFx(
        () -> {
          final PageSyntaxColorizer colorizer =
              new PageSyntaxColorizer(
                  List.of(),
                  new JavaFxStylePropertiesRegistry(new StylePropertiesRegistry(List.of())));
          final PageAnalysisFactory pageAnalysisFactory =
              new PageAnalysisFactory(
                  new InterwikiRepository(), new NamespaceRepository(), new ProtocolRepository());
          final ApiEditWrapper apiEditWrapper = Mockito.mock(ApiEditWrapper.class);
          final ContentAppendConfiguration config =
              new ContentAppendConfiguration(
                  "Summary",
                  List.of(new ContentAppendConfigurationText(false, "Unchecked", "Text")));

          final ContentAppendAction action =
              new ContentAppendAction(
                  colorizer,
                  pageAnalysisFactory,
                  apiEditWrapper,
                  config,
                  new ContentAppendContext(
                      "Append Action", WikipediaDefinitions.EN, "User talk:Test", ""));

          action.execute();

          Mockito.verifyNoInteractions(apiEditWrapper);
        });
  }
}
