package org.wpcleaner.api.api.query.meta.siteinfo;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import java.util.Map;
import org.jspecify.annotations.Nullable;

public final class SiteInfoTestFactory {

  private SiteInfoTestFactory() {}

  public static SiteInfo createSiteInfo(
      final Map<String, Namespace> namespaces, @Nullable final General general) {
    return new SiteInfo(
        List.of(),
        Map.of(),
        List.of(),
        List.of(),
        List.of(),
        List.of(),
        List.of(),
        general,
        List.of(),
        List.of(),
        Map.of(),
        List.of(),
        List.of(),
        List.of(),
        namespaces,
        List.of(),
        null,
        null,
        List.of(),
        List.of(),
        List.of(),
        null,
        null,
        List.of(),
        List.of());
  }

  public static General createGeneral(
      final Map<String, List<String>> linterCategories, final Map<String, Boolean> magicLinks) {
    return new General(
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        linterCategories,
        null,
        magicLinks,
        null,
        null,
        null,
        null,
        null,
        null,
        null);
  }
}
