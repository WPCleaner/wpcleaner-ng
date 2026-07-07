package org.wpcleaner.api.hook.login;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import org.springframework.stereotype.Service;
import org.wpcleaner.api.api.query.meta.siteinfo.SiteInfo;

@Service
public class SiteInfoExtractor {

  private final NamespaceExtractor namespaceExtractor;

  public SiteInfoExtractor(final NamespaceExtractor namespaceExtractor) {
    this.namespaceExtractor = namespaceExtractor;
  }

  public void extract(final SiteInfo siteInfo) {
    namespaceExtractor.extract(siteInfo);
  }
}
