package org.wpcleaner.api.analysis.table;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import org.jspecify.annotations.Nullable;

public record TableCaption(
    int begin, int end, int contentBegin, @Nullable String attributes, String content) {}
