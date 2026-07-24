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
import org.wpcleaner.api.repository.namespace.Namespace;

public final class NamespaceCheckComboBox extends CheckComboBox<Namespace> {

  public NamespaceCheckComboBox() {
    super();
    setPrefWidth(250);
    setConverter(
        new StringConverter<>() {
          @Override
          public String toString(@Nullable final Namespace namespace) {
            return namespace != null ? namespace.name() : "";
          }

          @Override
          public @Nullable Namespace fromString(final String string) {
            return null;
          }
        });
  }

  public void setup(
      final List<Namespace> availableNamespaces, final Set<Integer> selectedNamespaceIds) {
    getItems().addAll(availableNamespaces);
    for (final Namespace ns : availableNamespaces) {
      if (selectedNamespaceIds.contains(ns.id())) {
        getCheckModel().check(ns);
      }
    }
  }
}
