package org.wpcleaner.application.bot;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.wpcleaner.api.wiki.definition.KnownDefinitions;
import org.wpcleaner.api.wiki.definition.WikiDefinition;
import org.wpcleaner.application.bot.script.BotScript;
import org.wpcleaner.application.bot.script.BotScriptReader;
import org.wpcleaner.application.bot.script.BotTask;
import org.wpcleaner.application.bot.task.TaskContext;
import org.wpcleaner.application.bot.task.TaskExecutor;
import org.wpcleaner.lib.check.configuration.CheckWikiConfigurationLoader;

@Service
public class BotRunner {

  public static final int SUCCESS = 0;
  public static final int FAILURE = 1;
  public static final int INVALID_ARGUMENTS = 2;

  private static final Logger LOGGER =
      LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final BotLogin botLogin;
  private final BotScriptReader scriptReader;
  private final CheckWikiConfigurationLoader checkWikiConfigurationLoader;
  private final KnownDefinitions knownDefinitions;
  private final TaskExecutor taskExecutor;

  public BotRunner(
      final BotLogin botLogin,
      final BotScriptReader scriptReader,
      final CheckWikiConfigurationLoader checkWikiConfigurationLoader,
      final KnownDefinitions knownDefinitions,
      final TaskExecutor taskExecutor) {
    this.botLogin = botLogin;
    this.scriptReader = scriptReader;
    this.checkWikiConfigurationLoader = checkWikiConfigurationLoader;
    this.knownDefinitions = knownDefinitions;
    this.taskExecutor = taskExecutor;
  }

  @SuppressWarnings("PMD.AvoidCatchingGenericException")
  public int run(final String... args) {
    final BotArguments arguments;
    final WikiDefinition wiki;
    final List<BotScript> scripts;
    try {
      arguments = BotArguments.parse(args);
      wiki =
          knownDefinitions
              .getDefinition(arguments.wiki())
              .orElseThrow(
                  () ->
                      new IllegalArgumentException("Unknown wiki %s".formatted(arguments.wiki())));
      scripts = scriptReader.read(arguments.scripts());
    } catch (final IllegalArgumentException | IOException e) {
      LOGGER.error(e.getMessage());
      LOGGER.error(BotArguments.USAGE);
      return INVALID_ARGUMENTS;
    }
    final Set<Integer> algorithms =
        scripts.stream()
            .flatMap(script -> script.algorithms().stream())
            .collect(Collectors.toUnmodifiableSet());
    checkWikiConfigurationLoader.addAlgorithms(algorithms);
    try {
      botLogin.login(wiki);
      runScripts(
          arguments,
          scripts,
          new TaskContext(wiki, arguments.contentFolder(), arguments.outputFolder()));
      return SUCCESS;
    } catch (final InterruptedException e) {
      Thread.currentThread().interrupt();
      LOGGER.error("Bot interrupted", e);
      return FAILURE;
    } catch (final IOException | RuntimeException e) {
      LOGGER.error("Bot failure: {}", e.getMessage(), e);
      return FAILURE;
    } finally {
      checkWikiConfigurationLoader.removeAlgorithms(algorithms);
    }
  }

  private void runScripts(
      final BotArguments arguments, final List<BotScript> scripts, final TaskContext context)
      throws IOException, InterruptedException {
    for (int index = 0; index < scripts.size(); index++) {
      LOGGER.info("Running script {}", arguments.scripts().get(index));
      for (final BotTask task : scripts.get(index).tasks()) {
        taskExecutor.execute(task, context);
      }
    }
  }
}
