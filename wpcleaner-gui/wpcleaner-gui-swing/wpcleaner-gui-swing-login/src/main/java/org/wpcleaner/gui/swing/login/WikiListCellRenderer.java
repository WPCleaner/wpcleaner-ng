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
import org.wpcleaner.api.wiki.definition.WikiDefinition;
import org.wpcleaner.gui.swing.core.image.ImageIconLoader;
import org.wpcleaner.lib.image.ImageSize;

public class WikiListCellRenderer extends JLabel implements ListCellRenderer<WikiDefinition> {

  @Serial private static final long serialVersionUID = 124692843637346127L;

  private final ImageIconLoader imageService;

  public WikiListCellRenderer(final ImageIconLoader imageService) {
    this.imageService = imageService;
    setOpaque(true);
    setHorizontalAlignment(LEFT);
    setVerticalAlignment(CENTER);
    setBorder(new EmptyBorder(0, 3, 0, 0));
    setFont(getFont().deriveFont(Font.PLAIN));
  }

  @Override
  public Component getListCellRendererComponent(
      final JList<? extends WikiDefinition> list,
      final WikiDefinition value,
      final int index,
      final boolean isSelected,
      final boolean cellHasFocus) {

    if (value == null) {
      setText("");
      setIcon(null);
      return this;
    }

    setText("%s - %s".formatted(value.code(), value.name()));
    imageService.getImage(value.image(), ImageSize.MENU).ifPresent(this::setIcon);

    return this;
  }
}
