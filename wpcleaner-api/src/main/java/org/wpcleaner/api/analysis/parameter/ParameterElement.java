package org.wpcleaner.api.analysis.parameter;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import org.jspecify.annotations.Nullable;
import org.wpcleaner.api.analysis.Element;

public record ParameterElement(int begin, int end, String name, @Nullable String defaultValue)
    implements Element {}
