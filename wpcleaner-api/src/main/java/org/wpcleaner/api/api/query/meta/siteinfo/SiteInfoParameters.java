package org.wpcleaner.api.api.query.meta.siteinfo;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

public enum SiteInfoParameters {
  FILTER_INTERWIKI("sifilteriw"),
  NUMBER_IN_GROUP("sinumberingroup"),
  PROPERTIES("siprop"),
  SHOW_IMAGE_LIST("sishowimagelist"),
  ;

  public final String value;

  SiteInfoParameters(final String value) {
    this.value = value;
  }

  public enum FilterInterwiki {
    LOCAL("local"),
    NOT_LOCAL("!local"),
    ;

    public final String value;

    FilterInterwiki(final String value) {
      this.value = value;
    }
  }

  public enum Properties {
    DB_REPLICATION_LAG("dbrepllag"),
    DEFAULT_OPTIONS("defaultoptions"),
    EXTENSION_TAGS("extensiontags"),
    EXTENSIONS("extensions"),
    FILE_EXTENSIONS("fileextensions"),
    FUNCTION_HOOKS("functionhooks"),
    GENERAL("general"),
    INTERWIKI_MAP("interwikimap"),
    LANGUAGE_VARIANTS("languagevariants"),
    LANGUAGES("languages"),
    LIBRARIES("libraries"),
    MAGIC_WORDS("magicwords"),
    NAMESPACE_ALIASES("namespacealiases"),
    NAMESPACES("namespaces"),
    PROTOCOLS("protocols"),
    RESTRICTIONS("restrictions"),
    RIGHTS_INFO("rightsinfo"),
    SHOW_HOOKS("showhooks"),
    SKINS("skins"),
    SPECIAL_PAGE_ALIASES("specialpagealiases"),
    STATISTICS("statistics"),
    UPLOAD_DIALOG("uploaddialog"),
    USER_GROUPS("usergroups"),
    VARIABLES("variables"),
    ;

    public final String value;

    Properties(final String value) {
      this.value = value;
    }
  }
}
