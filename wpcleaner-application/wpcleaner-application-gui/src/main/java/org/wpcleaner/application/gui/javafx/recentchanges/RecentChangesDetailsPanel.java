package org.wpcleaner.application.gui.javafx.recentchanges;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import com.github.difflib.DiffUtils;
import com.github.difflib.patch.AbstractDelta;
import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.InlineCssTextArea;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wpcleaner.api.api.query.prop.revisions.Page;
import org.wpcleaner.api.api.query.prop.revisions.RevisionSlot;
import org.wpcleaner.api.api.query.prop.revisions.RevisionsParameters;
import org.wpcleaner.api.api.query.prop.revisions.RevisionsQuery;
import org.wpcleaner.api.utils.GT;
import org.wpcleaner.application.gui.javafx.JavaFxImageLoader;
import org.wpcleaner.application.gui.javafx.JavaFxProgressTracker;

public final class RecentChangesDetailsPanel extends VBox {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final JavaFxRecentChangesWindowServices services;
  private final JavaFxProgressTracker progressTracker;
  private final BooleanProperty loading;
  private final InlineCssTextArea contentArea;
  private final RecentChangesDifferencesPanel differencesPanel;
  private final ObjectProperty<@Nullable FilteredRecentChange> selectedRecentChange =
      new SimpleObjectProperty<>(this, "selectedRecentChange");

  public RecentChangesDetailsPanel(
      final JavaFxRecentChangesWindowServices services,
      final JavaFxImageLoader imageLoader,
      final JavaFxProgressTracker progressTracker,
      final BooleanProperty loading) {
    super(10);
    this.services = services;
    this.progressTracker = progressTracker;
    this.loading = loading;

    this.contentArea = new InlineCssTextArea();
    this.contentArea.setEditable(false);
    this.contentArea.setWrapText(true);
    this.contentArea.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
    final VirtualizedScrollPane<InlineCssTextArea> contentAreaScrollPane =
        new VirtualizedScrollPane<>(contentArea);

    this.differencesPanel = new RecentChangesDifferencesPanel();

    final TabPane tabPane = new TabPane();
    tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
    setVgrow(tabPane, Priority.ALWAYS);

    final Tab newTextTab = new Tab(GT._T("New text"), contentAreaScrollPane);
    final Tab differencesTab = new Tab(GT._T("Differences"), differencesPanel);
    tabPane.getTabs().addAll(newTextTab, differencesTab);

    final RecentChangesDetailsToolBar navigationToolBar =
        new RecentChangesDetailsToolBar(
            imageLoader, services.actionServices(), tabPane, differencesTab, differencesPanel);
    navigationToolBar.currentRecentChangeProperty().bind(selectedRecentChange);

    getChildren().addAll(navigationToolBar, tabPane);
    setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
  }

  public void viewModifications(final FilteredRecentChange rc) {
    selectedRecentChange.set(rc);
    contentArea.clear();
    differencesPanel.clear();
    final Integer revid = rc.revId();
    if (revid == null) {
      return;
    }
    loading.set(true);
    final Thread thread = new Thread(() -> tryRetrieveRevisionContent(revid, rc.oldRevId()));
    thread.setDaemon(true);
    thread.start();
  }

  @SuppressWarnings("PMD.AvoidCatchingGenericException")
  private void tryRetrieveRevisionContent(final Integer revId, @Nullable final Integer oldRevId) {
    try (AutoCloseable _ = progressTracker.start(GT._T("Retrieving modifications"))) {
      final List<Page> pages = retrieveRevisionsContent(revId, oldRevId);
      LOGGER.info("Retrieved {} pages for revId {} and oldRevId {}", pages.size(), revId, oldRevId);
      final String content = extractContentForRevision(pages, revId);
      final String oldContent = extractContentForRevision(pages, oldRevId);
      LOGGER.info(
          "Extracted content lengths - content: {}, oldContent: {}",
          content != null ? Integer.toString(content.length()) : "null",
          oldContent != null ? Integer.toString(oldContent.length()) : "null");

      final List<AbstractDelta<Character>> deltas = computeDeltas(content, oldContent);
      LOGGER.info("Computed {} deltas", deltas.size());

      Platform.runLater(
          () -> {
            if (content != null) {
              contentArea.replaceText(content);
              contentArea.setStyle(0, content.length(), "");
            }
            differencesPanel.updateContents(content, oldContent, deltas);
          });
    } catch (final Exception e) {
      LOGGER.error("Error retrieving modifications", e);
    } finally {
      Platform.runLater(() -> loading.set(false));
    }
  }

  private List<AbstractDelta<Character>> computeDeltas(
      @Nullable final String content, @Nullable final String oldContent) {
    if (content == null || oldContent == null) {
      return List.of();
    }
    final List<Character> originalChars = oldContent.chars().mapToObj(c -> (char) c).toList();
    final List<Character> revisedChars = content.chars().mapToObj(c -> (char) c).toList();
    return DiffUtils.diff(originalChars, revisedChars).getDeltas();
  }

  @Nullable
  private String extractContentForRevision(final List<Page> pages, @Nullable final Integer revId) {
    if (revId == null) {
      return null;
    }
    return pages.stream()
        .flatMap(page -> page.revisions().stream())
        .filter(revision -> revId.equals(revision.revId()))
        .map(revision -> revision.slots().get("main"))
        .filter(Objects::nonNull)
        .map(RevisionSlot::content)
        .filter(Objects::nonNull)
        .findFirst()
        .orElse(null);
  }

  private List<Page> retrieveRevisionsContent(
      final Integer revid, @Nullable final Integer oldRevid) {
    final RevisionsQuery query =
        RevisionsQuery.emptyBuilder()
            .properties(
                Set.of(RevisionsParameters.Properties.CONTENT, RevisionsParameters.Properties.IDS))
            .slots(Set.of("main"))
            .build();
    final List<Integer> revIds =
        Stream.of(oldRevid, revid).filter(Objects::nonNull).filter(value -> value != 0).toList();
    return services
        .apiRevisions()
        .retrieveRevisionsByRevisionId(services.user().getCurrentUser().wiki(), revIds, query);
  }
}
