package org.wpcleaner.application.gui.settings.windows;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import org.wpcleaner.api.settings.SettingsElement;

public record WindowSettings(int x, int y, int width, int height) implements SettingsElement {}
