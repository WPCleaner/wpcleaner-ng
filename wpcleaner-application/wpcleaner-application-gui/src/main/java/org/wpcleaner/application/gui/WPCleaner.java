package org.wpcleaner.application.gui;

/*
 * SPDX-FileCopyrightText: © 2024 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.wpcleaner.application.gui.core.factory.LoginWindowFactory;

@SpringBootApplication(scanBasePackages = "org.wpcleaner")
@ComponentScan(
    basePackages = "org.wpcleaner",
    excludeFilters = {
      @ComponentScan.Filter(
          type = FilterType.REGEX,
          pattern = "org\\.wpcleaner\\.application\\.gui\\.swing\\..*"),
      @ComponentScan.Filter(
          type = FilterType.REGEX,
          pattern = "org\\.wpcleaner\\.application\\.gui\\.javafx\\..*")
    })
public class WPCleaner {

  static void main(final String... args) {
    try (ConfigurableApplicationContext ctx =
        new SpringApplicationBuilder(WPCleaner.class).headless(false).run(args)) {
      ctx.getBean(LoginWindowFactory.class).displayLoginWindow();
    }
  }
}
