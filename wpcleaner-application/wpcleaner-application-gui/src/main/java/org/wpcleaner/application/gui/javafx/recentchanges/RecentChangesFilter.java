package org.wpcleaner.application.gui.javafx.recentchanges;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.jspecify.annotations.Nullable;
import org.wpcleaner.api.api.query.list.recentchanges.RecentChange;
import org.wpcleaner.api.api.query.list.recentchanges.RecentChangesParameters;
import org.wpcleaner.api.utils.GT;

public record RecentChangesFilter(
    String name,
    Set<Integer> namespace,
    @Nullable Severity severity,
    Set<String> tag,
    Set<Type> type,
    SubPages subPages) {

  public static final RecentChangesFilter ACCEPT_ALL =
      new RecentChangesFilter(
          GT._T("Accept all"), Set.of(), null, Set.of(), Set.of(), SubPages.BOTH);

  public enum SubPages {
    @JsonProperty("both")
    BOTH,
    @JsonProperty("top_pages")
    TOP_PAGES,
    @JsonProperty("sub_pages")
    SUB_PAGES
  }

  public enum Type {
    @JsonProperty("categorize")
    CATEGORIZE("categorize"),
    @JsonProperty("edit")
    EDIT("edit"),
    @JsonProperty("edit-new")
    EDIT_NEW("edit-new"),
    @JsonProperty("external")
    EXTERNAL("external"),
    @JsonProperty("log")
    LOG("log"),
    @JsonProperty("new")
    NEW("new");

    public final String value;

    Type(final String value) {
      this.value = value;
    }
  }

  public RecentChangesFilter(
      final String name,
      final Set<Integer> namespace,
      @Nullable final Severity severity,
      final Set<String> tag,
      final Set<Type> type,
      @Nullable final SubPages subPages) {
    this.name = name;
    this.namespace = namespace;
    this.severity = severity;
    this.tag = tag;
    this.type = type;
    this.subPages = Objects.requireNonNullElse(subPages, SubPages.BOTH);
  }

  public boolean matches(final RecentChange rc, final Collection<RecentChange> recentChanges) {
    return matchesNamespace(rc)
        && matchesTag(rc)
        && matchesType(rc, recentChanges)
        && matchesSubPages(rc);
  }

  public boolean matchesSubPages(final RecentChange rc) {
    return switch (subPages) {
      case BOTH -> true;
      case TOP_PAGES ->
          Optional.ofNullable(rc.title()).map(title -> !title.contains("/")).orElse(Boolean.TRUE);
      case SUB_PAGES ->
          Optional.ofNullable(rc.title()).map(title -> title.contains("/")).orElse(Boolean.FALSE);
    };
  }

  private boolean matchesNamespace(final RecentChange rc) {
    return namespace.isEmpty() || namespace.contains(rc.ns());
  }

  private boolean matchesTag(final RecentChange rc) {
    return tag.isEmpty() || rc.tags().stream().anyMatch(tag::contains);
  }

  private boolean matchesType(final RecentChange rc, final Collection<RecentChange> recentChanges) {
    return type.isEmpty()
        || type.stream()
            .anyMatch(
                t -> {
                  if (t == Type.EDIT_NEW) {
                    return Objects.equals(RecentChangesParameters.Type.EDIT.value, rc.type())
                        && recentChanges.stream()
                            .anyMatch(otherRc -> matchNewFor(otherRc, rc.title(), rc.pageId()));
                  }
                  return Objects.equals(t.value, rc.type());
                });
  }

  private boolean matchNewFor(
      final RecentChange rc, @Nullable final String title, final @Nullable Integer pageId) {
    return Objects.equals(RecentChangesParameters.Type.NEW.value, rc.type())
        && ((title != null && Objects.equals(title, rc.title()))
            || (pageId != null && Objects.equals(pageId, rc.pageId())));
  }
}
