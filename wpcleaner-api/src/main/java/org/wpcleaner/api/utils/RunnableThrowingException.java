package org.wpcleaner.api.utils;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

@FunctionalInterface
public interface RunnableThrowingException<E extends Exception> {

  void run() throws E;
}
