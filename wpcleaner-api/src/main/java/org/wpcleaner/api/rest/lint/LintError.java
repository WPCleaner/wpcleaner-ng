package org.wpcleaner.api.rest.lint;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

public record LintError(
    @JsonProperty("type") String type,
    @JsonProperty("dsr") List<@Nullable Integer> dsr,
    @JsonProperty("params") Map<String, @Nullable Object> parameters,
    @JsonProperty("templateInfo") @Nullable TemplateInfo templateInfo) {

  public static final int UNKNOWN_POSITION = -1;

  private static final int BEGIN_INDEX = 0;
  private static final int END_INDEX = 1;

  @JsonCreator
  static LintError of(
      @JsonProperty("type") final String type,
      @JsonProperty("dsr") @Nullable final List<@Nullable Integer> dsr,
      @JsonProperty("params") @Nullable final Object parameters,
      @JsonProperty("templateInfo") @Nullable final TemplateInfo templateInfo) {
    return new LintError(
        type, dsr == null ? List.of() : dsr, toParameters(parameters), templateInfo);
  }

  private static Map<String, @Nullable Object> toParameters(@Nullable final Object parameters) {
    final Map<String, @Nullable Object> result = new LinkedHashMap<>();
    if (parameters instanceof final Map<?, ?> map) {
      map.forEach((name, value) -> result.put(String.valueOf(name), value));
    }
    return result;
  }

  public int begin() {
    return position(BEGIN_INDEX);
  }

  public int end() {
    return position(END_INDEX);
  }

  private int position(final int index) {
    return index < dsr.size()
        ? Objects.requireNonNullElse(dsr.get(index), UNKNOWN_POSITION)
        : UNKNOWN_POSITION;
  }

  public record TemplateInfo(
      @JsonProperty("multiPartTemplateBlock") @Nullable Boolean multiPartTemplateBlock,
      @JsonProperty("name") @Nullable String name) {

    public boolean isMultiPartTemplateBlock() {
      return Boolean.TRUE.equals(multiPartTemplateBlock);
    }
  }
}
