package org.wpcleaner.application.gui.javafx.recentchanges.userpagehosting;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import org.wpcleaner.application.gui.javafx.core.action.content.append.ContentAppendAction;
import org.wpcleaner.application.gui.javafx.core.action.content.replace.ContentReplaceAction;
import org.wpcleaner.application.gui.javafx.core.window.JavaFxWindow;

record UserPageHostingActionParams(
    JavaFxWindow<?> owner,
    ContentReplaceAction userPageAction,
    ContentAppendAction userTalkPageAction) {}
