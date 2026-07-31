package org.wpcleaner.application.gui.javafx.login;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import org.wpcleaner.lib.image.ImageCollection;

enum WikiBuilderType {
  WIKIPEDIA("Wikipedia", ImageCollection.LOGO_WIKIPEDIA),
  WIKIBOOKS("Wikibooks", ImageCollection.LOGO_WIKIBOOKS),
  WIKIQUOTE("Wikiquote", ImageCollection.LOGO_WIKIQUOTE),
  WIKISOURCE("Wikisource", ImageCollection.LOGO_WIKISOURCE),
  WIKIVERSITY("Wikiversity", ImageCollection.LOGO_WIKIVERSITY),
  WIKIVOYAGE("Wikivoyage", ImageCollection.LOGO_WIKIVOYAGE),
  WIKTIONARY("Wiktionary", ImageCollection.LOGO_WIKTIONARY),
  FANDOM("Fandom", ImageCollection.LOGO_FANDOM),
  GENERIC("Generic", ImageCollection.LOGO_MEDIAWIKI);

  private final String label;
  private final ImageCollection image;

  WikiBuilderType(final String label, final ImageCollection image) {
    this.label = label;
    this.image = image;
  }

  public ImageCollection getImage() {
    return image;
  }

  @Override
  public String toString() {
    return label;
  }
}
