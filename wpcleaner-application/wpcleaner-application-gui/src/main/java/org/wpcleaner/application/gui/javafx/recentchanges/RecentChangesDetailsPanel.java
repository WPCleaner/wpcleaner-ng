package org.wpcleaner.application.gui.javafx.recentchanges;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Set;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.scene.control.TextArea;
import org.jspecify.annotations.Nullable;
import org.wpcleaner.api.api.query.prop.revisions.Page;
import org.wpcleaner.api.api.query.prop.revisions.Revision;
import org.wpcleaner.api.api.query.prop.revisions.RevisionSlot;
import org.wpcleaner.api.api.query.prop.revisions.RevisionsParameters;
import org.wpcleaner.api.api.query.prop.revisions.RevisionsQuery;
import org.wpcleaner.api.utils.GT;
import org.wpcleaner.api.wiki.definition.WikiDefinition;
import org.wpcleaner.application.base.processor.ProgressStep;
import org.wpcleaner.application.gui.javafx.JavaFxProgressTracker;

public final class RecentChangesDetailsPanel extends TextArea {

  private static final org.slf4j.Logger LOGGER =
      org.slf4j.LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final JavaFxRecentChangesWindowServices services;
  private final JavaFxProgressTracker progressTracker;
  private final BooleanProperty loading;

  public RecentChangesDetailsPanel(
      final JavaFxRecentChangesWindowServices services,
      final JavaFxProgressTracker progressTracker,
      final BooleanProperty loading) {
    super();
    this.services = services;
    this.progressTracker = progressTracker;
    this.loading = loading;
    setEditable(false);
    setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
  }

  public void viewModifications(final FilteredRecentChange rc) {
    final Integer revid = rc.revid();
    if (revid == null) {
      return;
    }
    loading.set(true);
    final WikiDefinition wiki = services.user().getCurrentUser().wiki();
    final Thread thread = new Thread(() -> tryRetrieveRevisionContent(wiki, revid));
    thread.setDaemon(true);
    thread.start();
  }

  @SuppressWarnings("PMD.AvoidCatchingGenericException")
  private void tryRetrieveRevisionContent(final WikiDefinition wiki, final Integer revid) {
    try (ProgressStep _ = progressTracker.start(GT._T("Retrieving modifications"))) {
      final String content = retrieveRevisionContent(wiki, revid);
      if (content != null) {
        Platform.runLater(() -> setText(content));
      }
    } catch (final Exception e) {
      LOGGER.error("Error retrieving modifications", e);
    } finally {
      Platform.runLater(() -> loading.set(false));
    }
  }

  @Nullable
  private String retrieveRevisionContent(final WikiDefinition wiki, final Integer revid) {
    final RevisionsQuery query =
        RevisionsQuery.emptyBuilder()
            .properties(Set.of(RevisionsParameters.Properties.CONTENT))
            .slots(Set.of("main"))
            .build();
    final List<Page> pages =
        services.apiRevisions().retrieveRevisionsByRevisionId(wiki, List.of(revid), query);
    if (pages.isEmpty()) {
      return null;
    }
    final Page page = pages.getFirst();
    if (page.revisions().isEmpty()) {
      return null;
    }
    final Revision revision = page.revisions().getFirst();
    if (!revision.slots().containsKey("main")) {
      return null;
    }
    final RevisionSlot mainSlot = revision.slots().get("main");
    if (mainSlot == null) {
      return null;
    }
    return mainSlot.content();
  }
}
