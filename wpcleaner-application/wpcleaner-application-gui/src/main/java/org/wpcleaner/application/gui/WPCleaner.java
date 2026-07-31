package org.wpcleaner.application.gui;

/*
 * SPDX-FileCopyrightText: © 2024 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.jspecify.annotations.Nullable;
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
          pattern = "org\\.wpcleaner\\.application\\.gui\\.javafx\\..*")
    })
public class WPCleaner {

  static void main(final String... args) {
    if (needsRelaunch()) {
      relaunch(args);
      return;
    }

    try (ConfigurableApplicationContext ctx =
        new SpringApplicationBuilder(WPCleaner.class).headless(false).run(args)) {
      ctx.getBean(LoginWindowFactory.class).displayLoginWindow();
    }
  }

  private static boolean needsRelaunch() {
    final List<String> inputArgs = ManagementFactory.getRuntimeMXBean().getInputArguments();
    boolean hasNativeAccess = false;
    boolean hasUnsafeMemory = false;
    for (final String arg : inputArgs) {
      if (arg.contains("enable-native-access")) {
        hasNativeAccess = true;
      }
      if (arg.contains("sun-misc-unsafe-memory-access")) {
        hasUnsafeMemory = true;
      }
    }
    return !hasNativeAccess || !hasUnsafeMemory;
  }

  @SuppressWarnings({"PMD.DoNotTerminateVM", "PMD.AvoidCatchingGenericException"})
  private static void relaunch(final String... args) {
    try {
      final String javaExe =
          ProcessHandle.current()
              .info()
              .command()
              .orElseGet(() -> System.getProperty("java.home") + "/bin/java");
      final String classPath = System.getProperty("java.class.path");

      final List<String> command = createCommand(javaExe, classPath);

      command.addAll(Arrays.asList(args));

      final Process process = new ProcessBuilder(command).inheritIO().start();
      System.exit(process.waitFor());
    } catch (final Exception e) {
      try (ConfigurableApplicationContext ctx =
          new SpringApplicationBuilder(WPCleaner.class).headless(false).run(args)) {
        ctx.getBean(LoginWindowFactory.class).displayLoginWindow();
      } catch (final Exception ex) {
        e.addSuppressed(ex);
        throw new IllegalStateException("Failed to launch application", e);
      }
    }
  }

  private static List<String> createCommand(
      final String javaExe, @Nullable final String classPath) {
    final List<String> command = new ArrayList<>();
    command.add(javaExe);
    if (classPath != null && classPath.endsWith(".jar")) {
      command.add("--enable-native-access=ALL-UNNAMED");
    } else {
      command.add("--enable-native-access=ALL-UNNAMED,javafx.graphics");
    }
    command.add("--sun-misc-unsafe-memory-access=allow");

    if (classPath != null && classPath.endsWith(".jar")) {
      command.add("-jar");
      command.add(classPath);
    } else {
      command.add("-cp");
      command.add(classPath != null ? classPath : "");
      command.add(WPCleaner.class.getName());
    }
    return command;
  }
}
