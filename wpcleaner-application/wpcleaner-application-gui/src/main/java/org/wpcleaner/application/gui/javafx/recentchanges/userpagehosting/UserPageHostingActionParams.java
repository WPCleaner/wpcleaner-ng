package org.wpcleaner.application.gui.javafx.recentchanges.userpagehosting;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import org.wpcleaner.application.gui.javafx.core.window.JavaFxWindow;

record UserPageHostingActionParams(
    JavaFxWindow<?> owner,
    String userPageComment,
    String userTalkPageComment,
    List<String> selectedTalkPageTexts) {}
