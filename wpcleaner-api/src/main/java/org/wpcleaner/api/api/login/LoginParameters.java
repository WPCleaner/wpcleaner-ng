package org.wpcleaner.api.api.login;

/*
 * SPDX-FileCopyrightText: © 2025 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

public enum LoginParameters {
  NAME("lgname"),
  PASSWORD("lgpassword"),
  TOKEN("lgtoken"),
  ;

  public final String value;

  LoginParameters(final String value) {
    this.value = value;
  }
}
