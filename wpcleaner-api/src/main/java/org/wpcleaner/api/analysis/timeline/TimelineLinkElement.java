package org.wpcleaner.api.analysis.timeline;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import org.jspecify.annotations.Nullable;
import org.wpcleaner.api.analysis.Element;

public record TimelineLinkElement(
    int begin, int end, TimelineLinkType type, String target, @Nullable String text)
    implements Element {}
