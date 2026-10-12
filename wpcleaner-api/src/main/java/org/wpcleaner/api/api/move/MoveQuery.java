package org.wpcleaner.api.api.move;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

public sealed interface MoveQuery permits MoveQueryByTitle, MoveQueryByPageId {

  MoveQueryCommon common();

  String description();

  MoveQuery withCommon(MoveQueryCommon common);
}
