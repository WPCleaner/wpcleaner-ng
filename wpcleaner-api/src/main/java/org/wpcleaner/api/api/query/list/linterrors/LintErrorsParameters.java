package org.wpcleaner.api.api.query.list.linterrors;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

public enum LintErrorsParameters {
  CATEGORIES("lntcategories"),
  FROM("lntfrom"),
  LIMIT("lntlimit"),
  NAMESPACE("lntnamespace"),
  PAGE_ID("lntpageid"),
  TITLE("lnttitle"),
  ;

  public final String value;

  LintErrorsParameters(final String value) {
    this.value = value;
  }
}
