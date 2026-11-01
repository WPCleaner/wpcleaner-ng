package org.wpcleaner.api.analysis.image;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import org.jspecify.annotations.Nullable;

public record ImageParameter(
    int begin, int end, String value, @Nullable String magicWord, @Nullable String argument) {}
