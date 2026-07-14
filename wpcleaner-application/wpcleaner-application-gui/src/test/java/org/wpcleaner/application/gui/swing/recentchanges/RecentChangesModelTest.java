package org.wpcleaner.application.gui.swing.recentchanges;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.time.Instant;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.DefaultResourceLoader;
import org.wpcleaner.api.api.query.list.recentchanges.RecentChange;
import org.wpcleaner.application.gui.swing.core.component.table.CellRendererFactory;
import org.wpcleaner.application.gui.swing.core.component.table.ColumnConfigurerFactory;
import org.wpcleaner.application.gui.swing.core.image.ImageIconLoader;
import org.wpcleaner.lib.image.ImageLoader;

class RecentChangesModelTest {

  @DisplayName("Display correct values for recent changes model")
  @Test
  void testRecentChangesModelValues() {
    // GIVEN
    final RecentChangesModel model = getRecentChangesModel();

    // THEN
    Assertions.assertThat(model.getRowCount()).isEqualTo(1);
    Assertions.assertThat(model.getColumnCount()).isEqualTo(5);

    Assertions.assertThat(model.getColumnName(0)).isEqualTo("Time");
    Assertions.assertThat(model.getColumnName(1)).isEqualTo("Title");
    Assertions.assertThat(model.getColumnName(2)).isEqualTo("User");
    Assertions.assertThat(model.getColumnName(3)).isEqualTo("Comment");
    Assertions.assertThat(model.getColumnName(4)).isEqualTo("Tags");

    Assertions.assertThat(model.getValueAt(0, 0)).isNotNull().isNotEqualTo("");
    Assertions.assertThat(model.getValueAt(0, 1)).isEqualTo("Title 1");
    Assertions.assertThat(model.getValueAt(0, 2)).isEqualTo("User 1");
    Assertions.assertThat(model.getValueAt(0, 3)).isEqualTo("edit comment");
    Assertions.assertThat(model.getValueAt(0, 4))
        .isInstanceOf(String.class)
        .isEqualTo(
            """
            <html>
            Tags:<br>
            * tag1<br>
            * tag2<br>
            </html>"""
                .stripIndent());
  }

  private static @NonNull RecentChangesModel getRecentChangesModel() {
    final RecentChange change1 =
        new RecentChange(
            null,
            null,
            "edit comment",
            null,
            null,
            null,
            null,
            null,
            100,
            0,
            50,
            1234,
            12,
            "parsed comment",
            null,
            1,
            null,
            5678,
            "hash",
            List.of("tag1", "tag2"),
            Instant.parse("2026-06-18T12:00:00Z"),
            "Title 1",
            "edit",
            "User 1",
            101);
    return new RecentChangesModel(
        List.of(change1),
        new ColumnConfigurerFactory(
            new CellRendererFactory(
                new ImageIconLoader(new ImageLoader(new DefaultResourceLoader())))));
  }
}
