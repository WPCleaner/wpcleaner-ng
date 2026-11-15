package org.wpcleaner.application.gui.javafx.analysis;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.Objects;
import org.wpcleaner.api.repository.linter.LinterCategory;
import org.wpcleaner.api.rest.lint.LintError;

record LinterError(LintError error, LinterCategory category) {

  String label() {
    return Objects.requireNonNullElse(category.label(), error.type());
  }
}
