package org.wpcleaner.application.gui.javafx.core.pageanalysis;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.Map;
import java.util.Objects;
import org.springframework.boot.test.context.TestComponent;
import org.wpcleaner.api.analysis.PageAnalysisTestFactory;
import org.wpcleaner.application.gui.javafx.JavaFxImageLoader;
import org.wpcleaner.application.gui.javafx.core.pageanalysis.coloration.PageSyntaxColorizer;
import org.wpcleaner.lib.check.CheckAlgorithmRegistry;
import org.wpcleaner.lib.check.configuration.CheckWikiConfiguration;
import org.wpcleaner.lib.check.configuration.CheckWikiConfigurationRepository;
import org.wpcleaner.lib.image.ImageLoader;

@TestComponent
public final class PageAnalysisCheckPaneFactory {

  private final PageSyntaxColorizer colorizer;
  private final ImageLoader imageLoader;
  private final CheckAlgorithmRegistry algorithmRegistry;
  private final CheckWikiConfigurationRepository checkWikiConfigurationRepository;

  public PageAnalysisCheckPaneFactory(
      final PageSyntaxColorizer colorizer,
      final ImageLoader imageLoader,
      final CheckAlgorithmRegistry algorithmRegistry,
      final CheckWikiConfigurationRepository checkWikiConfigurationRepository) {
    this.colorizer = colorizer;
    this.imageLoader = imageLoader;
    this.algorithmRegistry = algorithmRegistry;
    this.checkWikiConfigurationRepository = checkWikiConfigurationRepository;
  }

  public PageAnalysisCheckPane createPane() {
    return new PageAnalysisCheckPane(
        colorizer,
        new JavaFxImageLoader(imageLoader),
        algorithmRegistry,
        PageAnalysisTestFactory.createPageAnalysisFactory());
  }

  public void givenCheckWikiConfiguration(final Map<String, String> properties) {
    checkWikiConfigurationRepository.setConfiguration(
        PageAnalysisTestFactory.WIKI,
        CheckWikiConfiguration.of(
            properties,
            Map.of(),
            Objects.requireNonNull(PageAnalysisTestFactory.WIKI.checkWikiCode())));
  }
}
