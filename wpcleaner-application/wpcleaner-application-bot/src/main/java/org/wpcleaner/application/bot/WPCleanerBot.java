package org.wpcleaner.application.bot;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication(scanBasePackages = "org.wpcleaner")
public class WPCleanerBot {

  static void main(final String... args) {
    final int exitCode;
    try (ConfigurableApplicationContext context =
        new SpringApplicationBuilder(WPCleanerBot.class)
            .web(WebApplicationType.NONE)
            .headless(true)
            .run(args)) {
      exitCode = context.getBean(BotRunner.class).run(args);
    }
    System.exit(exitCode);
  }
}
