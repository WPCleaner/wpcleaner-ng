package org.wpcleaner.api.analysis.template;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import org.wpcleaner.api.analysis.Element;

public record TemplateElement(
    int begin, int end, String name, List<TemplateParameter> parameters, List<String> modifiers)
    implements Element {

  public TemplateElement {
    parameters = List.copyOf(parameters);
    modifiers = List.copyOf(modifiers);
  }
}
