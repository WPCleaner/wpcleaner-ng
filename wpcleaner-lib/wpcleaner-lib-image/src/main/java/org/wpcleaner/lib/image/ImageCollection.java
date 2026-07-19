package org.wpcleaner.lib.image;

/*
 * SPDX-FileCopyrightText: © 2024 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

@SuppressWarnings("SpellCheckingInspection")
public enum ImageCollection {
  DOCUMENT_SAVE("commons/gnome-document-save.png"),
  EDIT("commons/gnome-document-properties.png"),
  HELP("commons/help-browser.png"),
  HELP_ABOUT("commons/breathe-help-about.png"),
  HELP_FAQ("commons/gnome-help-faq.png"),
  LANGUAGE("commons/nuvola-unknown-flag.png"),
  LANGUAGE_ADD("commons/add-language.png"),
  LANGUAGE_EN("commons/nuvola-english-language-flag.png"),
  LANGUAGE_FR("commons/nuvola-france-flag.png"),
  LIST_ADD("commons/list-add.png"),
  LIST_REMOVE("commons/list-remove.png"),
  LOGO_COMMONS("commons/commons-logo.png"),
  LOGO_MEDIAWIKI("commons/mediawiki-2020-icon.png"),
  LOGO_PHABRICATOR("commons/favicon-phabricator-wm.png"),
  LOGO_WIKIBOOKS("commons/wikibooks-logo.png"),
  LOGO_WIKIMEDIA("commons/wikimedia-community-logo.png"),
  LOGO_WIKIPEDIA("commons/wikipedia-logo-v2.png"),
  LOGO_WIKIQUOTE("commons/wikiquote-logo.png"),
  LOGO_WIKISOURCE("commons/wikisource-logo.png"),
  LOGO_WIKIVERSITY("commons/wikiversity-logo.png"),
  LOGO_WIKIVOYAGE("commons/wikivoyage-logo.png"),
  LOGO_WIKTIONARY("commons/wiktionary-logo.png"),
  LOGO_WPCLEANER("commons/nuvola-web-broom.png"),
  OPTIONS("commons/gnome-preferences-other.png"),
  PAGE("commons/gnome-text-x-generic.png"),
  PASSWORD("commons/gnome-dialog-password.png"),
  REFRESH("commons/gnome-view-refresh.png"),
  REFRESH_STOP("commons/gnome-view-refresh-stop.png"),
  SYSTEM_OPTIONS("commons/gnome-preferences-system.png"),
  TAG("commons/codex-icon-tag-color-progressive.png"),
  USER("commons/gnome-face-cool.png"),
  WARNING("commons/gnome-dialog-warning.png");

  private final String filename;

  ImageCollection(final String filename) {
    this.filename = filename;
  }

  String getFilename() {
    return filename;
  }
}
