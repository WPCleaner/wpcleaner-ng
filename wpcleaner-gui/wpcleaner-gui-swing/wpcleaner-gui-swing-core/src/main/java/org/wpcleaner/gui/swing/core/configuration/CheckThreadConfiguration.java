package org.wpcleaner.gui.swing.core.configuration;

/*
 * SPDX-FileCopyrightText: © 2024 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import javax.swing.RepaintManager;
import org.springframework.boot.autoconfigure.AutoConfiguration;

@AutoConfiguration
public class CheckThreadConfiguration {

  private static final boolean CHECK_EDT = true;

  public CheckThreadConfiguration() {
    if (CHECK_EDT) {
      RepaintManager.setCurrentManager(new CheckThreadViolationRepaintManager(true));
    }
  }
}
