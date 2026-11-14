/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

@org.jspecify.annotations.NullMarked
module org.wpcleaner.application.bot {
  requires transitive com.fasterxml.jackson.annotation;
  requires transitive com.fasterxml.jackson.databind;
  requires transitive org.jspecify;
  requires transitive org.wpcleaner.api;
  requires transitive org.wpcleaner.application.base;
  requires transitive org.wpcleaner.lib.check;
  requires transitive spring.beans;
  requires transitive spring.boot.autoconfigure;
  requires transitive spring.boot;
  requires transitive spring.context;
  requires transitive spring.core;
  requires com.fasterxml.jackson.dataformat.yaml;
  requires java.xml;
  requires org.apache.commons.compress;
  requires org.slf4j;
  exports org.wpcleaner.application.bot.content;
  exports org.wpcleaner.application.bot.request;
  exports org.wpcleaner.application.bot.script;
  exports org.wpcleaner.application.bot.task.contentcurrent;
  exports org.wpcleaner.application.bot.task;
  exports org.wpcleaner.application.bot;
  opens org.wpcleaner.application.bot.content;
  opens org.wpcleaner.application.bot.request;
  opens org.wpcleaner.application.bot.script;
  opens org.wpcleaner.application.bot.task.contentcurrent;
  opens org.wpcleaner.application.bot.task;
  opens org.wpcleaner.application.bot;
}
