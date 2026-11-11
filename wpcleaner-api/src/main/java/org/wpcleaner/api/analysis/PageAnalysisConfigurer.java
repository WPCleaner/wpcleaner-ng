package org.wpcleaner.api.analysis;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.Set;
import org.springframework.stereotype.Component;
import org.wpcleaner.api.api.query.meta.siteinfo.SiteInfoParameters;
import org.wpcleaner.api.hook.login.LoginHook;

@Component
class PageAnalysisConfigurer {

  PageAnalysisConfigurer(final LoginHook loginHook) {
    loginHook.addSiteInfoProperties(
        Set.of(
            SiteInfoParameters.Properties.DOUBLE_UNDERSCORES,
            SiteInfoParameters.Properties.EXTENSION_TAGS,
            SiteInfoParameters.Properties.FUNCTION_HOOKS,
            SiteInfoParameters.Properties.GENERAL,
            SiteInfoParameters.Properties.INTERWIKI_MAP,
            SiteInfoParameters.Properties.MAGIC_WORDS,
            SiteInfoParameters.Properties.NAMESPACES,
            SiteInfoParameters.Properties.NAMESPACE_ALIASES,
            SiteInfoParameters.Properties.PROTOCOLS,
            SiteInfoParameters.Properties.SPECIAL_PAGE_ALIASES,
            SiteInfoParameters.Properties.VARIABLES));
  }
}
