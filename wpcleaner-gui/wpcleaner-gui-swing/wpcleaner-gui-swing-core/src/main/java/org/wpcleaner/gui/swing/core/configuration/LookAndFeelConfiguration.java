package org.wpcleaner.gui.swing.core.configuration;

/*
 * SPDX-FileCopyrightText: © 2024 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;

@AutoConfiguration
public class LookAndFeelConfiguration {

  private static final boolean USE_SYSTEM_LF = true;
  private static final Logger LOGGER =
      LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  public LookAndFeelConfiguration(
      @Value("${wpcleaner.graphical.lookandfeel.type:WPCleaner}") final LookAndFeelType type,
      @Value("${wpcleaner.graphical.lookandfeel.name:}") final String name) {
    final Optional<String> lookAndFeelClassName =
        switch (type) {
          case SYSTEM ->
              USE_SYSTEM_LF
                  ? Optional.of(UIManager.getSystemLookAndFeelClassName())
                  : Optional.empty();
          case USER -> getLookAndFeelClassName(name.isBlank() ? "Nimbus" : name);
          case WPCLEANER -> getLookAndFeelClassName("Nimbus");
        };
    lookAndFeelClassName.ifPresent(this::setLookAndFeel);
  }

  private Optional<String> getLookAndFeelClassName(final String name) {
    return Arrays.stream(UIManager.getInstalledLookAndFeels())
        .filter(info -> Objects.equals(name, info.getName()))
        .map(UIManager.LookAndFeelInfo::getClassName)
        .findFirst();
  }

  private void setLookAndFeel(final String className) {
    try {
      UIManager.setLookAndFeel(className);
    } catch (UnsupportedLookAndFeelException
        | ClassNotFoundException
        | InstantiationException
        | IllegalAccessException e) {
      LOGGER.info("Unable to set look and feel {}", className);
    }
  }
}
