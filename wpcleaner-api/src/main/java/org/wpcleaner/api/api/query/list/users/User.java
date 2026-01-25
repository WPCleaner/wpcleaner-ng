package org.wpcleaner.api.api.query.list.users;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import jakarta.annotation.Nullable;
import java.time.Instant;
import java.util.List;

public record User(
    @JsonProperty("attachedlocal") @Nullable AttachedLocal attachedLocal,
    @JsonProperty("centralids") @Nullable CentralIds centralIds,
    @JsonProperty("editcount") @Nullable Integer editcount,
    @JsonProperty("emailable") @Nullable Boolean emailable,
    @JsonProperty("gender") @Nullable String gender,
    @JsonProperty("groupmemberships") @JsonSetter(nulls = Nulls.AS_EMPTY)
        List<GroupMembership> groupMemberships,
    @JsonProperty("groups") @JsonSetter(nulls = Nulls.AS_EMPTY) List<String> groups,
    @JsonProperty("implicitgroups") @JsonSetter(nulls = Nulls.AS_EMPTY) List<String> implicitGroups,
    @JsonProperty("invalid") @Nullable Boolean invalid,
    @JsonProperty("missing") @Nullable Boolean missing,
    @JsonProperty("name") String name,
    @JsonProperty("registration") @Nullable Instant registration,
    @JsonProperty("rights") @JsonSetter(nulls = Nulls.AS_EMPTY) List<String> rights,
    @JsonProperty("tempexpired") @Nullable String tempExpired,
    @JsonProperty("userid") @Nullable Integer userid) {

  public static User ofMissing(final String name) {
    return new User(
        null, null, null, false, null, List.of(), List.of(), List.of(), null, true, name, null,
        List.of(), null, null);
  }

  public record AttachedLocal(
      @JsonProperty("CentralAuth") boolean centralAuth, @JsonProperty("local") boolean local) {}

  public record CentralIds(
      @JsonProperty("CentralAuth") String centralAuth, @JsonProperty("local") String local) {}

  public record GroupMembership(
      @JsonProperty("group") String group, @JsonProperty("expiry") String expiry) {}
}
