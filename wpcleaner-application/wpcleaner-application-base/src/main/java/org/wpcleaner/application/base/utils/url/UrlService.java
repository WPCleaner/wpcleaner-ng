package org.wpcleaner.application.base.utils.url;

/*
 * SPDX-FileCopyrightText: © 2024 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import org.springframework.stereotype.Service;

@Service
public class UrlService {

  private static final String WIKIPEDIA_EN = "https://en.wikipedia.org/wiki";

  public static final String ASK_QUESTION =
      "%s/Wikipedia_talk:WPCleaner/Next_Generation".formatted(WIKIPEDIA_EN);
  public static final String HELP = "%s/Wikipedia:WPCleaner/Help".formatted(WIKIPEDIA_EN);
  public static final String REQUEST_FEATURE =
      "https://phabricator.wikimedia.org/maniphest/task/edit/form/102/?projects=%s&subscribers=%s"
          .formatted("wpcleaner", "NicoV");
  public static final String BOT_PASSWORDS = "https://www.mediawiki.org/wiki/Special:BotPasswords";

  private final ReportBugService reportBugService;

  public UrlService(final ReportBugService reportBugService) {
    this.reportBugService = reportBugService;
  }

  public String reportBug() {
    return reportBugService.url();
  }
}
