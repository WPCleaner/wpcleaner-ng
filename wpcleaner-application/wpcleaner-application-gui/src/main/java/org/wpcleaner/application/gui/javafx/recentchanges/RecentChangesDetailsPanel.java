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
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
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
import org.wpcleaner.lib.image.ImageCollection;
import org.wpcleaner.lib.image.ImageSize;

public final class RecentChangesDetailsPanel extends VBox {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final JavaFxRecentChangesWindowServices services;
  private final JavaFxProgressTracker progressTracker;
  private final BooleanProperty loading;
  private final TextField titleField;
  private final InlineCssTextArea contentArea;
  private final RecentChangesDifferencesPanel differencesPanel;

  public RecentChangesDetailsPanel(
      final JavaFxRecentChangesWindowServices services,
      final JavaFxImageLoader imageLoader,
      final JavaFxProgressTracker progressTracker,
      final BooleanProperty loading) {
    super(10);
    this.services = services;
    this.progressTracker = progressTracker;
    this.loading = loading;

    final Label pageLabel = new Label(GT._T("Page:"));
    imageLoader
        .getImageView(ImageCollection.PAGE, ImageSize.LABEL)
        .ifPresent(pageLabel::setGraphic);
    this.titleField = new TextField();
    this.titleField.setEditable(false);

    final HBox pageLine = new HBox(10);
    pageLine.setAlignment(Pos.CENTER_LEFT);
    pageLine.getChildren().addAll(pageLabel, titleField);
    HBox.setHgrow(titleField, Priority.ALWAYS);

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

    getChildren().addAll(pageLine, tabPane);
    setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
  }

  public void viewModifications(final FilteredRecentChange rc) {
    titleField.setText(rc.title());
    contentArea.clear();
    differencesPanel.clear();
    final Integer revid = rc.revid();
    if (revid == null) {
      return;
    }
    loading.set(true);
    final Thread thread = new Thread(() -> tryRetrieveRevisionContent(revid, rc.oldRevid()));
    thread.setDaemon(true);
    thread.start();
  }

  @SuppressWarnings("PMD.AvoidCatchingGenericException")
  private void tryRetrieveRevisionContent(final Integer revid, @Nullable final Integer oldRevid) {
    try (AutoCloseable _ = progressTracker.start(GT._T("Retrieving modifications"))) {
      final List<Page> pages = retrieveRevisionsContent(revid, oldRevid);
      LOGGER.info("Retrieved {} pages for revid {} and oldRevid {}", pages.size(), revid, oldRevid);
      final String content = extractContentForRevision(pages, revid);
      final String oldContent = extractContentForRevision(pages, oldRevid);
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
  private String extractContentForRevision(final List<Page> pages, @Nullable final Integer revid) {
    if (revid == null) {
      return null;
    }
    return pages.stream()
        .flatMap(page -> page.revisions().stream())
        .filter(revision -> revid.equals(revision.revid()))
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
