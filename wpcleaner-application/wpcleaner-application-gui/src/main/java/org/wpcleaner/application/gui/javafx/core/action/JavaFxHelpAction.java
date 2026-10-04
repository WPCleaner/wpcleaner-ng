package org.wpcleaner.application.gui.javafx.core.action;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.wpcleaner.api.api.query.prop.langlinks.ApiLanglinks;
import org.wpcleaner.api.api.query.prop.langlinks.Langlink;
import org.wpcleaner.api.api.query.prop.langlinks.LanglinksParameters;
import org.wpcleaner.api.api.query.prop.langlinks.LanglinksQuery;
import org.wpcleaner.api.api.query.prop.langlinks.Page;
import org.wpcleaner.api.wiki.definition.WikipediaDefinitions;
import org.wpcleaner.application.gui.core.action.HelpAction;
import org.wpcleaner.application.gui.core.desktop.DesktopService;
import org.wpcleaner.application.gui.javafx.JavaFxInitializer;
import org.wpcleaner.application.gui.javafx.core.window.JavaFxWindow;

@Service
public record JavaFxHelpAction(ApiLanglinks apiLanglinks, DesktopService desktopService)
    implements HelpAction {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  private static final String DEFAULT_PAGE = "Wikipedia:WPCleanerNG";
  private static final String PREFIX = "Wikipedia:WPCleanerNG/";

  public void displayHelp(final JavaFxWindow<?> window) {
    displayHelp(window.getHelpPage());
  }

  @Override
  public void displayHelp(@Nullable final String helpPage) {
    final Thread thread =
        new Thread(
            () -> {
              final String url = getHelpUrl(helpPage);
              JavaFxInitializer.browse(desktopService, url);
            });
    thread.setDaemon(true);
    thread.start();
  }

  private String getEnglishPageName(@Nullable final String helpPage) {
    if (helpPage != null && !helpPage.isBlank()) {
      return PREFIX + helpPage;
    }
    return DEFAULT_PAGE;
  }

  public String getHelpUrl(@Nullable final String helpPage) {
    return getHelpUrl(helpPage, Locale.getDefault().getLanguage());
  }

  @SuppressWarnings("PMD.AvoidCatchingGenericException")
  public String getHelpUrl(@Nullable final String helpPage, @Nullable final String userLanguage) {
    final String englishPageName = getEnglishPageName(helpPage);
    final String englishUrl = WikipediaDefinitions.EN.pageUrl(englishPageName);
    if (userLanguage == null
        || userLanguage.isBlank()
        || userLanguage.equalsIgnoreCase(Locale.ENGLISH.getLanguage())) {
      return englishUrl;
    }
    try {
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
    } catch (final Exception e) {
      LOGGER.warn("Failed to retrieve langlinks for {}", englishPageName, e);
      return englishUrl;
    }
  }
}
