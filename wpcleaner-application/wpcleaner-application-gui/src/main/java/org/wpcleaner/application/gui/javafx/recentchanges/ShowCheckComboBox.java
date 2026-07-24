package org.wpcleaner.application.gui.javafx.recentchanges;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.Set;
import javafx.util.StringConverter;
import org.controlsfx.control.CheckComboBox;
import org.jspecify.annotations.Nullable;
import org.wpcleaner.api.api.query.list.recentchanges.RecentChangesParameters;

public final class ShowCheckComboBox extends CheckComboBox<RecentChangesParameters.Show> {

  public ShowCheckComboBox() {
    super();
    setPrefWidth(250);
    setConverter(
        new StringConverter<>() {
          @Override
          public String toString(final RecentChangesParameters.@Nullable Show show) {
            return show != null ? show.value : "";
          }

          @Override
          public RecentChangesParameters.@Nullable Show fromString(final String string) {
            return null;
          }
        });
  }

  public void setup(final Set<RecentChangesParameters.Show> selectedShows) {
    getItems().addAll(RecentChangesParameters.Show.values());
    for (final RecentChangesParameters.Show show : RecentChangesParameters.Show.values()) {
      if (selectedShows.contains(show)) {
        getCheckModel().check(show);
      }
    }
  }
}
