package org.wpcleaner.application.gui;

/*
 * SPDX-FileCopyrightText: © 2024 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.wpcleaner.gui.core.factory.LoginWindowFactory;

@SpringBootApplication(scanBasePackages = "org.wpcleaner")
public class WPCleaner {

  public static void main(final String[] args) {
    try (ConfigurableApplicationContext ctx =
        new SpringApplicationBuilder(WPCleaner.class).headless(false).run(args)) {
      ctx.getBean(LoginWindowFactory.class).displayLoginWindow();
    }
  }
}
