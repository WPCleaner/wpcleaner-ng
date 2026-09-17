package org.wpcleaner.api.api.edit;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

public sealed interface EditQuery permits EditQueryByTitle, EditQueryByPageId {

  EditQueryCommon common();
}
