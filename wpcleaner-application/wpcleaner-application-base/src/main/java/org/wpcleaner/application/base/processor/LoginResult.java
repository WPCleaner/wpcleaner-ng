package org.wpcleaner.application.base.processor;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import org.wpcleaner.api.hook.login.LoginWarning;
import org.wpcleaner.api.wiki.definition.WikiDefinition;

public record LoginResult(WikiDefinition wiki, String username, List<LoginWarning> warnings) {

  public LoginResult {
    warnings = List.copyOf(warnings);
  }
}
