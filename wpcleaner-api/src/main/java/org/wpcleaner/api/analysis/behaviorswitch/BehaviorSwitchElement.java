package org.wpcleaner.api.analysis.behaviorswitch;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import org.wpcleaner.api.analysis.Element;

public record BehaviorSwitchElement(int begin, int end, String name, String magicWord)
    implements Element {}
