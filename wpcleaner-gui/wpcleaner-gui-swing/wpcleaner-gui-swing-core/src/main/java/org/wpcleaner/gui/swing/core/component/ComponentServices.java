package org.wpcleaner.gui.swing.core.component;

/*
 * SPDX-FileCopyrightText: © 2024 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import org.springframework.stereotype.Service;

@Service
public record ComponentServices(
    ButtonService buttons, LabelService labels, ToolBarService toolBars) {}
