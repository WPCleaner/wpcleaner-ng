/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

@org.jspecify.annotations.NullMarked
module org.wpcleaner.application.base {
  requires transitive org.jspecify;
  requires transitive org.wpcleaner.api;
  requires transitive spring.context;
  requires transitive spring.core;
  exports org.wpcleaner.application.base.processor;
  exports org.wpcleaner.application.base.utils.url;
  opens org.wpcleaner.application.base.processor;
  opens org.wpcleaner.application.base.utils.url;
}
