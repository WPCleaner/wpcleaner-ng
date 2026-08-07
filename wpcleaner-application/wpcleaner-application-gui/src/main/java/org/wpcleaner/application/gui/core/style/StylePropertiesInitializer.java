package org.wpcleaner.application.gui.core.style;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.Map;

public interface StylePropertiesInitializer {

  Map<String, StyleProperties> getDefaultStyles();
}
