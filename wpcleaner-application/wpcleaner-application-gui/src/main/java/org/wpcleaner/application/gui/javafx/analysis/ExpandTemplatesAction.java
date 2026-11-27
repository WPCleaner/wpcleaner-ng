package org.wpcleaner.application.gui.javafx.analysis;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.Set;
import org.wpcleaner.api.api.expandtemplates.ExpandTemplates;
import org.wpcleaner.api.api.expandtemplates.ExpandTemplatesParameters;
import org.wpcleaner.api.api.expandtemplates.ExpandTemplatesQuery;
import org.wpcleaner.api.progress.LongRunningTask;
import org.wpcleaner.api.progress.ProgressStep;
import org.wpcleaner.api.progress.ProgressTracker;
import org.wpcleaner.api.utils.GT;
import org.wpcleaner.application.gui.javafx.core.pageanalysis.ExpansionAction;
import org.wpcleaner.application.gui.javafx.core.pageanalysis.PageAnalysisCheckPane;
import org.wpcleaner.application.gui.javafx.core.window.JavaFxWindow;
import org.wpcleaner.lib.check.CheckError;
import org.wpcleaner.lib.check.ExpandedReplacement;

final class ExpandTemplatesAction implements ExpansionAction {

  private final JavaFxWindow<?> owner;
  private final JavaFxAnalysisWindowServices services;
  private final String pageName;
  private final PageAnalysisCheckPane checkPane;

  ExpandTemplatesAction(
      final JavaFxWindow<?> owner,
      final JavaFxAnalysisWindowServices services,
      final String pageName,
      final PageAnalysisCheckPane checkPane) {
    this.owner = owner;
    this.services = services;
    this.pageName = pageName;
    this.checkPane = checkPane;
  }

  @Override
  public void expand(final CheckError error, final ExpandedReplacement expandedReplacement) {
    final String text = checkPane.getArea().getText();
    owner.executeAsync(
        new ExpandTemplatesTask(expandedReplacement.text()),
        expandTemplates -> applyExpansion(error, text, expandTemplates),
        this::expansionWithError);
  }

  private final class ExpandTemplatesTask implements LongRunningTask<ExpandTemplates> {

    private final String text;

    private ExpandTemplatesTask(final String text) {
      this.text = text;
    }

    @Override
    public ExpandTemplates call(final ProgressTracker tracker) {
      try (ProgressStep _ = tracker.start(GT._T("Expanding templates"))) {
        return services
            .apiExpandTemplates()
            .expandTemplates(
                services.user().getCurrentUser().wiki(),
                ExpandTemplatesQuery.emptyBuilder()
                    .properties(Set.of(ExpandTemplatesParameters.Properties.WIKITEXT))
                    .text(text)
                    .title(pageName)
                    .build());
      }
    }
  }

  private void applyExpansion(
      final CheckError error, final String text, final ExpandTemplates expandTemplates) {
    final String wikiText = expandTemplates.wikiText();
    if (wikiText == null) {
      owner.showWarning(
          GT._T("Error expanding templates"), GT._T("No text returned by the expansion."));
      return;
    }
    if (!text.equals(checkPane.getArea().getText())) {
      owner.showWarning(
          GT._T("Error expanding templates"),
          GT._T("The text has been modified during the expansion."));
      return;
    }
    checkPane.replaceError(error, wikiText);
  }

  private void expansionWithError(final Exception e) {
    owner.showWarning(
        GT._T("Error expanding templates"),
        GT._T("Error expanding templates: %s", ErrorMessage.of(e)));
  }
}
