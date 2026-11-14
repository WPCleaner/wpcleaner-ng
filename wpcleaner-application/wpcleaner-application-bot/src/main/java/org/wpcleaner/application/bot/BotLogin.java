package org.wpcleaner.application.bot;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.lang.invoke.MethodHandles;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.wpcleaner.api.api.ConnectedUser;
import org.wpcleaner.api.api.Credential;
import org.wpcleaner.api.api.CredentialsProvider;
import org.wpcleaner.api.api.CurrentUserService;
import org.wpcleaner.api.hook.login.LoginWarning;
import org.wpcleaner.api.progress.ProgressTracker;
import org.wpcleaner.api.wiki.definition.WikiDefinition;
import org.wpcleaner.application.base.processor.LoginProcessor;
import org.wpcleaner.application.base.processor.LoginResult;

@Service
public class BotLogin {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final CredentialsProvider credentialsProvider;
  private final CurrentUserService currentUserService;
  private final LoginProcessor loginProcessor;

  public BotLogin(
      final CredentialsProvider credentialsProvider,
      final CurrentUserService currentUserService,
      final LoginProcessor loginProcessor) {
    this.credentialsProvider = credentialsProvider;
    this.currentUserService = currentUserService;
    this.loginProcessor = loginProcessor;
  }

  public void login(final WikiDefinition wiki) {
    final Credential credential =
        credentialsProvider
            .getCredential(wiki)
            .orElseThrow(
                () ->
                    new IllegalArgumentException(
                        "No credentials configured for wiki %s".formatted(wiki.code())));
    final LoginResult result =
        loginProcessor.execute(
            LoginProcessor.Input.forLogin(
                wiki, credential.username(), credential.password().toCharArray()),
            new ProgressTracker(BotLogin::logSteps));
    for (final LoginWarning warning : result.warnings()) {
      LOGGER.warn("Login warning on {}: {}", warning.page(), warning.message());
    }
    final ConnectedUser user = currentUserService.getCurrentUser();
    LOGGER.info(
        "Logged in {} as {} (groups: {})", wiki, user.username(), String.join(", ", user.groups()));
  }

  private static void logSteps(final List<String> steps) {
    if (!steps.isEmpty()) {
      LOGGER.info(steps.getLast());
    }
  }
}
