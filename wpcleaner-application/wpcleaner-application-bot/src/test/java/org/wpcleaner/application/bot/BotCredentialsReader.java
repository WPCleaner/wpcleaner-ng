package org.wpcleaner.application.bot;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import org.springframework.boot.test.context.TestComponent;
import org.wpcleaner.api.api.Credential;
import org.wpcleaner.api.api.CredentialsReader;
import org.wpcleaner.api.wiki.definition.WikiDefinition;
import org.wpcleaner.api.wiki.definition.WikipediaDefinitions;
import org.wpcleaner.application.base.ApplicationLogin;

@TestComponent
public final class BotCredentialsReader implements CredentialsReader {

  public static final WikiDefinition WIKI = WikipediaDefinitions.FR;
  public static final String USERNAME = "Bot";

  @Override
  public List<Credential> getCredentials() {
    return List.of(new Credential(USERNAME, ApplicationLogin.PASSWORD, WIKI));
  }
}
