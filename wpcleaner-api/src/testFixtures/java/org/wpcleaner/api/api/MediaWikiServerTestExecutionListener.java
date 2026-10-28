package org.wpcleaner.api.api;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import org.springframework.test.context.TestContext;
import org.springframework.test.context.support.AbstractTestExecutionListener;
import org.springframework.test.web.client.MockRestServiceServer;

public class MediaWikiServerTestExecutionListener extends AbstractTestExecutionListener {

  @Override
  public void afterTestMethod(final TestContext testContext) {
    if (!testContext.hasApplicationContext()) {
      return;
    }
    testContext
        .getApplicationContext()
        .getBeanProvider(MockRestServiceServer.class)
        .ifAvailable(MediaWikiServerTestExecutionListener::verifyAndReset);
  }

  private static void verifyAndReset(final MockRestServiceServer mediaWikiServer) {
    try {
      mediaWikiServer.verify();
    } finally {
      mediaWikiServer.reset();
    }
  }
}
