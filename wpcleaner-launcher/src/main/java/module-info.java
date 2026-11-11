/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

@org.jspecify.annotations.NullMarked
module org.wpcleaner.launcher {
  requires transitive org.jspecify;
  requires com.fasterxml.jackson.annotation;
  requires com.fasterxml.jackson.databind;
  requires java.desktop;
  requires java.logging;
  requires java.net.http;
  requires static com.google.errorprone.annotations;
  exports org.wpcleaner.launcher.display;
  exports org.wpcleaner.launcher.install;
  exports org.wpcleaner.launcher.platform;
  exports org.wpcleaner.launcher.release;
  exports org.wpcleaner.launcher.runtime;
  exports org.wpcleaner.launcher.shortcut;
  exports org.wpcleaner.launcher.start;
  exports org.wpcleaner.launcher;
  opens org.wpcleaner.launcher.install;
  opens org.wpcleaner.launcher.release;
}
