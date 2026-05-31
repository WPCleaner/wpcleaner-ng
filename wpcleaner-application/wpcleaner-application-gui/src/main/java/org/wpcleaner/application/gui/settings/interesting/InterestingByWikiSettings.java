package org.wpcleaner.application.gui.settings.interesting;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import org.wpcleaner.api.settings.SettingsElement;

public record InterestingByWikiSettings(List<String> pages) implements SettingsElement {}
