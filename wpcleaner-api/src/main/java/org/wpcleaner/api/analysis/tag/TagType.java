package org.wpcleaner.api.analysis.tag;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

public interface TagType {

  String getNormalizedName();

  boolean canBeStart();

  boolean canBeEnd();

  boolean canBeSelfClosing();

  boolean isVoid();
}
