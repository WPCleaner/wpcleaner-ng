package org.wpcleaner.application.gui.javafx.core.action;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.BDDMockito;
import org.wpcleaner.api.api.query.prop.langlinks.ApiLanglinks;
import org.wpcleaner.api.api.query.prop.langlinks.Langlink;
import org.wpcleaner.api.api.query.prop.langlinks.Page;
import org.wpcleaner.api.progress.LongRunningTask;
import org.wpcleaner.api.wiki.definition.WikipediaDefinitions;
import org.wpcleaner.application.gui.core.desktop.DesktopService;
import org.wpcleaner.application.gui.javafx.core.window.JavaFxWindow;

class JavaFxHelpActionTest {

  @DisplayName("getHelpUrl with null help page and English user language returns default page")
  @Test
  void getHelpUrlWithNullHelpPageAndEnglish() {
    final ApiLanglinks apiLanglinks = BDDMockito.mock(ApiLanglinks.class);
    final DesktopService desktopService = BDDMockito.mock(DesktopService.class);
    final JavaFxHelpAction action = new JavaFxHelpAction(apiLanglinks, desktopService);

    final String url = action.getHelpUrl(null, "en");

    Assertions.assertThat(url).isEqualTo(WikipediaDefinitions.EN.pageUrl("Wikipedia:WPCleanerNG"));
    BDDMockito.then(apiLanglinks).shouldHaveNoInteractions();
  }

  @DisplayName("getHelpUrl with blank help page and English user language returns default page")
  @Test
  void getHelpUrlWithBlankHelpPageAndEnglish() {
    final ApiLanglinks apiLanglinks = BDDMockito.mock(ApiLanglinks.class);
    final DesktopService desktopService = BDDMockito.mock(DesktopService.class);
    final JavaFxHelpAction action = new JavaFxHelpAction(apiLanglinks, desktopService);

    final String url = action.getHelpUrl("   ", "en");

    Assertions.assertThat(url).isEqualTo(WikipediaDefinitions.EN.pageUrl("Wikipedia:WPCleanerNG"));
    BDDMockito.then(apiLanglinks).shouldHaveNoInteractions();
  }

  @DisplayName("getHelpUrl with Login help page and English user language returns Login page")
  @Test
  void getHelpUrlWithLoginPageAndEnglish() {
    final ApiLanglinks apiLanglinks = BDDMockito.mock(ApiLanglinks.class);
    final DesktopService desktopService = BDDMockito.mock(DesktopService.class);
    final JavaFxHelpAction action = new JavaFxHelpAction(apiLanglinks, desktopService);

    final String url = action.getHelpUrl("Login", "en");

    Assertions.assertThat(url)
        .isEqualTo(WikipediaDefinitions.EN.pageUrl("Wikipedia:WPCleanerNG/Login"));
    BDDMockito.then(apiLanglinks).shouldHaveNoInteractions();
  }

  @DisplayName("getHelpUrl with Main help page and English user language returns Main page")
  @Test
  void getHelpUrlWithMainPageAndEnglish() {
    final ApiLanglinks apiLanglinks = BDDMockito.mock(ApiLanglinks.class);
    final DesktopService desktopService = BDDMockito.mock(DesktopService.class);
    final JavaFxHelpAction action = new JavaFxHelpAction(apiLanglinks, desktopService);

    final String url = action.getHelpUrl("Main", "en");

    Assertions.assertThat(url)
        .isEqualTo(WikipediaDefinitions.EN.pageUrl("Wikipedia:WPCleanerNG/Main"));
    BDDMockito.then(apiLanglinks).shouldHaveNoInteractions();
  }

  @DisplayName("getHelpUrl with foreign user language returns localized page if langlink exists")
  @Test
  void getHelpUrlWithForeignLanguageWhenLanglinkExists() {
    final ApiLanglinks apiLanglinks = BDDMockito.mock(ApiLanglinks.class);
    final DesktopService desktopService = BDDMockito.mock(DesktopService.class);
    final JavaFxHelpAction action = new JavaFxHelpAction(apiLanglinks, desktopService);

    final String localizedUrl = "https://fr.wikipedia.org/wiki/Wikip%C3%A9dia:WPCleanerNG/Login";
    final Langlink langlink =
        new Langlink("français", "fr", "French", "Wikipédia:WPCleanerNG/Login", localizedUrl);
    final Page page = new Page(List.of(langlink), false, 4, 1234, "Wikipedia:WPCleanerNG/Login");
    BDDMockito.given(
            apiLanglinks.retrieveLanglinksByTitle(
                BDDMockito.eq(WikipediaDefinitions.EN),
                BDDMockito.eq(List.of("Wikipedia:WPCleanerNG/Login")),
                BDDMockito.any()))
        .willReturn(List.of(page));

    final String url = action.getHelpUrl("Login", "fr");

    Assertions.assertThat(url).isEqualTo(localizedUrl);
  }

  @DisplayName("getHelpUrl with foreign user language falls back to English if no langlink exists")
  @Test
  void getHelpUrlWithForeignLanguageWhenNoLanglinkExists() {
    final ApiLanglinks apiLanglinks = BDDMockito.mock(ApiLanglinks.class);
    final DesktopService desktopService = BDDMockito.mock(DesktopService.class);
    final JavaFxHelpAction action = new JavaFxHelpAction(apiLanglinks, desktopService);

    final Page page = new Page(List.of(), false, 4, 1234, "Wikipedia:WPCleanerNG/Login");
    BDDMockito.given(
            apiLanglinks.retrieveLanglinksByTitle(
                BDDMockito.eq(WikipediaDefinitions.EN),
                BDDMockito.eq(List.of("Wikipedia:WPCleanerNG/Login")),
                BDDMockito.any()))
        .willReturn(List.of(page));

    final String url = action.getHelpUrl("Login", "fr");

    Assertions.assertThat(url)
        .isEqualTo(WikipediaDefinitions.EN.pageUrl("Wikipedia:WPCleanerNG/Login"));
  }

  @DisplayName("displayHelp for window extracts help page and delegates to executeAsync")
  @Test
  void displayHelpForWindow() {
    final ApiLanglinks apiLanglinks = BDDMockito.mock(ApiLanglinks.class);
    final DesktopService desktopService = BDDMockito.mock(DesktopService.class);
    final JavaFxHelpAction action = new JavaFxHelpAction(apiLanglinks, desktopService);
    final JavaFxWindow<?> window = BDDMockito.mock(JavaFxWindow.class);
    BDDMockito.given(window.getHelpPage()).willReturn("Login");

    action.displayHelp(window);

    final ArgumentCaptor<LongRunningTask<String>> taskCaptor = ArgumentCaptor.captor();
    BDDMockito.then(window)
        .should()
        .executeAsync(taskCaptor.capture(), BDDMockito.any(), BDDMockito.any());
    final LongRunningTask<String> task = taskCaptor.getValue();
    Assertions.assertThat(task).isNotNull();
    Assertions.assertThat(task.showProgress()).isTrue();
  }
}
