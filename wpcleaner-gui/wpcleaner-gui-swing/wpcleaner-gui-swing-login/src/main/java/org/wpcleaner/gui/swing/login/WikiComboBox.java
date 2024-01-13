package org.wpcleaner.gui.swing.login;

/*
 * SPDX-FileCopyrightText: © 2024 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.io.Serial;
import javax.swing.JComboBox;
import org.wpcleaner.api.wiki.definition.KnownDefinitions;
import org.wpcleaner.api.wiki.definition.WikiDefinition;

public class WikiComboBox extends JComboBox<WikiDefinition> {

  @Serial private static final long serialVersionUID = -9002570238040204181L;

  public WikiComboBox(final KnownDefinitions wikiDefinitions) {
    super(wikiDefinitions.getDefinitions().toArray(WikiDefinition[]::new));
    setEditable(false);
    setSelectedItem(wikiDefinitions.getPreferred());
  }
}
