package org.wpcleaner.application.gui.javafx.recentchanges;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.Set;
import javafx.util.StringConverter;
import org.controlsfx.control.CheckComboBox;
import org.jspecify.annotations.Nullable;

public final class FilterTypeCheckComboBox extends CheckComboBox<RecentChangesFilter.Type> {

  public FilterTypeCheckComboBox() {
    super();
    setPrefWidth(250);
    setConverter(
        new StringConverter<>() {
          @Override
          public String toString(final RecentChangesFilter.@Nullable Type type) {
            return type != null ? type.value : "";
          }

          @Override
          public RecentChangesFilter.@Nullable Type fromString(final String string) {
            return null;
          }
        });
  }

  public void setup(final Set<RecentChangesFilter.Type> selectedTypes) {
    getItems().addAll(RecentChangesFilter.Type.values());
    for (final RecentChangesFilter.Type type : RecentChangesFilter.Type.values()) {
      if (selectedTypes.contains(type)) {
        getCheckModel().check(type);
      }
    }
  }
}
