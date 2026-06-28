package org.wpcleaner.application.gui.swing.recentchanges;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.Serial;
import java.util.List;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import org.wpcleaner.api.api.query.list.recentchanges.RecentChange;
import org.wpcleaner.api.api.query.list.recentchanges.RecentChangesParameters;
import org.wpcleaner.api.api.query.list.recentchanges.RecentChangesParameters.Properties;
import org.wpcleaner.api.api.query.list.recentchanges.RecentChangesQuery;
import org.wpcleaner.api.wiki.definition.WikiDefinition;
import org.wpcleaner.application.gui.swing.core.window.WPCleanerWindow;

public final class SwingRecentChangesWindow
    extends WPCleanerWindow<SwingRecentChangesWindowServices> {

  @Serial private static final long serialVersionUID = 1L;

  static void create(final SwingRecentChangesWindowServices services) {
    final SwingRecentChangesWindow window = new SwingRecentChangesWindow(services);
    window.initialize();
  }

  private SwingRecentChangesWindow(final SwingRecentChangesWindowServices services) {
    super(services);
  }

  @Override
  protected void initialize() {
    super.initialize();
    setDefaultCloseOperation(DISPOSE_ON_CLOSE);

    final WikiDefinition wiki = services.user().getCurrentUser().wiki();
    final RecentChangesQuery query =
        RecentChangesQuery.builder()
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
    final List<RecentChange> recentChanges =
        services.apiRecentChanges().retrieveRecentChanges(wiki, query);

    final RecentChangesModel model =
        new RecentChangesModel(recentChanges, services.columnFactory());
    final RecentChangesTable table = new RecentChangesTable(model);

    final JPanel panel = new JPanel(new BorderLayout());
    final JScrollPane tableScroll = new JScrollPane(table);
    tableScroll.setMinimumSize(new Dimension(300, 200));
    tableScroll.setPreferredSize(new Dimension(1200, 500));
    tableScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
    panel.add(tableScroll, BorderLayout.CENTER);

    getContentPane().add(panel);
    pack();
    setVisible(true);
  }
}
