package org.wpcleaner.api.hook.login;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import org.wpcleaner.api.progress.ProgressTracker;
import org.wpcleaner.api.wiki.definition.WikiDefinition;

public interface LoginStep {

  void execute(WikiDefinition wiki, ProgressTracker tracker);
}
