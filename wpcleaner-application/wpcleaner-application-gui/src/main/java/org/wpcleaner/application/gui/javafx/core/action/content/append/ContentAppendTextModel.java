package org.wpcleaner.application.gui.javafx.core.action.content.append;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public final class ContentAppendTextModel {

  private final BooleanProperty addedByDefault = new SimpleBooleanProperty(false);
  private final StringProperty label = new SimpleStringProperty("");
  private final StringProperty text = new SimpleStringProperty("");

  public ContentAppendTextModel(
      final boolean addedByDefault, final String label, final String text) {
    this.addedByDefault.set(addedByDefault);
    this.label.set(label);
    this.text.set(text);
  }

  public ContentAppendTextModel(final ContentAppendConfigurationText config) {
    this(config.addedByDefault(), config.label(), config.text());
  }

  public BooleanProperty addedByDefaultProperty() {
    return addedByDefault;
  }

  public StringProperty labelProperty() {
    return label;
  }

  public StringProperty textProperty() {
    return text;
  }

  public ContentAppendConfigurationText toConfig() {
    return new ContentAppendConfigurationText(addedByDefault.get(), label.get(), text.get());
  }
}
