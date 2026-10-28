package org.wpcleaner.application.gui.javafx.core.action.content.movetodraft;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import org.assertj.core.api.Assertions;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.wpcleaner.api.api.CurrentUserService;
import org.wpcleaner.api.api.move.MockApiMove;
import org.wpcleaner.api.api.move.Move;
import org.wpcleaner.api.api.move.MoveQueryByTitle;
import org.wpcleaner.api.api.move.MoveQueryCommon;
import org.wpcleaner.api.api.query.meta.tokens.MockApiTokens;
import org.wpcleaner.api.api.query.meta.tokens.Tokens;
import org.wpcleaner.api.api.query.meta.tokens.TokensParameters;
import org.wpcleaner.api.api.query.prop.revisions.MockApiRevisions;
import org.wpcleaner.api.api.query.prop.revisions.Page;
import org.wpcleaner.api.api.query.prop.revisions.Revision;
import org.wpcleaner.api.api.query.prop.revisions.RevisionSlot;
import org.wpcleaner.api.api.query.prop.revisions.RevisionsParameters;
import org.wpcleaner.api.api.query.prop.revisions.RevisionsQuery;
import org.wpcleaner.api.wiki.definition.WikiDefinition;
import org.wpcleaner.api.wiki.definition.WikipediaDefinitions;
import org.wpcleaner.application.gui.GuiSpringBootTest;
import org.wpcleaner.application.gui.javafx.JavaFxTest;
import org.wpcleaner.application.gui.javafx.core.action.content.PageContentDisplay;
import org.wpcleaner.application.gui.javafx.core.window.JavaFxWindow;
import org.wpcleaner.application.gui.javafx.recentchanges.JavaFxRecentChangesWindowServices;

@GuiSpringBootTest
class MoveToDraftActionTest extends JavaFxTest {

  private static final WikiDefinition WIKI = WikipediaDefinitions.EN;
  private static final String CSRF_TOKEN = "csrf-token+\\";
  private static final RevisionsQuery CONTENT_QUERY =
      RevisionsQuery.emptyBuilder()
          .properties(
              Set.of(RevisionsParameters.Properties.CONTENT, RevisionsParameters.Properties.IDS))
          .slots(Set.of("main"))
          .build();

  @Autowired private JavaFxRecentChangesWindowServices services;
  @Autowired private CurrentUserService currentUserService;
  @Autowired private MockApiMove mockApiMove;
  @Autowired private MockApiRevisions mockApiRevisions;
  @Autowired private MockApiTokens mockApiTokens;

  @Value("${version.prefix}")
  private String versionPrefix;

  @Nullable private TestWindow window;

  private static final class TestWindow extends JavaFxWindow<JavaFxRecentChangesWindowServices> {

    TestWindow(final JavaFxRecentChangesWindowServices services) {
      super(services);
      initialize();
    }

    @NonNull
    @Override
    public String getName() {
      return "testWindow";
    }

    @NonNull
    @Override
    protected Scene createScene() {
      return new Scene(new StackPane(), 100, 100);
    }
  }

  @BeforeEach
  void setUp() {
    currentUserService.logout();
  }

  @AfterEach
  void tearDown() throws InterruptedException, ExecutionException, TimeoutException {
    final TestWindow currentWindow = window;
    if (currentWindow != null) {
      runOnJavaFx(() -> currentWindow.getStage().close());
    }
  }

  @DisplayName("MoveToDraftAction initializes controls and completeness correctly")
  @Test
  void initializationAndProperties()
      throws InterruptedException, ExecutionException, TimeoutException {
    final MoveToDraftAction action =
        createAction(new MoveToDraftConfiguration("Draft", "Move to draft"), "User:Test", "Draft");

    runOnJavaFx(
        () -> {
          Assertions.assertThat(action.getConfigurationControl()).isNotNull();
          Assertions.assertThat(action.getDisplayPane()).isInstanceOf(PageContentDisplay.class);
          Assertions.assertThat(action.isComplete()).isTrue();
          Assertions.assertThat(getDisplayLabel(action).getText()).isEqualTo("User:Test/Draft");
          Assertions.assertThat(getDisplayedContent(action)).isEqualTo("Initial draft content");
          Assertions.assertThat(action.getSubPageName()).isEqualTo("Draft");
        });
  }

  @DisplayName("MoveToDraftAction getSubPageName returns trimmed sub-page name")
  @Test
  void getSubPageName() throws InterruptedException, ExecutionException, TimeoutException {
    final MoveToDraftAction action =
        createAction(
            new MoveToDraftConfiguration("  DraftSubPage  ", "Summary text"),
            "User:Author",
            "DraftSubPage");

    Assertions.assertThat(callOnJavaFx(action::getSubPageName)).isEqualTo("DraftSubPage");
  }

  @DisplayName("MoveToDraftAction execute moves the page with the configured summary")
  @Test
  void execute() throws InterruptedException, ExecutionException, TimeoutException {
    final MoveToDraftAction action =
        createAction(
            new MoveToDraftConfiguration("CustomSubPage", "Summary text"),
            "User:Author",
            "CustomSubPage");
    mockApiTokens
        .givenRequestTokens(WIKI, List.of(TokensParameters.Type.CSRF))
        .willReturn(new Tokens(null, CSRF_TOKEN, null, null, null, null, null, null, null));
    mockApiMove
        .givenMove(
            WIKI,
            new MoveQueryByTitle(
                "User:Author",
                MoveQueryCommon.emptyBuilder()
                    .to("User:Author/CustomSubPage")
                    .reason("WPCleanerNG v%s - Summary text".formatted(versionPrefix))
                    .noRedirect(true)
                    .ignoreWarnings(true)
                    .token(CSRF_TOKEN)
                    .build()))
        .willReturn(new Move("User:Author", "Summary text", false, "User:Author/CustomSubPage"));

    action.execute();
  }

  @DisplayName("MoveToDraftAction refresh displays the content of the new sub-page")
  @Test
  void refreshSuccess() throws InterruptedException, ExecutionException, TimeoutException {
    final MoveToDraftAction action =
        createAction(new MoveToDraftConfiguration("Draft", "Summary text"), "User:Author", "Draft");
    mockApiRevisions
        .givenRetrieveRevisionsByTitle(WIKI, List.of("User:Author/NewDraft"), CONTENT_QUERY)
        .willReturn(List.of(createPage("User:Author/NewDraft", "Refreshed content")));

    runOnJavaFx(
        () -> {
          getSubPageNameField(action).setText("NewDraft");
          getRefreshButton(action).fire();
        });

    waitForJavaFx(
        () ->
            Optional.of(getDisplayLabel(action).getText()).filter("User:Author/NewDraft"::equals));
    Assertions.assertThat(callOnJavaFx(() -> getDisplayedContent(action)))
        .isEqualTo("Refreshed content");
  }

  @DisplayName("MoveToDraftAction refresh shows error when subpage name is blank")
  @Test
  void refreshBlankSubPage() throws InterruptedException, ExecutionException, TimeoutException {
    final MoveToDraftAction action =
        createAction(new MoveToDraftConfiguration("", "Summary text"), "User:Author", "");

    Platform.runLater(() -> getRefreshButton(action).fire());

    Assertions.assertThat(closeAlert()).isEqualTo("Please enter a sub-page title");
  }

  @DisplayName("MoveToDraftAction refresh shows error when retrieving content fails")
  @Test
  void refreshError() throws InterruptedException, ExecutionException, TimeoutException {
    final MoveToDraftAction action =
        createAction(new MoveToDraftConfiguration("Draft", "Summary text"), "User:Author", "Draft");
    mockApiRevisions
        .givenRetrieveRevisionsByTitle(WIKI, List.of("User:Author/Draft"), CONTENT_QUERY)
        .willFail();

    runOnJavaFx(() -> getRefreshButton(action).fire());

    Assertions.assertThat(closeAlert()).startsWith("Error loading page content: ").contains("500");
    Assertions.assertThat(callOnJavaFx(() -> getDisplayLabel(action).getText()))
        .isEqualTo("User:Author/Draft");
  }

  private MoveToDraftAction createAction(
      final MoveToDraftConfiguration configuration,
      final String basePageTitle,
      final String subPageName)
      throws InterruptedException, ExecutionException, TimeoutException {
    return callOnJavaFx(
        () -> {
          final TestWindow owner = new TestWindow(services);
          window = owner;
          return new MoveToDraftAction(
              owner,
              services.imageLoader(),
              services.colorizer(),
              services.pageAnalysisFactory(),
              services.apiEdit(),
              services.apiRevisions(),
              configuration,
              new MoveToDraftContext(
                  "Move to draft", WIKI, basePageTitle, subPageName, "Initial draft content"));
        });
  }

  private static Page createPage(final String title, final String content) {
    final Revision revision =
        new Revision(
            null,
            false,
            null,
            null,
            123,
            null,
            null,
            Map.of("main", new RevisionSlot(content, null, null, null)),
            List.of(),
            null,
            null,
            null);
    return new Page(false, 2, 1234, List.of(revision), title);
  }

  private static Label getDisplayLabel(final MoveToDraftAction action) {
    return (Label) action.getDisplayPane().getChildren().getFirst();
  }

  private static String getDisplayedContent(final MoveToDraftAction action) {
    return ((PageContentDisplay) action.getDisplayPane()).getArea().getText();
  }

  private static TextField getSubPageNameField(final MoveToDraftAction action) {
    return (TextField) getGrid(action).getChildren().get(3);
  }

  private static Button getRefreshButton(final MoveToDraftAction action) {
    return (Button) getGrid(action).getChildren().get(4);
  }

  private static GridPane getGrid(final MoveToDraftAction action) {
    return (GridPane) ((MoveToDraftActionControl) action.getConfigurationControl()).getContent();
  }
}
