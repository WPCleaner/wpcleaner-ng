package org.wpcleaner.api.rest.lint;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

public record LintError(
    @JsonProperty("type") String type,
    @JsonProperty("dsr") @JsonSetter(nulls = Nulls.AS_EMPTY) List<@Nullable Integer> dsr,
    @JsonProperty("params") @JsonSetter(nulls = Nulls.AS_EMPTY)
        Map<String, @Nullable Object> parameters,
    @JsonProperty("templateInfo") @Nullable TemplateInfo templateInfo) {

  public static final int UNKNOWN_POSITION = -1;

  private static final int BEGIN_INDEX = 0;
  private static final int END_INDEX = 1;

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
