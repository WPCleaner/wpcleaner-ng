package org.wpcleaner.gui.swing.core.configuration;

/*
 * SPDX-FileCopyrightText: © 2024 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import javax.swing.JFrame;
import org.springframework.boot.autoconfigure.AutoConfiguration;

@AutoConfiguration
public class JFrameConfiguration {

  public JFrameConfiguration() {
    JFrame.setDefaultLookAndFeelDecorated(true);
  }
}
