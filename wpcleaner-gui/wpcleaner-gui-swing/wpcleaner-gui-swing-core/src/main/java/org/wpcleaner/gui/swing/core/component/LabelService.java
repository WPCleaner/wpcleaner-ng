package org.wpcleaner.gui.swing.core.component;

/*
 * SPDX-FileCopyrightText: © 2024 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import org.springframework.stereotype.Service;
import org.wpcleaner.gui.swing.core.image.ImageIconLoader;

@Service
public class LabelService {

  private final ImageIconLoader imageService;

  LabelService(final ImageIconLoader imageService) {
    this.imageService = imageService;
  }

  public JLabelBuilder builder(final String message, final boolean displayMessage) {
    return new JLabelBuilder(imageService, message, displayMessage);
  }
}
