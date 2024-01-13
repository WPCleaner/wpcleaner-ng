package org.wpcleaner.gui.swing.login;

/*
 * SPDX-FileCopyrightText: © 2024 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.io.Serial;
import javax.swing.JComboBox;
import org.wpcleaner.api.language.Language;

public class LanguageComboBox extends JComboBox<Language> {

  @Serial private static final long serialVersionUID = 5378793628036793605L;

  public LanguageComboBox() {
    super(Language.values());
    setEditable(false);
    setSelectedItem(Language.defaultLanguage());
  }
}
