package org.wpcleaner.api.repository.linter;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

public record LinterCategory(String name, LinterPriority priority) {}
