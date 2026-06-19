package org.wpcleaner.application.gui.swing.recentechanges;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.awt.BorderLayout;
import java.io.Serial;
import java.util.List;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import org.wpcleaner.api.api.query.list.recentchanges.RecentChange;
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
                    Properties.IDS,
                    Properties.TAGS,
                    Properties.TIMESTAMP,
                    Properties.TITLE,
                    Properties.USER))
            .build();
    final List<RecentChange> recentChanges =
        services.apiRecentChanges().retrieveRecentChanges(wiki, query);

    final RecentChangesModel model = new RecentChangesModel(recentChanges);
    final RecentChangesTable table = new RecentChangesTable(model);

    final JPanel panel = new JPanel(new BorderLayout());
    panel.add(new JScrollPane(table), BorderLayout.CENTER);

    getContentPane().add(panel);
    pack();
    setVisible(true);
  }
}
