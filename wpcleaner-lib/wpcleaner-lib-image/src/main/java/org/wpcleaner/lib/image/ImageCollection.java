package org.wpcleaner.lib.image;

/*
 * SPDX-FileCopyrightText: © 2024 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

@SuppressWarnings("PMD.ExcessivePublicCount")
public enum ImageCollection {
  ALERT("commons/codex-icon-alert.png"),
  ALERT_ERROR("commons/codex-icon-alert-color-error.png"),
  ALERT_ORANGE("commons/codex-icon-alert-orange.png"),
  ALERT_PLACEHOLDER("commons/codex-icon-alert-color-placeholder.png"),
  ALERT_PROGRESSIVE("commons/codex-icon-alert-color-progressive.png"),
  ALERT_SUCCESS("commons/codex-icon-alert-color-success.png"),
  ALERT_WARNING("commons/codex-icon-alert-color-warning.png"),
  ALERT_YELLOW("commons/codex-icon-alert-yellow.png"),
  ANALYSIS("commons/gnome-system-run.png"),
  DIFF("commons/gnome-edit-copy.png"),
  DOCUMENT_SAVE("commons/gnome-document-save.png"),
  EDIT("commons/gnome-document-properties.png"),
  ERROR("commons/codex-icon-error.png"),
  ERROR_ERROR("commons/codex-icon-error-color-error.png"),
  ERROR_PLACEHOLDER("commons/codex-icon-error-color-placeholder.png"),
  ERROR_PROGRESSIVE("commons/codex-icon-error-color-progressive.png"),
  ERROR_SUCCESS("commons/codex-icon-error-color-success.png"),
  ERROR_WARNING("commons/codex-icon-error-color-warning.png"),
  GO_FIRST("commons/gnome-go-first.png"),
  GO_LAST("commons/gnome-go-last.png"),
  GO_NEXT("commons/gnome-go-next.png"),
  GO_PREVIOUS("commons/gnome-go-previous.png"),
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
  LOGO_FANDOM("commons/fandom-heart-logo.png"),
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
  MOVE_DOWN("commons/gnome-go-down.png"),
  MOVE_FIRST("commons/gnome-go-top.png"),
  MOVE_LAST("commons/gnome-go-bottom.png"),
  MOVE_UP("commons/gnome-go-up.png"),
  NOTICE("commons/codex-icon-notice.png"),
  NOTICE_ERROR("commons/codex-icon-notice-color-error.png"),
  NOTICE_PLACEHOLDER("commons/codex-icon-notice-color-placeholder.png"),
  NOTICE_PROGRESSIVE("commons/codex-icon-notice-color-progressive.png"),
  NOTICE_SUCCESS("commons/codex-icon-notice-color-success.png"),
  NOTICE_WARNING("commons/codex-icon-notice-color-warning.png"),
  OPEN_URL("commons/gnome-web-browser.png"),
  OPTIONS("commons/gnome-preferences-other.png"),
  PAGE("commons/gnome-text-x-generic.png"),
  PASSWORD("commons/gnome-dialog-password.png"),
  RANDOM("commons/nuvola-apps-atlantik.png"),
  RECENT_CHANGES("commons/gnome-logviewer.png"),
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
