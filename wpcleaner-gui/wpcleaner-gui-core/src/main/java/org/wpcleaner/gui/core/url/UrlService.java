package org.wpcleaner.gui.core.url;

/*
 * SPDX-FileCopyrightText: © 2024 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import org.springframework.stereotype.Service;

@Service
public class UrlService {

  private final ReportBugService reportBugService;

  public UrlService(final ReportBugService reportBugService) {
    this.reportBugService = reportBugService;
  }

  public String askQuestion() {
    return "https://en.wikipedia.org/wiki/Wikipedia_talk:WPCleaner/Next_Generation";
  }

  public String help() {
    return "https://en.wikipedia.org/wiki/Wikipedia:WPCleaner/Help";
  }

  public String reportBug() {
    return reportBugService.url();
  }

  public String requestFeature() {
    return "https://phabricator.wikimedia.org/maniphest/task/edit/form/102/?projects=%s&subscribers=%s"
        .formatted("wpcleaner", "NicoV");
  }
}
