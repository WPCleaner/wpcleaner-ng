package org.wpcleaner.application.gui.javafx.core.action.content.append;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javafx.scene.control.Control;
import javafx.scene.layout.Pane;
import org.jspecify.annotations.Nullable;
import org.wpcleaner.api.analysis.PageAnalysisFactory;
import org.wpcleaner.api.api.edit.ApiEditWrapper;
import org.wpcleaner.api.api.edit.EditQueryByTitle;
import org.wpcleaner.api.api.edit.EditQueryCommon;
import org.wpcleaner.api.wiki.definition.WikiDefinition;
import org.wpcleaner.application.gui.javafx.core.action.content.PageContentDisplay;
import org.wpcleaner.application.gui.javafx.core.pageanalysis.coloration.PageSyntaxColorizer;

public class ContentAppendAction {

  private final ApiEditWrapper apiEditWrapper;
  private final WikiDefinition wiki;
  private final String pageTitle;
  private final ContentAppendActionControl configurationControl;
  private final PageContentDisplay displayPane;
  private final Map<String, String> variables = new HashMap<>();

  public ContentAppendAction(
      final PageSyntaxColorizer colorizer,
      final PageAnalysisFactory analysisFactory,
      final ApiEditWrapper apiEditWrapper,
      final WikiDefinition wiki,
      final String blockTitle,
      final ContentAppendConfiguration configuration,
      final String pageTitle,
      final String pageContent) {
    this.apiEditWrapper = apiEditWrapper;
    this.wiki = wiki;
    this.pageTitle = Objects.requireNonNull(pageTitle, "pageTitle");
    this.configurationControl = new ContentAppendActionControl(blockTitle, configuration);
    this.displayPane = new PageContentDisplay(colorizer, analysisFactory, pageTitle, pageContent);
  }

  public Control getConfigurationControl() {
    return configurationControl;
  }

  public Pane getDisplayPane() {
    return displayPane;
  }

  public boolean isComplete() {
    return configurationControl.isComplete();
  }

  public void setVariable(final String name, final String value) {
    variables.put(Objects.requireNonNull(name, "name"), Objects.requireNonNull(value, "value"));
  }

  @Nullable
  public String getVariable(final String name) {
    return variables.get(name);
  }

  public void execute() {
    final List<String> selectedTexts = configurationControl.getSelectedTexts();
    if (selectedTexts.isEmpty()) {
      return;
    }
    final List<String> replacedTexts = selectedTexts.stream().map(this::replaceVariables).toList();
    final String textToAppend = "\n\n" + String.join("\n\n", replacedTexts);
    apiEditWrapper.edit(
        wiki,
        new EditQueryByTitle(
            pageTitle,
            EditQueryCommon.emptyBuilder()
                .appendText(textToAppend)
                .summary(configurationControl.getSummary())
                .build()));
  }

  private String replaceVariables(final String text) {
    if (variables.isEmpty() || text == null) {
      return text;
    }
    String result = text;
    for (final Map.Entry<String, String> entry : variables.entrySet()) {
      result = result.replace(entry.getKey(), entry.getValue());
    }
    return result;
  }
}
