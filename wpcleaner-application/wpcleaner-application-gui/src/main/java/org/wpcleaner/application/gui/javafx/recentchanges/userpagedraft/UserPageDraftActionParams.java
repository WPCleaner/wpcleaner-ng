package org.wpcleaner.application.gui.javafx.recentchanges.userpagedraft;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import org.wpcleaner.application.gui.javafx.core.action.content.append.ContentAppendAction;
import org.wpcleaner.application.gui.javafx.core.action.content.movetodraft.MoveToDraftAction;
import org.wpcleaner.application.gui.javafx.core.action.content.replace.ContentReplaceAction;
import org.wpcleaner.application.gui.javafx.core.window.JavaFxWindow;

record UserPageDraftActionParams(
    JavaFxWindow<?> owner,
    String userName,
    MoveToDraftAction moveToDraftAction,
    ContentReplaceAction userPageAction,
    ContentAppendAction userTalkPageAction) {}
