package org.wpcleaner.gui.swing.login;

/*
 * SPDX-FileCopyrightText: © 2024 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.awt.Component;
import java.awt.Font;
import java.io.Serial;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.border.EmptyBorder;
import org.wpcleaner.api.language.Language;
import org.wpcleaner.gui.swing.core.image.ImageIconLoader;
import org.wpcleaner.lib.image.ImageSize;

public final class LanguageListCellRenderer extends JLabel implements ListCellRenderer<Language> {

  @Serial private static final long serialVersionUID = 1737892581461933296L;

  private final ImageIconLoader imageService;

  public LanguageListCellRenderer(final ImageIconLoader imageService) {
    this.imageService = imageService;
    setOpaque(true);
    setHorizontalAlignment(LEFT);
    setVerticalAlignment(CENTER);
    setBorder(new EmptyBorder(0, 3, 0, 0));
    setFont(getFont().deriveFont(Font.PLAIN));
  }

  @Override
  @SuppressWarnings("CanIgnoreReturnValueSuggester")
  public Component getListCellRendererComponent(
      final JList<? extends Language> list,
      final Language value,
      final int index,
      final boolean isSelected,
      final boolean cellHasFocus) {

    if (value == null) {
      setText("");
      setIcon(null);
      return this;
    }

    setText("%s - %s".formatted(value.getCode(), value.getDescription()));
    imageService.getImage(value.getImage(), ImageSize.MENU).ifPresent(this::setIcon);

    return this;
  }
}
