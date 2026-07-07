/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

@org.jspecify.annotations.NullMarked
module org.wpcleaner.api {
  requires transitive com.fasterxml.jackson.annotation;
  requires transitive com.fasterxml.jackson.databind;
  requires transitive java.desktop;
  requires transitive org.jspecify;
  requires transitive org.wpcleaner.lib.image;
  requires transitive spring.beans;
  requires transitive spring.context;
  requires transitive spring.core;
  requires transitive spring.web;
  requires com.fasterxml.jackson.datatype.jsr310;
  requires java.prefs;
  requires jdk.net;
  requires org.slf4j;
  requires org.yaml.snakeyaml;
  exports org.wpcleaner.api.api.login;
  exports org.wpcleaner.api.api.query.list.recentchanges;
  exports org.wpcleaner.api.api.query.list.users;
  exports org.wpcleaner.api.api.query.meta.siteinfo;
  exports org.wpcleaner.api.api.query.meta.tokens;
  exports org.wpcleaner.api.api.query;
  exports org.wpcleaner.api.api;
  exports org.wpcleaner.api.hook.login;
  exports org.wpcleaner.api.language;
  exports org.wpcleaner.api.repository.namespace;
  exports org.wpcleaner.api.settings;
  exports org.wpcleaner.api.utils;
  exports org.wpcleaner.api.wiki.builder;
  exports org.wpcleaner.api.wiki.definition;
  opens org.wpcleaner.api.api.login;
  opens org.wpcleaner.api.api.query.list.recentchanges;
  opens org.wpcleaner.api.api.query.list.users;
  opens org.wpcleaner.api.api.query.meta.siteinfo;
  opens org.wpcleaner.api.api.query.meta.tokens;
  opens org.wpcleaner.api.api.query;
  opens org.wpcleaner.api.api;
  opens org.wpcleaner.api.hook.login;
  opens org.wpcleaner.api.language;
  opens org.wpcleaner.api.repository.namespace;
  opens org.wpcleaner.api.settings;
  opens org.wpcleaner.api.utils;
  opens org.wpcleaner.api.wiki.builder;
  opens org.wpcleaner.api.wiki.definition;
}
