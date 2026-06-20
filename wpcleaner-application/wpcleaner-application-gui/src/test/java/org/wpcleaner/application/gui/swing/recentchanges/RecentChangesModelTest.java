package org.wpcleaner.application.gui.swing.recentchanges;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.time.Instant;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.wpcleaner.api.api.query.list.recentchanges.RecentChange;

class RecentChangesModelTest {

  @DisplayName("Display correct values for recent changes model")
  @Test
  void testRecentChangesModelValues() {
    // GIVEN
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

    final RecentChangesModel model = new RecentChangesModel(List.of(change1));

    // THEN
    Assertions.assertThat(model.getRowCount()).isEqualTo(1);
    Assertions.assertThat(model.getColumnCount()).isEqualTo(4);

    Assertions.assertThat(model.getColumnName(0)).isEqualTo("Timestamp");
    Assertions.assertThat(model.getColumnName(1)).isEqualTo("Title");
    Assertions.assertThat(model.getColumnName(2)).isEqualTo("User");
    Assertions.assertThat(model.getColumnName(3)).isEqualTo("Tags");

    Assertions.assertThat(model.getValueAt(0, 0)).isNotNull().isNotEqualTo("");
    Assertions.assertThat(model.getValueAt(0, 1)).isEqualTo("Title 1");
    Assertions.assertThat(model.getValueAt(0, 2)).isEqualTo("User 1");
    Assertions.assertThat(model.getValueAt(0, 3)).isEqualTo("tag1, tag2");
  }
}
