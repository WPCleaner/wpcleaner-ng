package org.wpcleaner.application.gui.core;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.wpcleaner.application.gui.WPCleaner;

class GuiConfigurationTest {

  private final ApplicationContextRunner contextRunner =
      new ApplicationContextRunner().withUserConfiguration(WPCleaner.class);

  @DisplayName("Default GUI configuration loads JavaFX when gui property is missing")
  @Test
  void testDefaultGuiConfigurationLoadsJavaFx() {
    this.contextRunner.run(
        context -> Assertions.assertThat(context).hasSingleBean(JavaFxConfiguration.class));
  }

  @DisplayName("GUI configuration loads JavaFX when gui property is set to javafx")
  @Test
  void testGuiConfigurationLoadsJavaFxWhenPropertySet() {
    this.contextRunner
        .withPropertyValues("gui=javafx")
        .run(context -> Assertions.assertThat(context).hasSingleBean(JavaFxConfiguration.class));
  }
}
