package org.wpcleaner.api.analysis.magiclink;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import org.wpcleaner.api.analysis.Element;
import org.wpcleaner.api.repository.magiclink.MagicLinkType;

public record MagicLinkElement(int begin, int end, MagicLinkType type, String number)
    implements Element {}
