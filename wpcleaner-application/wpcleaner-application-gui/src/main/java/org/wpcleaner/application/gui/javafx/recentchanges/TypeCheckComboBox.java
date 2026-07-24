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

public final class TypeCheckComboBox extends CheckComboBox<RecentChangesParameters.Type> {

  public TypeCheckComboBox() {
    super();
    setPrefWidth(250);
    setConverter(
        new StringConverter<>() {
          @Override
          public String toString(final RecentChangesParameters.@Nullable Type type) {
            return type != null ? type.value : "";
          }

          @Override
          public RecentChangesParameters.@Nullable Type fromString(final String string) {
            return null;
          }
        });
  }

  public void setup(final Set<RecentChangesParameters.Type> selectedTypes) {
    getItems().addAll(RecentChangesParameters.Type.values());
    for (final RecentChangesParameters.Type type : RecentChangesParameters.Type.values()) {
      if (selectedTypes.contains(type)) {
        getCheckModel().check(type);
      }
    }
  }
}
