package org.wpcleaner.api.api.query.meta.siteinfo;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.wpcleaner.api.TestCallingMWApi;
import org.wpcleaner.api.wiki.definition.WikimediaDefinitions;

@SpringBootTest(classes = ApiSiteInfoTest.SpringBootTestConfig.class)
@TestCallingMWApi
class ApiSiteInfoTest {

  @Autowired private ApiSiteInfo apiSiteInfo;

  @ComponentScan(basePackages = "org.wpcleaner")
  @Configuration
  static class SpringBootTestConfig {}

  @DisplayName("Ask for all site info properties")
  @Test
  void requestAllSiteInfoProperties() {
    // WHEN
    final SiteInfo siteInfo =
        apiSiteInfo.requestSiteInfo(
            WikimediaDefinitions.META,
            List.of(SiteInfoParameters.Properties.values()),
            null,
            null,
            null);

    // THEN
    Assertions.assertThat(siteInfo).as("siteInfo").isNotNull();

    // Verify all properties are parsed and mapped correctly
    Assertions.assertThat(siteInfo.dbReplicationLag()).as("dbReplicationLag").isNotNull();
    Assertions.assertThat(siteInfo.defaultOptions()).as("defaultOptions").isNotEmpty();
    Assertions.assertThat(siteInfo.extensions()).as("extensions").isNotEmpty();
    Assertions.assertThat(siteInfo.extensionTags()).as("extensionTags").isNotEmpty();
    Assertions.assertThat(siteInfo.fileExtensions()).as("fileExtensions").isNotEmpty();
    Assertions.assertThat(siteInfo.functionHooks()).as("functionHooks").isNotEmpty();

    Assertions.assertThat(siteInfo.general()).as("general").isNotNull();
    Assertions.assertThat(siteInfo.general().lang()).as("lang").isEqualTo("en");
    Assertions.assertThat(siteInfo.general().wikiId()).as("wikiid").isEqualTo("metawiki");

    Assertions.assertThat(siteInfo.interwikiMap()).as("interwikiMap").isNotEmpty();
    Assertions.assertThat(siteInfo.languages()).as("languages").isNotEmpty();
    Assertions.assertThat(siteInfo.languageVariants()).as("languageVariants").isNotEmpty();
    Assertions.assertThat(siteInfo.libraries()).as("libraries").isNotEmpty();
    Assertions.assertThat(siteInfo.magicWords()).as("magicWords").isNotEmpty();
    Assertions.assertThat(siteInfo.namespaceAliases()).as("namespaceAliases").isNotEmpty();

    Assertions.assertThat(siteInfo.namespaces()).as("namespaces").isNotEmpty();
    Assertions.assertThat(siteInfo.namespaces().get("0")).as("namespace 0").isNotNull();

    Assertions.assertThat(siteInfo.protocols()).as("protocols").isNotEmpty();
    Assertions.assertThat(siteInfo.restrictions()).as("restrictions").isNotNull();
    Assertions.assertThat(siteInfo.restrictions().types()).as("restrictions types").isNotEmpty();

    Assertions.assertThat(siteInfo.rightsInfo()).as("rightsInfo").isNotNull();
    Assertions.assertThat(siteInfo.rightsInfo().url()).as("rightsInfo url").isNotNull();

    Assertions.assertThat(siteInfo.showHooks()).as("showHooks").isNotEmpty();
    Assertions.assertThat(siteInfo.skins()).as("skins").isNotEmpty();
    Assertions.assertThat(siteInfo.specialPageAliases()).as("specialPageAliases").isNotEmpty();
    Assertions.assertThat(siteInfo.statistics()).as("statistics").isNotNull();
    Assertions.assertThat(siteInfo.uploadDialog()).as("uploadDialog").isNotNull();
    Assertions.assertThat(siteInfo.userGroups()).as("userGroups").isNotEmpty();
    Assertions.assertThat(siteInfo.variables()).as("variables").isNotEmpty();
  }

  @DisplayName("Ask for subset of site info properties")
  @Test
  void requestSiteInfo() {
    // WHEN
    final SiteInfo siteInfo =
        apiSiteInfo.requestSiteInfo(
            WikimediaDefinitions.META,
            List.of(
                SiteInfoParameters.Properties.GENERAL,
                SiteInfoParameters.Properties.NAMESPACES,
                SiteInfoParameters.Properties.NAMESPACE_ALIASES,
                SiteInfoParameters.Properties.STATISTICS),
            null,
            null,
            null);

    // THEN
    Assertions.assertThat(siteInfo).as("siteInfo").isNotNull();

    // Verify General
    Assertions.assertThat(siteInfo.general()).as("general").isNotNull();
    Assertions.assertThat(siteInfo.general().lang()).as("lang").isEqualTo("en");
    Assertions.assertThat(siteInfo.general().wikiId()).as("wikiid").isEqualTo("metawiki");

    // Verify Namespaces
    Assertions.assertThat(siteInfo.namespaces()).as("namespaces").isNotEmpty();
    Assertions.assertThat(siteInfo.namespaces().get("0")).as("namespace 0").isNotNull();
    Assertions.assertThat(siteInfo.namespaces().get("0").id()).as("namespace 0 id").isEqualTo(0);

    // Verify Namespace Aliases
    Assertions.assertThat(siteInfo.namespaceAliases()).as("namespaceAliases").isNotNull();

    // Verify Statistics
    Assertions.assertThat(siteInfo.statistics()).as("statistics").isNotNull();
    Assertions.assertThat(siteInfo.statistics().pages()).as("pages").isGreaterThan(0);
  }

  @DisplayName("Ask for no properties (defaults to general)")
  @Test
  void requestSiteInfoNoProperties() {
    // WHEN
    final SiteInfo siteInfo =
        apiSiteInfo.requestSiteInfo(WikimediaDefinitions.META, List.of(), null, null, null);

    // THEN
    Assertions.assertThat(siteInfo).as("siteInfo").isNotNull();
    Assertions.assertThat(siteInfo.general()).as("general").isNotNull();
    Assertions.assertThat(siteInfo.general().lang()).as("lang").isEqualTo("en");
    Assertions.assertThat(siteInfo.general().wikiId()).as("wikiid").isEqualTo("metawiki");
    Assertions.assertThat(siteInfo.namespaces()).as("namespaces").isEmpty();
    Assertions.assertThat(siteInfo.namespaceAliases()).as("namespaceAliases").isEmpty();
    Assertions.assertThat(siteInfo.statistics()).as("statistics").isNull();
  }
}
