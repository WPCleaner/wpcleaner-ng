package org.wpcleaner.application.gui.javafx.core.action;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.wpcleaner.api.api.query.prop.langlinks.ApiLanglinks;
import org.wpcleaner.api.api.query.prop.langlinks.Langlink;
import org.wpcleaner.api.api.query.prop.langlinks.Page;
import org.wpcleaner.api.wiki.definition.WikipediaDefinitions;
import org.wpcleaner.application.gui.core.desktop.DesktopService;
import org.wpcleaner.application.gui.javafx.core.window.JavaFxWindow;

class JavaFxHelpActionTest {

  @DisplayName("getHelpUrl with null help page and English user language returns default page")
  @Test
  void getHelpUrlWithNullHelpPageAndEnglish() {
    final ApiLanglinks apiLanglinks = Mockito.mock(ApiLanglinks.class);
    final DesktopService desktopService = Mockito.mock(DesktopService.class);
    final JavaFxHelpAction action = new JavaFxHelpAction(apiLanglinks, desktopService);

    final String url = action.getHelpUrl(null, "en");

    Assertions.assertThat(url).isEqualTo(WikipediaDefinitions.EN.pageUrl("Wikipedia:WPCleanerNG"));
    Mockito.verifyNoInteractions(apiLanglinks);
  }

  @DisplayName("getHelpUrl with blank help page and English user language returns default page")
  @Test
  void getHelpUrlWithBlankHelpPageAndEnglish() {
    final ApiLanglinks apiLanglinks = Mockito.mock(ApiLanglinks.class);
    final DesktopService desktopService = Mockito.mock(DesktopService.class);
    final JavaFxHelpAction action = new JavaFxHelpAction(apiLanglinks, desktopService);

    final String url = action.getHelpUrl("   ", "en");

    Assertions.assertThat(url).isEqualTo(WikipediaDefinitions.EN.pageUrl("Wikipedia:WPCleanerNG"));
    Mockito.verifyNoInteractions(apiLanglinks);
  }

  @DisplayName("getHelpUrl with Login help page and English user language returns Login page")
  @Test
  void getHelpUrlWithLoginPageAndEnglish() {
    final ApiLanglinks apiLanglinks = Mockito.mock(ApiLanglinks.class);
    final DesktopService desktopService = Mockito.mock(DesktopService.class);
    final JavaFxHelpAction action = new JavaFxHelpAction(apiLanglinks, desktopService);

    final String url = action.getHelpUrl("Login", "en");

    Assertions.assertThat(url)
        .isEqualTo(WikipediaDefinitions.EN.pageUrl("Wikipedia:WPCleanerNG/Login"));
    Mockito.verifyNoInteractions(apiLanglinks);
  }

  @DisplayName("getHelpUrl with Main help page and English user language returns Main page")
  @Test
  void getHelpUrlWithMainPageAndEnglish() {
    final ApiLanglinks apiLanglinks = Mockito.mock(ApiLanglinks.class);
    final DesktopService desktopService = Mockito.mock(DesktopService.class);
    final JavaFxHelpAction action = new JavaFxHelpAction(apiLanglinks, desktopService);

    final String url = action.getHelpUrl("Main", "en");

    Assertions.assertThat(url)
        .isEqualTo(WikipediaDefinitions.EN.pageUrl("Wikipedia:WPCleanerNG/Main"));
    Mockito.verifyNoInteractions(apiLanglinks);
  }

  @DisplayName("getHelpUrl with foreign user language returns localized page if langlink exists")
  @Test
  void getHelpUrlWithForeignLanguageWhenLanglinkExists() {
    final ApiLanglinks apiLanglinks = Mockito.mock(ApiLanglinks.class);
    final DesktopService desktopService = Mockito.mock(DesktopService.class);
    final JavaFxHelpAction action = new JavaFxHelpAction(apiLanglinks, desktopService);

    final String localizedUrl = "https://fr.wikipedia.org/wiki/Wikip%C3%A9dia:WPCleanerNG/Login";
    final Langlink langlink =
        new Langlink("français", "fr", "French", "Wikipédia:WPCleanerNG/Login", localizedUrl);
    final Page page = new Page(List.of(langlink), false, 4, 1234, "Wikipedia:WPCleanerNG/Login");
    Mockito.when(
            apiLanglinks.retrieveLanglinksByTitle(
                Mockito.eq(WikipediaDefinitions.EN),
                Mockito.eq(List.of("Wikipedia:WPCleanerNG/Login")),
                Mockito.any()))
        .thenReturn(List.of(page));

    final String url = action.getHelpUrl("Login", "fr");

    Assertions.assertThat(url).isEqualTo(localizedUrl);
  }

  @DisplayName("getHelpUrl with foreign user language falls back to English if no langlink exists")
  @Test
  void getHelpUrlWithForeignLanguageWhenNoLanglinkExists() {
    final ApiLanglinks apiLanglinks = Mockito.mock(ApiLanglinks.class);
    final DesktopService desktopService = Mockito.mock(DesktopService.class);
    final JavaFxHelpAction action = new JavaFxHelpAction(apiLanglinks, desktopService);

    final Page page = new Page(List.of(), false, 4, 1234, "Wikipedia:WPCleanerNG/Login");
    Mockito.when(
            apiLanglinks.retrieveLanglinksByTitle(
                Mockito.eq(WikipediaDefinitions.EN),
                Mockito.eq(List.of("Wikipedia:WPCleanerNG/Login")),
                Mockito.any()))
        .thenReturn(List.of(page));

    final String url = action.getHelpUrl("Login", "fr");

    Assertions.assertThat(url)
        .isEqualTo(WikipediaDefinitions.EN.pageUrl("Wikipedia:WPCleanerNG/Login"));
  }

  @DisplayName(
      "getHelpUrl with foreign user language falls back to English when API throws exception")
  @Test
  void getHelpUrlWithForeignLanguageWhenApiThrowsException() {
    final ApiLanglinks apiLanglinks = Mockito.mock(ApiLanglinks.class);
    final DesktopService desktopService = Mockito.mock(DesktopService.class);
    final JavaFxHelpAction action = new JavaFxHelpAction(apiLanglinks, desktopService);

    Mockito.when(
            apiLanglinks.retrieveLanglinksByTitle(
                Mockito.eq(WikipediaDefinitions.EN),
                Mockito.eq(List.of("Wikipedia:WPCleanerNG/Login")),
                Mockito.any()))
        .thenThrow(new RuntimeException("API connection timeout"));

    final String url = action.getHelpUrl("Login", "fr");

    Assertions.assertThat(url)
        .isEqualTo(WikipediaDefinitions.EN.pageUrl("Wikipedia:WPCleanerNG/Login"));
  }

  @DisplayName("displayHelp for window extracts help page and delegates")
  @Test
  void displayHelpForWindow() {
    final ApiLanglinks apiLanglinks = Mockito.mock(ApiLanglinks.class);
    final DesktopService desktopService = Mockito.mock(DesktopService.class);
    final JavaFxHelpAction action = new JavaFxHelpAction(apiLanglinks, desktopService);
    final JavaFxWindow<?> window = Mockito.mock(JavaFxWindow.class);
    Mockito.when(window.getHelpPage()).thenReturn("Login");

    action.displayHelp(window);
  }
}
