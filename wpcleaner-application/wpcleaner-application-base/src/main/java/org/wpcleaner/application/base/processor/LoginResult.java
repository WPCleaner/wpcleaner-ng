package org.wpcleaner.application.base.processor;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import org.wpcleaner.api.wiki.definition.WikiDefinition;

public record LoginResult(WikiDefinition wiki, String username) {}
