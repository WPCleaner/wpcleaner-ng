package org.wpcleaner.application.gui.javafx.core.control;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import org.fxmisc.richtext.GenericStyledArea;

public final class RightToLeftTextWrapping {

  private RightToLeftTextWrapping() {}

  public static void wrapTextUnlessRightToLeft(final GenericStyledArea<?, ?, ?> area) {
    updateWrapText(area, area.getText());
    area.textProperty().addListener((_, _, text) -> updateWrapText(area, text));
  }

  private static void updateWrapText(final GenericStyledArea<?, ?, ?> area, final String text) {
    area.setWrapText(!containsRightToLeft(text));
  }

  private static boolean containsRightToLeft(final String text) {
    return text.codePoints()
        .map(Character::getDirectionality)
        .anyMatch(
            directionality ->
                directionality == Character.DIRECTIONALITY_RIGHT_TO_LEFT
                    || directionality == Character.DIRECTIONALITY_RIGHT_TO_LEFT_ARABIC);
  }
}
