package org.wpcleaner.api.language;

/*
 * SPDX-FileCopyrightText: © 2024 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import org.wpcleaner.lib.image.ImageCollection;

@SuppressWarnings("PMD.DataClass")
public enum Language {
  EN("en", "English", ImageCollection.LANGUAGE_EN),
  FR("fr", "French", ImageCollection.LANGUAGE_FR);

  public static final Language DEFAULT = EN;

  private final String code;
  private final String description;
  private final ImageCollection image;

  Language(final String code, final String description, final ImageCollection image) {
    this.code = code;
    this.description = description;
    this.image = image;
  }

  public String getCode() {
    return code;
  }

  public String getDescription() {
    return description;
  }

  public ImageCollection getImage() {
    return image;
  }
}
