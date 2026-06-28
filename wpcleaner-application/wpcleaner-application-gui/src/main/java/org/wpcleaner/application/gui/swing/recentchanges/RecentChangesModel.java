package org.wpcleaner.application.gui.swing.recentchanges;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.io.Serial;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;
import org.wpcleaner.api.api.query.list.recentchanges.RecentChange;
import org.wpcleaner.application.gui.swing.core.component.table.ByRowTableModel;
import org.wpcleaner.application.gui.swing.core.component.table.ColumnBuilder;
import org.wpcleaner.application.gui.swing.core.component.table.ColumnConfigurerFactory;
import org.wpcleaner.application.gui.swing.core.component.table.ColumnInformation;
import org.wpcleaner.application.gui.swing.core.component.table.FieldFormatters;
import org.wpcleaner.lib.image.ImageCollection;

public final class RecentChangesModel extends ByRowTableModel<RecentChange> {

  @Serial private static final long serialVersionUID = 1L;

  public RecentChangesModel(
      final List<RecentChange> recentChanges, final ColumnConfigurerFactory columnFactory) {
    super(buildColumns(columnFactory), recentChanges);
  }

  private static List<ColumnInformation<RecentChange, ?>> buildColumns(
      final ColumnConfigurerFactory columnFactory) {
    final ColumnInformation<RecentChange, String> columnComment =
        new ColumnBuilder<>("Comment", RecentChange::comment)
            .withConfigurer(columnFactory.minWidth(250))
            .build();
    final ColumnInformation<RecentChange, List<String>> columnTags =
        new ColumnBuilder<>("Tags", RecentChange::tags)
            .withFieldFormatter(RecentChangesModel::formatTags)
            .withConfigurer(columnFactory.iconWithTooltip(ImageCollection.TAG))
            .build();
    final ColumnInformation<RecentChange, Instant> columnTime =
        new ColumnBuilder<>("Time", RecentChange::timestamp)
            .withFieldFormatter(FieldFormatters::formatTime)
            .withConfigurer(columnFactory.timeField())
            .build();
    final ColumnInformation<RecentChange, String> columnTitle =
        new ColumnBuilder<>("Title", RecentChange::title)
            .withConfigurer(columnFactory.minWidth(200))
            .build();
    final ColumnInformation<RecentChange, String> columnUser =
        new ColumnBuilder<>("User", RecentChange::user)
            .withConfigurer(columnFactory.minWidth(90))
            .build();
    return List.of(columnTime, columnTitle, columnUser, columnComment, columnTags);
  }

  private static String formatTags(final List<String> tags) {
    if (tags.isEmpty()) {
      return "";
    }
    return """
        <html>
        Tags:<br>
        %s
        </html>"""
        .stripIndent()
        .formatted(tags.stream().map("* %s<br>"::formatted).collect(Collectors.joining("\n")));
  }
}
