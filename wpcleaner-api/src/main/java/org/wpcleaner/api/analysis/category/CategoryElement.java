package org.wpcleaner.api.analysis.category;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import org.jspecify.annotations.Nullable;
import org.wpcleaner.api.analysis.Element;

public record CategoryElement(
    int begin, int end, String namespace, String name, @Nullable String sortKey)
    implements Element {}
