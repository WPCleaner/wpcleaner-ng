package org.wpcleaner.api.analysis.interwikilink;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import org.jspecify.annotations.Nullable;
import org.wpcleaner.api.analysis.Element;

public record InterwikiLinkElement(
    int begin,
    int end,
    String prefix,
    String target,
    @Nullable String anchor,
    @Nullable String text)
    implements Element {}
