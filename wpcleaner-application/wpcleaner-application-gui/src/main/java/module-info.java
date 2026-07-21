/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

@org.jspecify.annotations.NullMarked
module org.wpcleaner.application.gui {
  requires transitive java.desktop;
  requires transitive javafx.controls;
  requires transitive org.jspecify;
  requires transitive org.wpcleaner.api;
  requires transitive org.wpcleaner.application.base;
  requires transitive org.wpcleaner.lib.image;
  requires transitive spring.boot.autoconfigure;
  requires transitive spring.boot;
  requires transitive spring.context;
  requires transitive spring.core;
  requires com.fasterxml.jackson.databind;
  requires org.slf4j;
  requires spring.web;
  requires static org.jetbrains.annotations;
  exports org.wpcleaner.application.gui.core.action;
  exports org.wpcleaner.application.gui.core.desktop;
  exports org.wpcleaner.application.gui.core.factory;
  exports org.wpcleaner.application.gui.javafx.login;
  exports org.wpcleaner.application.gui.javafx;
  exports org.wpcleaner.application.gui.settings.graphical;
  exports org.wpcleaner.application.gui.settings.interesting;
  exports org.wpcleaner.application.gui.settings.windows;
  exports org.wpcleaner.application.gui.swing.core.action;
  exports org.wpcleaner.application.gui.swing.core.component.table;
  exports org.wpcleaner.application.gui.swing.core.component;
  exports org.wpcleaner.application.gui.swing.core.configuration;
  exports org.wpcleaner.application.gui.swing.core.dialog;
  exports org.wpcleaner.application.gui.swing.core.image;
  exports org.wpcleaner.application.gui.swing.core.layout;
  exports org.wpcleaner.application.gui.swing.core.window;
  exports org.wpcleaner.application.gui.swing.core.worker;
  exports org.wpcleaner.application.gui.swing.core;
  exports org.wpcleaner.application.gui.swing.login;
  exports org.wpcleaner.application.gui.swing.main;
  exports org.wpcleaner.application.gui.swing.recentchanges;
  exports org.wpcleaner.application.gui;
  opens org.wpcleaner.application.gui.core.action;
  opens org.wpcleaner.application.gui.core.desktop;
  opens org.wpcleaner.application.gui.core.factory;
  opens org.wpcleaner.application.gui.javafx.login;
  opens org.wpcleaner.application.gui.javafx;
  opens org.wpcleaner.application.gui.settings.graphical;
  opens org.wpcleaner.application.gui.settings.interesting;
  opens org.wpcleaner.application.gui.settings.windows;
  opens org.wpcleaner.application.gui.swing.core.action;
  opens org.wpcleaner.application.gui.swing.core.component.table;
  opens org.wpcleaner.application.gui.swing.core.component;
  opens org.wpcleaner.application.gui.swing.core.configuration;
  opens org.wpcleaner.application.gui.swing.core.dialog;
  opens org.wpcleaner.application.gui.swing.core.image;
  opens org.wpcleaner.application.gui.swing.core.layout;
  opens org.wpcleaner.application.gui.swing.core.window;
  opens org.wpcleaner.application.gui.swing.core.worker;
  opens org.wpcleaner.application.gui.swing.core;
  opens org.wpcleaner.application.gui.swing.login;
  opens org.wpcleaner.application.gui.swing.main;
  opens org.wpcleaner.application.gui.swing.recentchanges;
  opens org.wpcleaner.application.gui;
}
