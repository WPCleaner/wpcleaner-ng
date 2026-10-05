package org.wpcleaner.application.gui.javafx.core.action;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.wpcleaner.api.api.query.prop.langlinks.ApiLanglinks;
import org.wpcleaner.api.api.query.prop.langlinks.Langlink;
import org.wpcleaner.api.api.query.prop.langlinks.LanglinksParameters;
import org.wpcleaner.api.api.query.prop.langlinks.LanglinksQuery;
import org.wpcleaner.api.api.query.prop.langlinks.Page;
import org.wpcleaner.api.progress.LongRunningTask;
import org.wpcleaner.api.progress.ProgressStep;
import org.wpcleaner.api.progress.ProgressTracker;
import org.wpcleaner.api.utils.GT;
import org.wpcleaner.api.wiki.definition.WikipediaDefinitions;
import org.wpcleaner.application.gui.core.desktop.DesktopService;
import org.wpcleaner.application.gui.javafx.JavaFxInitializer;
import org.wpcleaner.application.gui.javafx.core.window.JavaFxWindow;

@Service
public record JavaFxHelpAction(ApiLanglinks apiLanglinks, DesktopService desktopService) {

  private static final String DEFAULT_PAGE = "Wikipedia:WPCleanerNG";
  private static final String PREFIX = "Wikipedia:WPCleanerNG/";

  public void displayHelp(final JavaFxWindow<?> window) {
    window.executeAsync(
        new RetrieveHelpUrlTask(apiLanglinks, window.getHelpPage()),
        url -> JavaFxInitializer.browse(desktopService, url),
        _ -> JavaFxInitializer.browse(desktopService, getEnglishUrl(window.getHelpPage())));
  }

  static final class RetrieveHelpUrlTask implements LongRunningTask<String> {

    private final ApiLanglinks apiLanglinks;
    @Nullable private final String helpPage;
    private final String userLanguage;

    RetrieveHelpUrlTask(final ApiLanglinks apiLanglinks, @Nullable final String helpPage) {
      this(apiLanglinks, helpPage, Locale.getDefault().getLanguage());
    }

    RetrieveHelpUrlTask(
        final ApiLanglinks apiLanglinks,
        @Nullable final String helpPage,
        final String userLanguage) {
      this.apiLanglinks = apiLanglinks;
      this.helpPage = helpPage;
      this.userLanguage = userLanguage;
    }

    @Override
    public String call(final ProgressTracker tracker) {
      try (ProgressStep _ = tracker.start(GT._T("Retrieving help URL"))) {
        return getHelpUrl(apiLanglinks, helpPage, userLanguage);
      }
    }
  }

  public String getHelpUrl(@Nullable final String helpPage, @Nullable final String userLanguage) {
    return getHelpUrl(apiLanglinks, helpPage, userLanguage);
  }

  private static String getHelpUrl(
      final ApiLanglinks apiLanglinks,
      @Nullable final String helpPage,
      @Nullable final String userLanguage) {
    final String englishPageName = getEnglishPageName(helpPage);
    final String englishUrl = WikipediaDefinitions.EN.pageUrl(englishPageName);
    if (userLanguage == null
        || userLanguage.isBlank()
        || userLanguage.equalsIgnoreCase(Locale.ENGLISH.getLanguage())) {
      return englishUrl;
    }
    final LanglinksQuery options =
        LanglinksQuery.emptyBuilder()
            .language(userLanguage)
            .properties(Set.of(LanglinksParameters.Properties.URL))
            .build();
    final List<Page> pages =
        apiLanglinks.retrieveLanglinksByTitle(
            WikipediaDefinitions.EN, List.of(englishPageName), options);
    return pages.stream()
        .flatMap(page -> page.langlinks().stream())
        .filter(langlink -> userLanguage.equalsIgnoreCase(langlink.lang()))
        .map(Langlink::url)
        .filter(url -> url != null && !url.isBlank())
        .findFirst()
        .orElse(englishUrl);
  }

  private static String getEnglishUrl(@Nullable final String helpPage) {
    return WikipediaDefinitions.EN.pageUrl(getEnglishPageName(helpPage));
  }

  private static String getEnglishPageName(@Nullable final String helpPage) {
    if (helpPage != null && !helpPage.isBlank()) {
      return PREFIX + helpPage;
    }
    return DEFAULT_PAGE;
  }
}
