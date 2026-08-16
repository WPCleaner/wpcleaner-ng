package org.wpcleaner.api.hook.login;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.wpcleaner.api.api.Limit;
import org.wpcleaner.api.api.query.list.tags.ApiTags;
import org.wpcleaner.api.api.query.list.tags.Tag;
import org.wpcleaner.api.api.query.list.tags.TagsParameters;
import org.wpcleaner.api.api.query.meta.siteinfo.ApiSiteInfo;
import org.wpcleaner.api.api.query.meta.siteinfo.SiteInfoParameters;
import org.wpcleaner.api.progress.ProgressStep;
import org.wpcleaner.api.progress.ProgressTracker;
import org.wpcleaner.api.repository.tag.TagRepository;
import org.wpcleaner.api.utils.GT;
import org.wpcleaner.api.wiki.definition.WikiDefinition;

@Service
public class LoginHook {

  private final ApiSiteInfo apiSiteInfo;
  private final ApiTags apiTags;
  private final SiteInfoExtractor siteInfoExtractor;
  private final Set<SiteInfoParameters.Properties> siteInfoProperties;
  private final TagRepository tagRepository;
  private final Set<TagsParameters.Properties> tagsProperties;

  public LoginHook(
      final ApiSiteInfo apiSiteInfo,
      final ApiTags apiTags,
      final SiteInfoExtractor siteInfoExtractor,
      final TagRepository tagRepository) {
    this.apiSiteInfo = apiSiteInfo;
    this.apiTags = apiTags;
    this.siteInfoExtractor = siteInfoExtractor;
    this.siteInfoProperties = EnumSet.noneOf(SiteInfoParameters.Properties.class);
    this.tagRepository = tagRepository;
    this.tagsProperties = EnumSet.noneOf(TagsParameters.Properties.class);
  }

  public void executeHook(final WikiDefinition wiki, final ProgressTracker tracker) {
    if (!siteInfoProperties.isEmpty()) {
      try (ProgressStep _ = tracker.start(GT._T("Retrieving site information"))) {
        siteInfoExtractor.extract(
            apiSiteInfo.requestSiteInfo(wiki, siteInfoProperties, null, null, null));
      }
    }
    if (!tagsProperties.isEmpty()) {
      try (ProgressStep _ = tracker.start(GT._T("Retrieving tags"))) {
        final List<Tag> tags = apiTags.retrieveTags(wiki, Limit.max(), null, tagsProperties);
        tagRepository.addTags(tags);
      }
    }
  }

  public void addSiteInfoProperties(final Set<SiteInfoParameters.Properties> properties) {
    siteInfoProperties.addAll(properties);
  }

  public void addTagsProperties(final Set<TagsParameters.Properties> properties) {
    tagsProperties.addAll(properties);
  }
}
