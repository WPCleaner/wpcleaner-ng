package org.wpcleaner.api.hook.login;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.Collection;
import java.util.EnumSet;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.wpcleaner.api.api.query.meta.siteinfo.ApiSiteInfo;
import org.wpcleaner.api.api.query.meta.siteinfo.SiteInfoParameters;
import org.wpcleaner.api.wiki.definition.WikiDefinition;

@Service
public class LoginHook {

  private final ApiSiteInfo apiSiteInfo;
  private final SiteInfoExtractor siteInfoExtractor;
  private final Collection<SiteInfoParameters.Properties> siteInfoProperties;

  public LoginHook(final ApiSiteInfo apiSiteInfo, final SiteInfoExtractor siteInfoExtractor) {
    this.apiSiteInfo = apiSiteInfo;
    this.siteInfoExtractor = siteInfoExtractor;
    this.siteInfoProperties = EnumSet.noneOf(SiteInfoParameters.Properties.class);
  }

  public void executeHook(final WikiDefinition wiki) {
    if (!siteInfoProperties.isEmpty()) {
      siteInfoExtractor.extract(
          apiSiteInfo.requestSiteInfo(wiki, siteInfoProperties, null, null, null));
    }
  }

  public void addSiteInfoProperties(final Set<SiteInfoParameters.Properties> properties) {
    siteInfoProperties.addAll(properties);
  }
}
