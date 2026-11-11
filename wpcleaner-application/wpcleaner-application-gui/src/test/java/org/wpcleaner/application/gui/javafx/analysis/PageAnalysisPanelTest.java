package org.wpcleaner.application.gui.javafx.analysis;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.ToolBar;
import javafx.scene.image.ImageView;
import org.assertj.core.api.Assertions;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.web.client.MockRestServiceServer;
import org.wpcleaner.api.api.ApiError;
import org.wpcleaner.api.api.CurrentUserService;
import org.wpcleaner.api.api.edit.Edit;
import org.wpcleaner.api.api.edit.EditQueryByTitle;
import org.wpcleaner.api.api.edit.EditQueryCommon;
import org.wpcleaner.api.api.edit.MockApiEdit;
import org.wpcleaner.api.api.query.Slot;
import org.wpcleaner.api.api.query.meta.tokens.MockApiTokens;
import org.wpcleaner.api.api.query.prop.revisions.MockApiRevisions;
import org.wpcleaner.api.api.query.prop.revisions.Page;
import org.wpcleaner.api.api.query.prop.revisions.Revision;
import org.wpcleaner.api.api.query.prop.revisions.RevisionSlot;
import org.wpcleaner.api.api.query.prop.revisions.RevisionsParameters;
import org.wpcleaner.api.api.query.prop.revisions.RevisionsQuery;
import org.wpcleaner.api.repository.configuration.WPCleanerConfiguration;
import org.wpcleaner.api.repository.configuration.WPCleanerConfigurationRepository;
import org.wpcleaner.api.wiki.definition.WikiDefinition;
import org.wpcleaner.api.wiki.definition.WikipediaDefinitions;
import org.wpcleaner.application.gui.GuiLogin;
import org.wpcleaner.application.gui.GuiSpringBootTest;
import org.wpcleaner.application.gui.javafx.JavaFxTest;
import org.wpcleaner.application.gui.javafx.core.pageanalysis.CheckErrorsPane;
import org.wpcleaner.application.gui.javafx.core.pageanalysis.CheckErrorsToolBar;
import org.wpcleaner.lib.check.configuration.CheckWikiConfiguration;
import org.wpcleaner.lib.check.configuration.CheckWikiConfigurationRepository;
import org.wpcleaner.lib.check.server.MockCheckWikiServer;

@GuiSpringBootTest
class PageAnalysisPanelTest extends JavaFxTest {

  private static final WikiDefinition WIKI = WikipediaDefinitions.FR;
  private static final String USERNAME = "Jane Doe";
  private static final String TITLE = "Paris";
  private static final String TEXT = "== Histoire ==\n[[Paris]] est une ville.";
  private static final String FIXED_TEXT = "== Histoire ==\nParis est une ville.";
  private static final Instant TIMESTAMP = Instant.parse("2026-11-01T10:00:00Z");
  private static final String CSRF_TOKEN = "csrf-token+\\";
  private static final String COMMENT = "[[P:CS|Correction syntaxique]]";
  private static final String SUMMARY = COMMENT + " (Lien vers l’article)";
  private static final Duration MARK_TIMEOUT = Duration.ofSeconds(5);
  private static final RevisionsQuery CONTENT_QUERY =
      RevisionsQuery.emptyBuilder()
          .properties(
              Set.of(
                  RevisionsParameters.Properties.CONTENT, RevisionsParameters.Properties.TIMESTAMP))
          .slots(Set.of(Slot.MAIN))
          .build();

  @Autowired private CurrentUserService currentUserService;
  @Autowired private GuiLogin guiLogin;
  @Autowired private JavaFxAnalysisWindowServices services;
  @Autowired private WPCleanerConfigurationRepository wpcleanerRepository;
  @Autowired private CheckWikiConfigurationRepository checkWikiRepository;
  @Autowired private MockApiRevisions mockApiRevisions;
  @Autowired private MockApiTokens mockApiTokens;
  @Autowired private MockApiEdit mockApiEdit;
  @Autowired private MockRestServiceServer mediaWikiServer;

  @Value("${version.prefix}")
  private String versionPrefix;

  @Nullable private JavaFxAnalysisWindow window;

  @BeforeEach
  void setUp() {
    currentUserService.logout();
    guiLogin.login(WIKI, USERNAME, GuiLogin.USER_NAMESPACES);
    givenConfiguration();
  }

  @AfterEach
  void tearDown() throws InterruptedException, ExecutionException, TimeoutException {
    final JavaFxAnalysisWindow windowToClose = window;
    if (windowToClose != null) {
      runOnJavaFx(() -> windowToClose.getStage().close());
    }
    currentUserService.logout();
    mediaWikiServer.reset();
  }

  @DisplayName(
      "should save the fixed page with a summary listing the fixed algorithms, and mark them as fixed")
  @Test
  void saveFixedPage() throws InterruptedException, ExecutionException, TimeoutException {
    // GIVEN
    givenPageContent();
    mockApiTokens.givenCsrfToken(WIKI, CSRF_TOKEN);
    mockApiEdit
        .givenEdit(WIKI, createEdit(SUMMARY))
        .willReturn(new Edit(Edit.RESULT_SUCCESS, 1, TITLE, null, TIMESTAMP.plusSeconds(60)));
    new MockCheckWikiServer(mediaWikiServer).givenMarkAsFixed(WIKI, 48, TITLE);
    final JavaFxAnalysisWindow analysisWindow = openAndFixPage();
    Assertions.assertThat(callOnJavaFx(() -> getSummaryField(analysisWindow).getText()))
        .isEqualTo(SUMMARY);

    // WHEN
    runOnJavaFx(() -> getSaveButton(analysisWindow).fire());

    // THEN
    waitForJavaFx(
        () -> Optional.of(getSummaryField(analysisWindow).getText()).filter(String::isEmpty));
    mediaWikiServer.verify(MARK_TIMEOUT);
  }

  @DisplayName("should display an icon with a tooltip for saving the page")
  @Test
  void displaySaveButton() throws InterruptedException, ExecutionException, TimeoutException {
    // GIVEN
    givenPageContent();

    // WHEN
    final JavaFxAnalysisWindow analysisWindow = openPage();

    // THEN
    final Button button = callOnJavaFx(() -> getSaveButton(analysisWindow));
    Assertions.assertThat(button.getText()).isEmpty();
    Assertions.assertThat(button.getGraphic()).isInstanceOf(ImageView.class);
    Assertions.assertThat(button.getTooltip().getText()).isEqualTo("Save the page");
    final ToolBar toolBar = (ToolBar) button.getParent().getParent();
    Assertions.assertThat(toolBar).isInstanceOf(CheckErrorsToolBar.class);
    Assertions.assertThat(toolBar.getItems())
        .extracting(node -> node.getClass().getSimpleName())
        .containsExactly("Button", "Button", "Button", "Button", "Separator", "PageSaveButton");
  }

  @DisplayName("should keep the summary written by the user")
  @Test
  void keepUserSummary() throws InterruptedException, ExecutionException, TimeoutException {
    // GIVEN
    givenPageContent();
    final JavaFxAnalysisWindow analysisWindow = openPage();
    runOnJavaFx(() -> getSummaryField(analysisWindow).setText("Mon résumé"));

    // WHEN
    runOnJavaFx(() -> getAutomaticFixesButton(analysisWindow).fire());

    // THEN
    Assertions.assertThat(callOnJavaFx(() -> getSummaryField(analysisWindow).getText()))
        .isEqualTo("Mon résumé");
  }

  @DisplayName("should report an edit conflict and keep the modifications")
  @Test
  void saveWithEditConflict() throws InterruptedException, ExecutionException, TimeoutException {
    // GIVEN
    givenPageContent();
    mockApiTokens.givenCsrfToken(WIKI, CSRF_TOKEN);
    mockApiEdit
        .givenEdit(WIKI, createEdit(SUMMARY))
        .willReturnErrors(List.of(new ApiError("editconflict", "main", "Edit conflict.")));
    final JavaFxAnalysisWindow analysisWindow = openAndFixPage();

    // WHEN
    runOnJavaFx(() -> getSaveButton(analysisWindow).fire());

    // THEN
    Assertions.assertThat(closeAlert(analysisWindow)).contains("editconflict: Edit conflict.");
    Assertions.assertThat(callOnJavaFx(() -> getSummaryField(analysisWindow).getText()))
        .isEqualTo(SUMMARY);
    mediaWikiServer.verify();
  }

  @DisplayName("should not allow saving in demo mode")
  @Test
  void saveInDemoMode() throws InterruptedException, ExecutionException, TimeoutException {
    // GIVEN
    currentUserService.logout();
    currentUserService.login(WIKI, USERNAME, true);
    givenConfiguration();
    givenPageContent();

    // WHEN
    final JavaFxAnalysisWindow analysisWindow = openPage();

    // THEN
    Assertions.assertThat(callOnJavaFx(() -> getSaveButton(analysisWindow).isDisable())).isTrue();
  }

  private void givenConfiguration() {
    wpcleanerRepository.setConfiguration(
        WIKI, new WPCleanerConfiguration(Map.of("check_wiki_comment", COMMENT), Map.of()));
    checkWikiRepository.setConfiguration(
        WIKI,
        CheckWikiConfiguration.of(
            Map.of("error_048_prio_frwiki", "3", "error_048_head_frwiki", "Lien vers l’article"),
            Map.of(),
            "frwiki"));
  }

  private JavaFxAnalysisWindow openAndFixPage()
      throws InterruptedException, ExecutionException, TimeoutException {
    final JavaFxAnalysisWindow analysisWindow = openPage();
    runOnJavaFx(() -> getAutomaticFixesButton(analysisWindow).fire());
    return analysisWindow;
  }

  private void givenPageContent() {
    mockApiRevisions
        .givenRetrieveRevisionsByTitle(WIKI, List.of(TITLE), CONTENT_QUERY)
        .willReturn(List.of(createPage()));
  }

  private JavaFxAnalysisWindow openPage()
      throws InterruptedException, ExecutionException, TimeoutException {
    final JavaFxAnalysisWindow analysisWindow =
        callOnJavaFx(
            () -> {
              final JavaFxAnalysisWindow created = new JavaFxAnalysisWindow(services);
              created.analyze(TITLE);
              return created;
            });
    window = analysisWindow;
    waitForJavaFx(
        () ->
            find(analysisWindow, CheckErrorsPane.AUTOMATIC_FIXES_ID, Button.class)
                .filter(button -> !button.isDisable()));
    return analysisWindow;
  }

  private static Page createPage() {
    final Revision revision =
        new Revision(
            null,
            false,
            null,
            null,
            123,
            null,
            null,
            Map.of(Slot.MAIN.value, new RevisionSlot(TEXT, null, null, null)),
            List.of(),
            TIMESTAMP,
            null,
            null);
    return new Page(false, 0, 1234, List.of(revision), TITLE);
  }

  private EditQueryByTitle createEdit(final String summary) {
    return new EditQueryByTitle(
        TITLE,
        EditQueryCommon.emptyBuilder()
            .text(FIXED_TEXT)
            .summary("WPCleanerNG v%s - %s".formatted(versionPrefix, summary))
            .baseTimestamp(TIMESTAMP)
            .noCreate(true)
            .token(CSRF_TOKEN)
            .build());
  }

  private static Button getAutomaticFixesButton(final JavaFxAnalysisWindow analysisWindow) {
    return lookup(analysisWindow, CheckErrorsPane.AUTOMATIC_FIXES_ID, Button.class);
  }

  private static Button getSaveButton(final JavaFxAnalysisWindow analysisWindow) {
    return lookup(analysisWindow, PageSaveButton.SAVE_ID, Button.class);
  }

  private static TextField getSummaryField(final JavaFxAnalysisWindow analysisWindow) {
    return lookup(analysisWindow, PageSummaryPane.SUMMARY_ID, TextField.class);
  }
}
