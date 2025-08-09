package org.wpcleaner.api.api.query.meta.tokens;

/*
 * SPDX-FileCopyrightText: © 2025 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

public enum TokensParameters {
  TYPE("type");

  public final String value;

  TokensParameters(final String value) {
    this.value = value;
  }

  public enum Type {
    CREATE_ACCOUNT("createaccount"),
    CSRF("csrf"),
    DELETE_GLOBAL_ACCOUNT("deleteglobalaccount"),
    LOGIN("login"),
    PATROL("patrol"),
    ROLLBACK("rollback"),
    SET_GLOBAL_ACCOUNT_STATUS("setglobalaccountstatus"),
    USER_RIGHTS("userrights"),
    WATCH("watch"),
    ;

    public final String value;

    Type(final String value) {
      this.value = value;
    }
  }
}
