package org.wpcleaner.api.analysis.template;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.ArrayList;
import java.util.List;

public record TemplateBuilder(String name, List<Parameter> parameters) {

  private static final String TEMPLATE_START = "{{";
  private static final String TEMPLATE_END = "}}";
  private static final char PIPE = '|';
  private static final char EQUAL = '=';

  public TemplateBuilder {
    parameters = List.copyOf(parameters);
  }

  public static TemplateBuilder from(final String name) {
    return new TemplateBuilder(name, List.of());
  }

  public TemplateBuilder withParameter(final String parameterName, final String value) {
    final List<Parameter> newParameters = new ArrayList<>(parameters);
    newParameters.add(new Parameter(parameterName, value));
    return new TemplateBuilder(name, newParameters);
  }

  public String build() {
    final StringBuilder builder = new StringBuilder(TEMPLATE_START).append(name);
    int unnamedCount = 0;
    for (final Parameter parameter : parameters) {
      builder.append(PIPE);
      if (parameter.name().equals(Integer.toString(unnamedCount + 1))
          && parameter.value().indexOf(EQUAL) < 0) {
        unnamedCount++;
      } else {
        builder.append(parameter.name()).append(EQUAL);
      }
      builder.append(parameter.value());
    }
    return builder.append(TEMPLATE_END).toString();
  }

  public record Parameter(String name, String value) {}
}
