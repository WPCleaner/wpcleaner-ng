package org.wpcleaner.api.analysis.function;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import org.wpcleaner.api.analysis.Element;
import org.wpcleaner.api.analysis.template.TemplateParameter;

public record FunctionElement(
    int begin, int end, String name, String magicWord, List<TemplateParameter> parameters)
    implements Element {

  public FunctionElement {
    parameters = List.copyOf(parameters);
  }
}
