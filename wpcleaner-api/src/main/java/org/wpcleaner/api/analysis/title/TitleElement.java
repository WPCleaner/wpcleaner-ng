package org.wpcleaner.api.analysis.title;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import org.wpcleaner.api.analysis.Element;

public record TitleElement(int begin, int end, int level, String title) implements Element {}
