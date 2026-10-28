package org.wpcleaner.application.gui;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.test.web.client.MockRestServiceServer;
import org.wpcleaner.api.api.Limit;
import org.wpcleaner.api.api.login.Login;
import org.wpcleaner.api.api.login.MockApiLogin;
import org.wpcleaner.api.api.query.list.tags.MockApiTags;
import org.wpcleaner.api.api.query.list.tags.TagsParameters;
import org.wpcleaner.api.api.query.list.users.MockApiUsers;
import org.wpcleaner.api.api.query.list.users.User;
import org.wpcleaner.api.api.query.meta.siteinfo.MockApiSiteInfo;
import org.wpcleaner.api.api.query.meta.siteinfo.Namespace;
import org.wpcleaner.api.api.query.meta.siteinfo.SiteInfo;
import org.wpcleaner.api.api.query.meta.siteinfo.SiteInfoParameters;
import org.wpcleaner.api.api.query.meta.tokens.MockApiTokens;
import org.wpcleaner.api.progress.ProgressTracker;
import org.wpcleaner.api.repository.namespace.CommonNamespaces;
import org.wpcleaner.api.wiki.definition.WikiDefinition;
import org.wpcleaner.application.base.processor.LoginProcessor;

public final class GuiLogin {

  public static final List<Namespace> USER_NAMESPACES =
      List.of(
          new Namespace(
              "User", "first-letter", false, CommonNamespaces.USER.id, "User", "User", true),
          new Namespace(
              "User talk",
              "first-letter",
              false,
              CommonNamespaces.USER_TALK.id,
              "User talk",
              "User talk",
              true));

  private static final String LOGIN_TOKEN = "login-token+\\";
  private static final String PASSWORD = "password";
  private static final Set<SiteInfoParameters.Properties> SITE_INFO_PROPERTIES =
      Set.of(
          SiteInfoParameters.Properties.INTERWIKI_MAP,
          SiteInfoParameters.Properties.NAMESPACES,
          SiteInfoParameters.Properties.NAMESPACE_ALIASES,
          SiteInfoParameters.Properties.PROTOCOLS);
  private static final Set<TagsParameters.Properties> TAGS_PROPERTIES =
      Set.of(
          TagsParameters.Properties.ACTIVE,
          TagsParameters.Properties.DEFINED,
          TagsParameters.Properties.DESCRIPTION,
          TagsParameters.Properties.DISPLAY_NAME);

  private final LoginProcessor loginProcessor;
  private final MockRestServiceServer mediaWikiServer;
  private final MockApiLogin mockApiLogin;
  private final MockApiSiteInfo mockApiSiteInfo;
  private final MockApiTags mockApiTags;
  private final MockApiTokens mockApiTokens;
  private final MockApiUsers mockApiUsers;

  public GuiLogin(
      final LoginProcessor loginProcessor,
      final MockRestServiceServer mediaWikiServer,
      final MockApiLogin mockApiLogin,
      final MockApiSiteInfo mockApiSiteInfo,
      final MockApiTags mockApiTags,
      final MockApiTokens mockApiTokens,
      final MockApiUsers mockApiUsers) {
    this.loginProcessor = loginProcessor;
    this.mediaWikiServer = mediaWikiServer;
    this.mockApiLogin = mockApiLogin;
    this.mockApiSiteInfo = mockApiSiteInfo;
    this.mockApiTags = mockApiTags;
    this.mockApiTokens = mockApiTokens;
    this.mockApiUsers = mockApiUsers;
  }

  public void login(
      final WikiDefinition wiki, final String username, final List<Namespace> namespaces) {
    mockApiTokens.givenLoginToken(wiki, LOGIN_TOKEN);
    mockApiLogin
        .givenLogin(wiki, username, PASSWORD, LOGIN_TOKEN)
        .willReturn(new Login(Login.RESULT_SUCCESS, 1L, username));
    mockApiSiteInfo
        .givenRequestSiteInfo(wiki, SITE_INFO_PROPERTIES, null, null, null)
        .willReturn(createSiteInfo(namespaces));
    mockApiTags.givenRetrieveTags(wiki, Limit.max(), null, TAGS_PROPERTIES).willReturn(List.of());
    mockApiUsers.givenRetrieveUser(wiki, username).willReturn(createUser(username));
    loginProcessor.execute(
        LoginProcessor.Input.forLogin(wiki, username, PASSWORD.toCharArray()),
        new ProgressTracker(_ -> {}));
    mediaWikiServer.verify();
    mediaWikiServer.reset();
  }

  private static SiteInfo createSiteInfo(final List<Namespace> namespaces) {
    final Map<String, Namespace> namespacesById =
        namespaces.stream()
            .collect(
                Collectors.toMap(namespace -> String.valueOf(namespace.id()), Function.identity()));
    return new SiteInfo(
        List.of(),
        Map.of(),
        List.of(),
        List.of(),
        List.of(),
        List.of(),
        null,
        List.of(),
        List.of(),
        Map.of(),
        List.of(),
        List.of(),
        List.of(),
        namespacesById,
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

  private static User createUser(final String username) {
    return new User(
        null, null, null, null, null, List.of(), List.of(), List.of(), null, null, username, null,
        List.of(), null, null);
  }
}
