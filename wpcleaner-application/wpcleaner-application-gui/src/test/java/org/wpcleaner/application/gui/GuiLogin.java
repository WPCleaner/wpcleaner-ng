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
import org.jspecify.annotations.Nullable;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.test.web.client.MockRestServiceServer;
import org.wpcleaner.api.api.Limit;
import org.wpcleaner.api.api.MediaWikiServerConfiguration;
import org.wpcleaner.api.api.login.Login;
import org.wpcleaner.api.api.login.MockApiLogin;
import org.wpcleaner.api.api.query.list.tags.MockApiTags;
import org.wpcleaner.api.api.query.list.tags.Tag;
import org.wpcleaner.api.api.query.list.tags.TagsParameters;
import org.wpcleaner.api.api.query.list.users.MockApiUsers;
import org.wpcleaner.api.api.query.list.users.User;
import org.wpcleaner.api.api.query.meta.siteinfo.MockApiSiteInfo;
import org.wpcleaner.api.api.query.meta.siteinfo.Namespace;
import org.wpcleaner.api.api.query.meta.siteinfo.SiteInfo;
import org.wpcleaner.api.api.query.meta.siteinfo.SiteInfoParameters;
import org.wpcleaner.api.api.query.meta.tokens.MockApiTokens;
import org.wpcleaner.api.api.query.prop.revisions.MockApiRevisions;
import org.wpcleaner.api.hook.login.WPCleanerConfigurationExpectations;
import org.wpcleaner.api.progress.ProgressTracker;
import org.wpcleaner.api.repository.namespace.CommonNamespaces;
import org.wpcleaner.api.wiki.definition.WikiDefinition;
import org.wpcleaner.application.base.processor.LoginProcessor;
import org.wpcleaner.lib.check.CheckAlgorithmRegistry;
import org.wpcleaner.lib.check.configuration.CheckWikiConfigurationExpectations;

@ConditionalOnProperty(MediaWikiServerConfiguration.ENABLED_PROPERTY)
@TestComponent
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

  public static final String PASSWORD = "p&ss+word";

  private static final String LOGIN_TOKEN = "login-token+\\";
  private static final String CHECK_WIKI_TRANSLATION_PAGE = "Project:Check Wiki/Translation";
  private static final Set<SiteInfoParameters.Properties> SITE_INFO_PROPERTIES =
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
          SiteInfoParameters.Properties.VARIABLES);
  private static final Set<TagsParameters.Properties> TAGS_PROPERTIES =
      Set.of(
          TagsParameters.Properties.ACTIVE,
          TagsParameters.Properties.DEFINED,
          TagsParameters.Properties.DESCRIPTION,
          TagsParameters.Properties.DISPLAY_NAME);

  private final CheckAlgorithmRegistry algorithmRegistry;
  private final LoginProcessor loginProcessor;
  private final MockRestServiceServer mediaWikiServer;
  private final MockApiLogin mockApiLogin;
  private final MockApiRevisions mockApiRevisions;
  private final MockApiSiteInfo mockApiSiteInfo;
  private final MockApiTags mockApiTags;
  private final MockApiTokens mockApiTokens;
  private final MockApiUsers mockApiUsers;

  public GuiLogin(
      final CheckAlgorithmRegistry algorithmRegistry,
      final LoginProcessor loginProcessor,
      final MockRestServiceServer mediaWikiServer,
      final MockApiLogin mockApiLogin,
      final MockApiRevisions mockApiRevisions,
      final MockApiSiteInfo mockApiSiteInfo,
      final MockApiTags mockApiTags,
      final MockApiTokens mockApiTokens,
      final MockApiUsers mockApiUsers) {
    this.algorithmRegistry = algorithmRegistry;
    this.loginProcessor = loginProcessor;
    this.mediaWikiServer = mediaWikiServer;
    this.mockApiLogin = mockApiLogin;
    this.mockApiRevisions = mockApiRevisions;
    this.mockApiSiteInfo = mockApiSiteInfo;
    this.mockApiTags = mockApiTags;
    this.mockApiTokens = mockApiTokens;
    this.mockApiUsers = mockApiUsers;
  }

  public void login(
      final WikiDefinition wiki, final String username, final List<Namespace> namespaces) {
    login(wiki, username, LoginData.of(namespaces));
  }

  public void login(final WikiDefinition wiki, final String username, final LoginData data) {
    givenLogin(wiki, username, data);
    loginProcessor.execute(
        LoginProcessor.Input.forLogin(wiki, username, PASSWORD.toCharArray()),
        ProgressTracker.NOOP);
    mediaWikiServer.verify();
    mediaWikiServer.reset();
  }

  public void givenLogin(
      final WikiDefinition wiki, final String username, final List<Namespace> namespaces) {
    givenLogin(wiki, username, LoginData.of(namespaces));
  }

  public void givenLogin(final WikiDefinition wiki, final String username, final LoginData data) {
    mockApiTokens.givenLoginToken(wiki, LOGIN_TOKEN);
    mockApiLogin
        .givenLogin(wiki, username, PASSWORD, LOGIN_TOKEN)
        .willReturn(new Login(Login.RESULT_SUCCESS, 1L, username));
    givenLoginHookAndUser(wiki, username, data);
  }

  public void givenDemoLogin(
      final WikiDefinition wiki, final String username, final List<Namespace> namespaces) {
    givenLoginHookAndUser(wiki, username, LoginData.of(namespaces));
  }

  private void givenLoginHookAndUser(
      final WikiDefinition wiki, final String username, final LoginData data) {
    final String compactUsername = compactUsername(username);
    mockApiSiteInfo
        .givenRequestSiteInfo(wiki, SITE_INFO_PROPERTIES, null, null, null)
        .willReturn(createSiteInfo(data.namespaces()));
    mockApiTags.givenRetrieveTags(wiki, Limit.max(), null, TAGS_PROPERTIES).willReturn(data.tags());
    final String checkWikiConfiguration = data.checkWikiConfiguration();
    WPCleanerConfigurationExpectations.givenConfiguration(
        mockApiRevisions,
        wiki,
        compactUsername,
        checkWikiConfiguration == null
            ? null
            : "check_wiki_translation_page=%s END".formatted(CHECK_WIKI_TRANSLATION_PAGE),
        data.userConfiguration());
    if (checkWikiConfiguration != null) {
      WPCleanerConfigurationExpectations.givenPage(
          mockApiRevisions, wiki, CHECK_WIKI_TRANSLATION_PAGE, checkWikiConfiguration);
      CheckWikiConfigurationExpectations.givenParametersPages(
          mockApiRevisions,
          algorithmRegistry,
          wiki,
          checkWikiConfiguration,
          data.parametersPages());
    }
    mockApiUsers
        .givenRetrieveUser(wiki, compactUsername)
        .willReturn(createUser(compactUsername, data.groups(), data.rights()));
  }

  private static String compactUsername(final String username) {
    final int atSignIndex = username.indexOf('@');
    return atSignIndex < 0 ? username : username.substring(0, atSignIndex);
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

  private static User createUser(
      final String username, final List<String> groups, final List<String> rights) {
    return new User(
        null, null, null, null, null, List.of(), groups, List.of(), null, null, username, null,
        rights, null, null);
  }

  public record LoginData(
      List<Namespace> namespaces,
      List<Tag> tags,
      List<String> groups,
      List<String> rights,
      @Nullable String userConfiguration,
      @Nullable String checkWikiConfiguration,
      Map<Integer, String> parametersPages) {

    public LoginData {
      parametersPages = Map.copyOf(parametersPages);
    }

    public LoginData(
        final List<Namespace> namespaces,
        final List<Tag> tags,
        final List<String> groups,
        final List<String> rights) {
      this(namespaces, tags, groups, rights, null, null, Map.of());
    }

    public static LoginData of(final List<Namespace> namespaces) {
      return new LoginData(namespaces, List.of(), List.of(), List.of());
    }

    public LoginData withUserConfiguration(final String configuration) {
      return new LoginData(
          namespaces, tags, groups, rights, configuration, checkWikiConfiguration, parametersPages);
    }

    public LoginData withCheckWikiConfiguration(final String configuration) {
      return new LoginData(
          namespaces, tags, groups, rights, userConfiguration, configuration, parametersPages);
    }

    public LoginData withParametersPages(final Map<Integer, String> pages) {
      return new LoginData(
          namespaces, tags, groups, rights, userConfiguration, checkWikiConfiguration, pages);
    }
  }
}
