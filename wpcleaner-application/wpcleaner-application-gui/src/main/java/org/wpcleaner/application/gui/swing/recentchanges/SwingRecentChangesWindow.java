package org.wpcleaner.application.gui.swing.recentchanges;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.Serial;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.Timer;
import org.jspecify.annotations.Nullable;
import org.wpcleaner.api.api.query.list.recentchanges.RecentChange;
import org.wpcleaner.api.api.query.list.recentchanges.RecentChangesParameters;
import org.wpcleaner.api.api.query.list.recentchanges.RecentChangesParameters.Properties;
import org.wpcleaner.api.api.query.list.recentchanges.RecentChangesQuery;
import org.wpcleaner.api.wiki.definition.WikiDefinition;
import org.wpcleaner.application.gui.swing.core.component.table.ByRowTableModel;
import org.wpcleaner.application.gui.swing.core.window.WPCleanerWindow;

public final class SwingRecentChangesWindow
    extends WPCleanerWindow<SwingRecentChangesWindowServices> {

  @Serial private static final long serialVersionUID = 1L;
  private static final int MAX_RECENT_CHANGES = 1000;
  private static final Duration RECENT_CHANGES_OVERLAP = Duration.ofSeconds(10);
  private static final RecentChangesQuery DEFAULT_QUERY =
      RecentChangesQuery.emptyBuilder()
          .limit("max")
          .properties(
              List.of(
                  Properties.COMMENT,
                  Properties.IDS,
                  Properties.TAGS,
                  Properties.TIMESTAMP,
                  Properties.TITLE,
                  Properties.USER))
          .type(List.of(RecentChangesParameters.Type.EDIT, RecentChangesParameters.Type.NEW))
          .build();

  private final RecentChangesModel model;
  private final Timer timerRecentChanges;
  @Nullable private Instant lastRecentChange;

  static void create(final SwingRecentChangesWindowServices services) {
    final SwingRecentChangesWindow window = new SwingRecentChangesWindow(services);
    window.initialize();
  }

  private SwingRecentChangesWindow(final SwingRecentChangesWindowServices services) {
    super(services);
    this.model = new RecentChangesModel(List.of(), services.columnFactory());
    this.timerRecentChanges = new Timer(60_000, _ -> refreshList());
  }

  @Override
  protected void initialize() {
    super.initialize();
    setDefaultCloseOperation(DISPOSE_ON_CLOSE);

    final RecentChangesTable table = new RecentChangesTable(model);
    refreshList();

    final JPanel panel = new JPanel(new BorderLayout());
    final JScrollPane tableScroll = new JScrollPane(table);
    tableScroll.setMinimumSize(new Dimension(300, 200));
    tableScroll.setPreferredSize(new Dimension(1200, 500));
    tableScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
    panel.add(tableScroll, BorderLayout.CENTER);

    getContentPane().add(panel);
    pack();
    setVisible(true);

    timerRecentChanges.start();
  }

  private void refreshList() {
    final WikiDefinition wiki = services.user().getCurrentUser().wiki();
    final RecentChangesQuery query = DEFAULT_QUERY.builder().end(lastRecentChange).build();
    final List<RecentChange> recentChanges =
        services.apiRecentChanges().retrieveRecentChanges(wiki, query);
    recentChanges.sort(RecentChangeComparator.INSTANCE);
    int currentRowIndex = 0;
    try (ByRowTableModel<RecentChange>.RowChanger rowChanger = model.rowChanger()) {
      for (final RecentChange rc : recentChanges) {
        while (currentRowIndex < model.getRowCount()
            && RecentChangeComparator.INSTANCE.compare(rc, model.getValue(currentRowIndex)) > 0) {
          currentRowIndex++;
        }
        if (currentRowIndex >= model.getRowCount()
            || !Objects.equals(rc, model.getValue(currentRowIndex))) {
          rowChanger.addValue(currentRowIndex, rc);
          currentRowIndex++;
        }
      }
      while (model.getRowCount() > MAX_RECENT_CHANGES) {
        rowChanger.removeValue(model.getRowCount() - 1);
      }
    }
    if (!recentChanges.isEmpty()) {
      lastRecentChange =
          Optional.ofNullable(recentChanges.getFirst().timestamp())
              .map(instant -> instant.minus(RECENT_CHANGES_OVERLAP))
              .orElse(null);
    }
  }
}
