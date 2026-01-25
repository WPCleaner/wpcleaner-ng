package org.wpcleaner.api.api.query.list.users;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

public enum UsersParameters {
  ATTACHED_WIKI("usattachedwiki"),
  PROPERTIES("usprop"),
  USERS("ususers"),
  USER_IDS("ususerids"),
  ;

  public final String value;

  UsersParameters(final String value) {
    this.value = value;
  }

  public enum Properties {
    BLOCK_INFO("blockinfo"),
    CAN_CREATE("cancreate"),
    CENTRAL_IDS("centralids"),
    EDIT_COUNT("editcount"),
    EMAILABLE("emailable"),
    GENDER("gender"),
    GROUPS("groups"),
    GROUP_MEMBERSHIPS("groupmemberships"),
    IMPLICIT_GROUPS("implicitgroups"),
    REGISTRATION("registration"),
    RIGHTS("rights"),
    TEMP_EXPIRED("tempexpired"),
    ;

    public final String value;

    Properties(final String value) {
      this.value = value;
    }
  }
}
