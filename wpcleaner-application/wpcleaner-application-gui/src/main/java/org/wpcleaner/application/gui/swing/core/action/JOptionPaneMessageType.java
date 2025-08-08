package org.wpcleaner.application.gui.swing.core.action;

/*
 * SPDX-FileCopyrightText: © 2025 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import javax.swing.JOptionPane;
import org.intellij.lang.annotations.MagicConstant;

@MagicConstant(
    flags = {
      JOptionPane.ERROR_MESSAGE,
      JOptionPane.INFORMATION_MESSAGE,
      JOptionPane.PLAIN_MESSAGE,
      JOptionPane.QUESTION_MESSAGE,
      JOptionPane.WARNING_MESSAGE
    })
@interface JOptionPaneMessageType {}
