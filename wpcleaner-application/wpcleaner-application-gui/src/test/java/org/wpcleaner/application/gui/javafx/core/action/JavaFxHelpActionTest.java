package org.wpcleaner.application.gui.javafx.core.action;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.wpcleaner.api.api.query.prop.langlinks.Langlink;
import org.wpcleaner.api.api.query.prop.langlinks.LanglinksParameters;
import org.wpcleaner.api.api.query.prop.langlinks.LanglinksQuery;
import org.wpcleaner.api.api.query.prop.langlinks.MockApiLanglinks;
import org.wpcleaner.api.api.query.prop.langlinks.Page;
import org.wpcleaner.api.wiki.definition.WikipediaDefinitions;
import org.wpcleaner.application.gui.GuiSpringBootTest;
import org.wpcleaner.application.gui.core.desktop.DesktopService;
import org.wpcleaner.application.gui.javafx.JavaFxTest;
import org.wpcleaner.application.gui.javafx.login.JavaFxLoginWindow;
import org.wpcleaner.application.gui.javafx.login.JavaFxLoginWindowServices;

@GuiSpringBootTest
class JavaFxHelpActionTest extends JavaFxTest {

  private static final String LOGIN_PAGE = "Wikipedia:WPCleanerNG/Login";
  private static final String ENGLISH_LOGIN_URL = WikipediaDefinitions.EN.pageUrl(LOGIN_PAGE);
  private static final String FRENCH_LOGIN_URL =
      "https://fr.wikipedia.org/wiki/Wikip%C3%A9dia:WPCleanerNG/Login";
  private static final Langlink FRENCH_LANGLINK =
      new Langlink(null, "fr", null, "Wikipédia:WPCleanerNG/Login", FRENCH_LOGIN_URL);
  private static final LanglinksQuery FRENCH_URL_QUERY =
      LanglinksQuery.emptyBuilder()
          .language("fr")
          .properties(Set.of(LanglinksParameters.Properties.URL))
          .build();

  private final Locale defaultLocale = Locale.getDefault();

  @Autowired private JavaFxHelpAction helpAction;
  @Autowired private JavaFxLoginWindowServices loginWindowServices;
  @Autowired private MockApiLanglinks mockApiLanglinks;
  @Autowired private DesktopService desktopService;

  @AfterEach
  void tearDown() {
    Locale.setDefault(defaultLocale);
  }

  @DisplayName("getHelpUrl with null help page and English user language returns default page")
  @Test
  void getHelpUrlWithNullHelpPageAndEnglish() {
    final String url = helpAction.getHelpUrl(null, "en");

    Assertions.assertThat(url).isEqualTo(WikipediaDefinitions.EN.pageUrl("Wikipedia:WPCleanerNG"));
  }

  @DisplayName("getHelpUrl with blank help page and English user language returns default page")
  @Test
  void getHelpUrlWithBlankHelpPageAndEnglish() {
    final String url = helpAction.getHelpUrl("   ", "en");

    Assertions.assertThat(url).isEqualTo(WikipediaDefinitions.EN.pageUrl("Wikipedia:WPCleanerNG"));
  }

  @DisplayName("getHelpUrl with Login help page and English user language returns Login page")
  @Test
  void getHelpUrlWithLoginPageAndEnglish() {
    final String url = helpAction.getHelpUrl("Login", "en");

    Assertions.assertThat(url).isEqualTo(ENGLISH_LOGIN_URL);
  }

  @DisplayName("getHelpUrl with Main help page and English user language returns Main page")
  @Test
  void getHelpUrlWithMainPageAndEnglish() {
    final String url = helpAction.getHelpUrl("Main", "en");

    Assertions.assertThat(url)
        .isEqualTo(WikipediaDefinitions.EN.pageUrl("Wikipedia:WPCleanerNG/Main"));
  }

  @DisplayName("getHelpUrl with foreign user language returns localized page if langlink exists")
  @Test
  void getHelpUrlWithForeignLanguageWhenLanglinkExists() {
    givenFrenchLanglinksForLoginPage(List.of(FRENCH_LANGLINK));

    final String url = helpAction.getHelpUrl("Login", "fr");

    Assertions.assertThat(url).isEqualTo(FRENCH_LOGIN_URL);
  }

  @DisplayName("getHelpUrl with foreign user language falls back to English if no langlink exists")
  @Test
  void getHelpUrlWithForeignLanguageWhenNoLanglinkExists() {
    givenFrenchLanglinksForLoginPage(List.of());

    final String url = helpAction.getHelpUrl("Login", "fr");

    Assertions.assertThat(url).isEqualTo(ENGLISH_LOGIN_URL);
  }

  @DisplayName("displayHelp for window browses localized help page")
  @Test
  void displayHelpForWindow() throws InterruptedException, ExecutionException, TimeoutException {
    Locale.setDefault(Locale.FRENCH);
    givenFrenchLanglinksForLoginPage(List.of(FRENCH_LANGLINK));

    displayHelpForLoginWindow();

    BDDMockito.then(desktopService).should(BDDMockito.timeout(5000)).browse(FRENCH_LOGIN_URL);
  }

  @DisplayName("displayHelp for window browses English help page if API fails")
  @Test
  void displayHelpForWindowWhenApiFails()
      throws InterruptedException, ExecutionException, TimeoutException {
    Locale.setDefault(Locale.FRENCH);
    mockApiLanglinks
        .givenRetrieveLanglinksByTitle(
            WikipediaDefinitions.EN, List.of(LOGIN_PAGE), FRENCH_URL_QUERY)
        .willFail();

    displayHelpForLoginWindow();

    BDDMockito.then(desktopService).should(BDDMockito.timeout(5000)).browse(ENGLISH_LOGIN_URL);
  }

  private void givenFrenchLanglinksForLoginPage(final List<Langlink> langlinks) {
    mockApiLanglinks
        .givenRetrieveLanglinksByTitle(
            WikipediaDefinitions.EN, List.of(LOGIN_PAGE), FRENCH_URL_QUERY)
        .willReturn(List.of(new Page(langlinks, false, 4, 1234, LOGIN_PAGE)));
  }

  private void displayHelpForLoginWindow()
      throws InterruptedException, ExecutionException, TimeoutException {
    final AtomicReference<JavaFxLoginWindow> window = new AtomicReference<>();
    runOnJavaFx(
        () -> {
          window.set(new JavaFxLoginWindow(loginWindowServices));
          helpAction.displayHelp(window.get());
        });
    BDDMockito.then(desktopService).should(BDDMockito.timeout(5000)).browse(BDDMockito.anyString());
    runOnJavaFx(() -> window.get().getStage().close());
  }
}
