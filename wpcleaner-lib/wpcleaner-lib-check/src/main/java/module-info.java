/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

@org.jspecify.annotations.NullMarked
module org.wpcleaner.lib.check {
  requires transitive org.jspecify;
  requires transitive org.wpcleaner.api;
  requires transitive spring.context;
  requires org.slf4j;
  requires org.yaml.snakeyaml;
  exports org.wpcleaner.lib.check.algorithm;
  exports org.wpcleaner.lib.check.configuration;
  exports org.wpcleaner.lib.check.server;
  exports org.wpcleaner.lib.check;
  opens org.wpcleaner.lib.check.algorithm;
  opens org.wpcleaner.lib.check.configuration;
  opens org.wpcleaner.lib.check.server;
  opens org.wpcleaner.lib.check;
}
