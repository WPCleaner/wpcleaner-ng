package org.wpcleaner.settings.local.graphical;

/*
 * SPDX-FileCopyrightText: © 2024 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import jakarta.annotation.Nullable;
import java.util.Objects;
import org.wpcleaner.settings.local.VersionedSettings;

public record GraphicalSettings(int version, LookAndFeelSettings lookAndFeel)
    implements VersionedSettings {

  public static final int LAST_VERSION = 1;

  public GraphicalSettings(final int version, @Nullable final LookAndFeelSettings lookAndFeel) {
    this.version = version;
    this.lookAndFeel = Objects.requireNonNullElseGet(lookAndFeel, LookAndFeelSettings::new);
  }

  public GraphicalSettings() {
    this(0, null);
  }

  @Override
  public int lastVersion() {
    return LAST_VERSION;
  }
}
