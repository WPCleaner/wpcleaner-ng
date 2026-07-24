package org.wpcleaner.application.gui.javafx.core;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import java.util.Set;
import javafx.util.StringConverter;
import org.controlsfx.control.CheckComboBox;
import org.jspecify.annotations.Nullable;
import org.wpcleaner.api.api.query.list.tags.Tag;

public final class TagCheckComboBox extends CheckComboBox<Tag> {

  public TagCheckComboBox() {
    super();
    setPrefWidth(250);
    setConverter(
        new StringConverter<>() {
          @Override
          public String toString(@Nullable final Tag tag) {
            return tag != null ? tag.name() : "";
          }

          @Override
          public @Nullable Tag fromString(final String string) {
            return null;
          }
        });
  }

  public void setup(final List<Tag> availableTags, @Nullable final Set<String> selectedTagNames) {
    getItems().addAll(availableTags);
    if (selectedTagNames != null) {
      for (final Tag tag : availableTags) {
        if (selectedTagNames.contains(tag.name())) {
          getCheckModel().check(tag);
        }
      }
    }
  }
}
