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
import org.wpcleaner.api.api.query.meta.tokens.MockApiTokens;
import org.wpcleaner.api.api.query.prop.revisions.MockApiRevisions;
import org.wpcleaner.api.repository.configuration.WPCleanerConfiguration;
import org.wpcleaner.api.repository.configuration.WPCleanerConfigurationRepository;
import org.wpcleaner.api.rest.lint.MockRestLint;
import org.wpcleaner.api.wiki.definition.WikiDefinition;
import org.wpcleaner.api.wiki.definition.WikipediaDefinitions;
import org.wpcleaner.application.base.ApplicationLogin;
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

  @Autowired private CurrentUserService currentUserService;
  @Autowired private ApplicationLogin guiLogin;
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
    guiLogin.login(
        WIKI,
        USERNAME,
        ApplicationLogin.LoginData.of(ApplicationLogin.USER_NAMESPACES)
            .withLinterCategories(Map.of("low", List.of("obsolete-tag"))));
    givenConfiguration();
  }

  @AfterEach
  void tearDown() throws InterruptedException, ExecutionException, TimeoutException {
    runOnJavaFx(services.windowsRegistry()::closeAllWindows);
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
        .containsExactly(
            "Button",
            "Button",
            "Button",
            "Button",
            "Separator",
            "CheckWikiDetectionsButton",
            "LinterErrorsButton",
            "BrowserButton",
            "BrowserButton",
            "Separator",
            "PageSaveButton");
    Assertions.assertThat(callOnJavaFx(() -> getCheckWikiDetectionsButton(analysisWindow)))
        .satisfies(
            checkWikiButton -> {
              Assertions.assertThat(checkWikiButton.isDisable()).isFalse();
              Assertions.assertThat(checkWikiButton.getGraphic()).isInstanceOf(ImageView.class);
              Assertions.assertThat(checkWikiButton.getTooltip().getText())
                  .isEqualTo("Retrieve the errors still detected by Check Wiki");
            });
  }

  @DisplayName("should open a window with the errors detected by Check Wiki")
  @Test
  void displayCheckWikiDetections()
      throws InterruptedException, ExecutionException, TimeoutException {
    // GIVEN
    givenPageContent();
    new MockCheckWikiServer(mediaWikiServer)
        .givenDetections(
            WIKI,
            TITLE,
            List.of("+ 48   15     [[Paris]]", "Paris is in whitelist with error: 64"));
    final JavaFxAnalysisWindow analysisWindow = openPage();

    // WHEN
    runOnJavaFx(() -> getCheckWikiDetectionsButton(analysisWindow).fire());

    // THEN
    Assertions.assertThat(
            waitForWindow(services.windowsRegistry(), CheckWikiDetectionsWindow.class)
                .getStage()
                .getOwner())
        .isSameAs(analysisWindow.getStage());
    mediaWikiServer.verify();
  }

  @DisplayName("should inform that Check Wiki detects no error in the page")
  @Test
  void displayNoCheckWikiDetection()
      throws InterruptedException, ExecutionException, TimeoutException {
    // GIVEN
    givenPageContent();
    new MockCheckWikiServer(mediaWikiServer).givenDetections(WIKI, TITLE, List.of("None"));
    final JavaFxAnalysisWindow analysisWindow = openPage();

    // WHEN
    runOnJavaFx(() -> getCheckWikiDetectionsButton(analysisWindow).fire());

    // THEN
    Assertions.assertThat(closeAlert(analysisWindow))
        .isEqualTo("No errors are currently detected by Check Wiki.");
    mediaWikiServer.verify();
  }

  @DisplayName("should send the current text to Linter and open a window with the declared errors")
  @Test
  void displayLinterErrors() throws InterruptedException, ExecutionException, TimeoutException {
    // GIVEN
    givenPageContent();
    new MockRestLint(mediaWikiServer)
        .givenLint(WIKI, TITLE, FIXED_TEXT)
        .willReturn(
            List.of(
                Map.of("type", "obsolete-tag", "dsr", List.of(0, 18, 8, 9)),
                Map.of("type", "large-tables", "dsr", List.of(0, 18, 0, 0))));
    final JavaFxAnalysisWindow analysisWindow = openAndFixPage();

    // WHEN
    runOnJavaFx(() -> getLinterErrorsButton(analysisWindow).fire());

    // THEN
    Assertions.assertThat(
            waitForWindow(services.windowsRegistry(), LinterErrorsWindow.class)
                .getStage()
                .getOwner())
        .isSameAs(analysisWindow.getStage());
    mediaWikiServer.verify();
  }

  @DisplayName("should inform that Linter detects no error declared on the wiki")
  @Test
  void displayNoLinterError() throws InterruptedException, ExecutionException, TimeoutException {
    // GIVEN
    givenPageContent();
    new MockRestLint(mediaWikiServer)
        .givenLint(WIKI, TITLE, TEXT)
        .willReturn(List.of(Map.of("type", "large-tables", "dsr", List.of(0, 18, 0, 0))));
    final JavaFxAnalysisWindow analysisWindow = openPage();

    // WHEN
    runOnJavaFx(() -> getLinterErrorsButton(analysisWindow).fire());

    // THEN
    Assertions.assertThat(closeAlert(analysisWindow))
        .isEqualTo("No errors are detected by Linter.");
    mediaWikiServer.verify();
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
    AnalysisPageExpectations.givenPageContent(mockApiRevisions, WIKI, TITLE, TEXT, TIMESTAMP);
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

  private static Button getCheckWikiDetectionsButton(final JavaFxAnalysisWindow analysisWindow) {
    return lookup(analysisWindow, CheckWikiDetectionsButton.CHECK_WIKI_DETECTIONS_ID, Button.class);
  }

  private static Button getLinterErrorsButton(final JavaFxAnalysisWindow analysisWindow) {
    return lookup(analysisWindow, LinterErrorsButton.LINTER_ERRORS_ID, Button.class);
  }

  private static Button getSaveButton(final JavaFxAnalysisWindow analysisWindow) {
    return lookup(analysisWindow, PageSaveButton.SAVE_ID, Button.class);
  }

  private static TextField getSummaryField(final JavaFxAnalysisWindow analysisWindow) {
    return lookup(analysisWindow, PageSummaryPane.SUMMARY_ID, TextField.class);
  }
}
