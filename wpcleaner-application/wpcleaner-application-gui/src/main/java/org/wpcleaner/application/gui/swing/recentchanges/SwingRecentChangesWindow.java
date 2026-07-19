package org.wpcleaner.application.gui.swing.recentchanges;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.Serial;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import javax.swing.AbstractButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.ScrollPaneConstants;
import javax.swing.Timer;
import org.jspecify.annotations.Nullable;
import org.wpcleaner.api.api.query.list.recentchanges.RecentChange;
import org.wpcleaner.api.api.query.list.recentchanges.RecentChangesParameters;
import org.wpcleaner.api.api.query.list.recentchanges.RecentChangesQuery;
import org.wpcleaner.api.wiki.definition.WikiDefinition;
import org.wpcleaner.application.gui.swing.core.component.table.ByRowTableModel;
import org.wpcleaner.application.gui.swing.core.layout.GridBagComponent;
import org.wpcleaner.application.gui.swing.core.window.WPCleanerWindow;
import org.wpcleaner.lib.image.ImageCollection;
import org.wpcleaner.lib.image.ImageSize;

public final class SwingRecentChangesWindow
    extends WPCleanerWindow<SwingRecentChangesWindowServices> {

  @Serial private static final long serialVersionUID = 1L;
  private static final int MAX_RECENT_CHANGES = 1000;
  private static final Duration RECENT_CHANGES_OVERLAP = Duration.ofSeconds(10);
  private static final RecentChangesQuery DEFAULT_QUERY =
      RecentChangesQuery.emptyBuilder()
          .limit("max")
          .properties(
              Set.of(
                  RecentChangesParameters.Properties.COMMENT,
                  RecentChangesParameters.Properties.IDS,
                  RecentChangesParameters.Properties.TAGS,
                  RecentChangesParameters.Properties.TIMESTAMP,
                  RecentChangesParameters.Properties.TITLE,
                  RecentChangesParameters.Properties.USER))
          .build();

  @Nullable private Instant lastRecentChange;
  private final RecentChangesModel model;
  private final transient RecentChangesOptionsInput optionsInput;
  private final Timer timerRecentChanges;

  static void create(final SwingRecentChangesWindowServices services) {
    final SwingRecentChangesWindow window = new SwingRecentChangesWindow(services);
    window.initialize();
  }

  private SwingRecentChangesWindow(final SwingRecentChangesWindowServices services) {
    super(services);
    this.model = new RecentChangesModel(List.of(), services.columnFactory());
    this.optionsInput = new RecentChangesOptionsInput(this, services);
    this.timerRecentChanges = new Timer(60_000, _ -> refreshList());
    timerRecentChanges.setInitialDelay(0);
  }

  @Override
  protected void initialize() {
    super.initialize();
    setTitle("WPCleaner - Recent changes");
    setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    final JPanel panel = new JPanel(new GridBagLayout());
    final GridBagConstraints constraints = services.swing().layout().initializeConstraints();

    addButtons(panel, constraints);
    addTable(panel, constraints);

    getContentPane().add(panel);
    pack();
    setVisible(true);
  }

  private void addButtons(final JPanel panel, final GridBagConstraints constraints) {
    final AbstractButton refresh =
        services
            .swing()
            .component()
            .buttons()
            .builder("Refresh list", false)
            .withIcon(ImageCollection.REFRESH_STOP, ImageSize.BUTTON)
            .withSelectedIcon(ImageCollection.REFRESH)
            .withToggle(true)
            .withAction(this::manageRefresh)
            .build();
    final JToolBar toolbar =
        services
            .swing()
            .component()
            .toolBars()
            .builder()
            .withComponent(refresh)
            .withComponent(optionsInput.getComboBox())
            .withComponent(optionsInput.getEditButton())
            .withComponent(optionsInput.getAddButton())
            .withComponent(optionsInput.getRemoveButton())
            .build();
    services.swing().layout().addRowSpanningAllColumns(panel, constraints, toolbar);
  }

  private void addTable(final JPanel panel, final GridBagConstraints constraints) {
    final RecentChangesTable table = new RecentChangesTable(model);
    final JScrollPane tableScroll = new JScrollPane(table);
    tableScroll.setMinimumSize(new Dimension(300, 200));
    tableScroll.setPreferredSize(new Dimension(1200, 500));
    tableScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
    services
        .swing()
        .layout()
        .addRow(
            panel,
            constraints,
            GridBagComponent.of(
                tableScroll,
                cellConstraints -> {
                  cellConstraints.fill = GridBagConstraints.BOTH;
                  cellConstraints.weightx = 1;
                  cellConstraints.weighty = 1;
                }));
  }

  private void manageRefresh(final Component component) {
    if (!(component instanceof AbstractButton button)) {
      return;
    }
    if (button.isSelected() && !timerRecentChanges.isRunning()) {
      timerRecentChanges.start();
    } else if (!button.isSelected() && timerRecentChanges.isRunning()) {
      timerRecentChanges.stop();
    }
  }

  private void refreshList() {
    final WikiDefinition wiki = services.user().getCurrentUser().wiki();
    final RecentChangesOptions currentOptions = optionsInput.getSelectedOptions();
    final RecentChangesQuery query =
        DEFAULT_QUERY
            .builder()
            .end(lastRecentChange)
            .namespace(currentOptions.namespace())
            .show(currentOptions.show())
            .tag(currentOptions.tag())
            .topOnly(currentOptions.topOnly())
            .type(currentOptions.type())
            .build();
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
