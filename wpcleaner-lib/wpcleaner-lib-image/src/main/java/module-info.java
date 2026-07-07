/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

@org.jspecify.annotations.NullMarked
module org.wpcleaner.lib.image {
  requires transitive org.jspecify;
  requires transitive spring.context;
  requires transitive spring.core;
  exports org.wpcleaner.lib.image;
  opens org.wpcleaner.lib.image;
}
