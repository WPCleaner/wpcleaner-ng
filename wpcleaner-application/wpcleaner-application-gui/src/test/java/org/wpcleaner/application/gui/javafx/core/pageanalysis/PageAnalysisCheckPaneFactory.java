package org.wpcleaner.application.gui.javafx.core.pageanalysis;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Scene;
import javafx.scene.control.Labeled;
import javafx.scene.input.ContextMenuEvent;
import org.springframework.boot.test.context.TestComponent;
import org.wpcleaner.api.analysis.PageAnalysisTestFactory;
import org.wpcleaner.api.analysis.pagetitle.PageTitle;
import org.wpcleaner.api.repository.configuration.WPCleanerConfiguration;
import org.wpcleaner.api.repository.configuration.WPCleanerConfigurationRepository;
import org.wpcleaner.application.gui.javafx.JavaFxImageLoader;
import org.wpcleaner.application.gui.javafx.core.action.JavaFxActionServices;
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
  private final WPCleanerConfigurationRepository wpcleanerConfigurationRepository;
  private final JavaFxActionServices actionServices;

  public PageAnalysisCheckPaneFactory(
      final PageSyntaxColorizer colorizer,
      final ImageLoader imageLoader,
      final CheckAlgorithmRegistry algorithmRegistry,
      final CheckWikiConfigurationRepository checkWikiConfigurationRepository,
      final WPCleanerConfigurationRepository wpcleanerConfigurationRepository,
      final JavaFxActionServices actionServices) {
    this.colorizer = colorizer;
    this.imageLoader = imageLoader;
    this.algorithmRegistry = algorithmRegistry;
    this.checkWikiConfigurationRepository = checkWikiConfigurationRepository;
    this.wpcleanerConfigurationRepository = wpcleanerConfigurationRepository;
    this.actionServices = actionServices;
  }

  public static void show(final PageAnalysisCheckPane pane) {
    if (pane.getScene() == null) {
      new Scene(pane, 800, 600);
    }
    pane.applyCss();
    pane.layout();
  }

  public static List<Labeled> displayedCells(final PageAnalysisCheckPane pane) {
    show(pane);
    return pane.getErrorsListView().lookupAll(".list-cell").stream()
        .map(Labeled.class::cast)
        .filter(cell -> Objects.nonNull(cell.getText()))
        .toList();
  }

  public static Point2D characterCenter(final PageAnalysisArea area, final int position) {
    final Bounds bounds =
        area.screenToLocal(area.getCharacterBoundsOnScreen(position, position + 1).orElseThrow());
    return new Point2D(bounds.getCenterX(), bounds.getCenterY());
  }

  public static void requestContextMenu(final PageAnalysisArea area, final int position) {
    final Point2D local = characterCenter(area, position);
    final Point2D scene = area.localToScene(local);
    final Point2D screen = area.localToScreen(local);
    area.fireEvent(
        new ContextMenuEvent(
            ContextMenuEvent.CONTEXT_MENU_REQUESTED,
            scene.getX(),
            scene.getY(),
            screen.getX(),
            screen.getY(),
            false,
            null));
  }

  public PageAnalysisCheckPane createPane() {
    return new PageAnalysisCheckPane(
        colorizer,
        new JavaFxImageLoader(imageLoader),
        algorithmRegistry,
        PageAnalysisTestFactory.createPageAnalysisFactory(),
        actionServices::browse);
  }

  public void givenWPCleanerConfiguration(final Map<String, String> generalProperties) {
    wpcleanerConfigurationRepository.setConfiguration(
        PageAnalysisTestFactory.WIKI, new WPCleanerConfiguration(generalProperties, Map.of()));
  }

  public void givenCheckWikiConfiguration(final Map<String, String> properties) {
    givenCheckWikiConfiguration(properties, Set.of());
  }

  public void givenCheckWikiConfiguration(
      final Map<String, String> properties, final Set<PageTitle> whitelist) {
    checkWikiConfigurationRepository.setConfiguration(
        PageAnalysisTestFactory.WIKI,
        CheckWikiConfiguration.of(
                properties,
                Map.of(),
                Objects.requireNonNull(PageAnalysisTestFactory.WIKI.checkWikiCode()))
            .withWhitelists(_ -> whitelist));
  }
}
