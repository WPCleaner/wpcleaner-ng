package org.wpcleaner.application.gui.javafx.recentchanges.userpagehosting;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

final class TalkPageTextModel {

  private final StringProperty label = new SimpleStringProperty("");
  private final StringProperty text = new SimpleStringProperty("");
  private final BooleanProperty addedByDefault = new SimpleBooleanProperty(false);

  public TalkPageTextModel(final String label, final String text, final boolean addedByDefault) {
    this.label.set(label);
    this.text.set(text);
    this.addedByDefault.set(addedByDefault);
  }

  public StringProperty labelProperty() {
    return label;
  }

  public StringProperty textProperty() {
    return text;
  }

  public BooleanProperty addedByDefaultProperty() {
    return addedByDefault;
  }
}
