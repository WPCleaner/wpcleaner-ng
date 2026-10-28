package org.wpcleaner.application.gui.javafx.recentchanges.userpagehosting;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import javafx.stage.Stage;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.BDDMockito;
import org.wpcleaner.api.analysis.PageAnalysisFactory;
import org.wpcleaner.api.api.ConnectedUser;
import org.wpcleaner.api.api.CurrentUserService;
import org.wpcleaner.api.api.query.prop.revisions.ApiRevisions;
import org.wpcleaner.api.api.query.prop.revisions.Page;
import org.wpcleaner.api.api.query.prop.revisions.Revision;
import org.wpcleaner.api.api.query.prop.revisions.RevisionSlot;
import org.wpcleaner.api.progress.LongRunningTask;
import org.wpcleaner.api.progress.ProgressTracker;
import org.wpcleaner.api.repository.CaseType;
import org.wpcleaner.api.repository.interwiki.InterwikiRepository;
import org.wpcleaner.api.repository.namespace.CommonNamespaces;
import org.wpcleaner.api.repository.namespace.Namespace;
import org.wpcleaner.api.repository.namespace.NamespaceRepository;
import org.wpcleaner.api.repository.protocol.ProtocolRepository;
import org.wpcleaner.api.settings.SettingsPersistence;
import org.wpcleaner.api.wiki.definition.WikiDefinition;
import org.wpcleaner.application.gui.core.style.StylePropertiesRegistry;
import org.wpcleaner.application.gui.javafx.JavaFxTest;
import org.wpcleaner.application.gui.javafx.JavaFxWindowsRegistry;
import org.wpcleaner.application.gui.javafx.core.action.JavaFxActionServices;
import org.wpcleaner.application.gui.javafx.core.pageanalysis.coloration.PageSyntaxColorizer;
import org.wpcleaner.application.gui.javafx.core.style.JavaFxStylePropertiesRegistry;
import org.wpcleaner.application.gui.javafx.core.window.JavaFxWindow;
import org.wpcleaner.application.gui.javafx.recentchanges.FilteredRecentChange;
import org.wpcleaner.application.gui.javafx.recentchanges.JavaFxRecentChangesWindowServices;
import org.wpcleaner.application.gui.javafx.recentchanges.RecentChangesAction;
import org.wpcleaner.application.gui.javafx.recentchanges.options.RecentChangesFilter;
import org.wpcleaner.lib.image.ImageLoader;

@SuppressWarnings("PMD.CouplingBetweenObjects")
class UserPageHostingActionTest extends JavaFxTest {

  @DisplayName("canApply returns true only for user pages that are not subpages")
  @Test
  void testCanApply() {
    final JavaFxRecentChangesWindowServices services =
        BDDMockito.mock(JavaFxRecentChangesWindowServices.class);
    final JavaFxWindow<?> owner = BDDMockito.mock(JavaFxWindow.class);
    final RecentChangesAction action = new UserPageHostingAction(services, owner);

    final FilteredRecentChange userPage =
        createRecentChange(CommonNamespaces.USER.id, "User:NicoV");
    final FilteredRecentChange userSubPage =
        createRecentChange(CommonNamespaces.USER.id, "User:NicoV/sandbox");
    final FilteredRecentChange articlePage =
        createRecentChange(CommonNamespaces.MAIN.id, "Main Page");
    final FilteredRecentChange nullNamespacePage = createRecentChange(null, "User:NicoV");

    Assertions.assertThat(action.canApply(userPage)).isTrue();
    Assertions.assertThat(action.canApply(userSubPage)).isFalse();
    Assertions.assertThat(action.canApply(articlePage)).isFalse();
    Assertions.assertThat(action.canApply(nullNamespacePage)).isFalse();
  }

  @DisplayName("configure displays configuration window")
  @Test
  void testConfigureDisplaysConfigurationWindow()
      throws InterruptedException, ExecutionException, TimeoutException {
    runOnJavaFx(
        () -> {
          final JavaFxRecentChangesWindowServices services =
              BDDMockito.mock(JavaFxRecentChangesWindowServices.class);
          final ImageLoader imageLoader = BDDMockito.mock(ImageLoader.class);
          final JavaFxActionServices actionServices = BDDMockito.mock(JavaFxActionServices.class);
          final JavaFxWindowsRegistry windowsRegistry =
              BDDMockito.mock(JavaFxWindowsRegistry.class);
          final CurrentUserService currentUserService = BDDMockito.mock(CurrentUserService.class);
          final ConnectedUser connectedUser = BDDMockito.mock(ConnectedUser.class);
          final WikiDefinition wiki = BDDMockito.mock(WikiDefinition.class);

          BDDMockito.given(services.imageLoader()).willReturn(imageLoader);
          BDDMockito.given(services.actionServices()).willReturn(actionServices);
          BDDMockito.given(services.windowsRegistry()).willReturn(windowsRegistry);
          BDDMockito.given(services.user()).willReturn(currentUserService);
          BDDMockito.given(currentUserService.getCurrentUser()).willReturn(connectedUser);
          BDDMockito.given(connectedUser.wiki()).willReturn(wiki);
          BDDMockito.given(wiki.code()).willReturn("enwiki");

          final JavaFxWindow<?> owner = BDDMockito.mock(JavaFxWindow.class);
          final Stage ownerStage = new Stage();
          BDDMockito.given(owner.getStage()).willReturn(ownerStage);

          final UserPageHostingAction action = new UserPageHostingAction(services, owner);
          action.configure();

          final ArgumentCaptor<JavaFxWindow<?>> captor = ArgumentCaptor.captor();
          BDDMockito.then(windowsRegistry).should().register(captor.capture());
          Assertions.assertThat(captor.getValue()).isInstanceOf(UserPageHostingConfigWindow.class);
          captor.getValue().getStage().close();
        });
  }

  @DisplayName("apply retrieves user page and talk page in a single API call")
  @Test
  void testApplyRetrievesPagesInSingleApiCall()
      throws InterruptedException, ExecutionException, TimeoutException, IOException {
    final String wikiCode = "testapplywiki";
    final Path configFolder = SettingsPersistence.getFolder().resolve(wikiCode);
    final Path configFile = configFolder.resolve("rc-userpage-hosting.json");
    try {
      Files.createDirectories(configFolder);
      final String validConfig =
          """
          {
            "userPage": {
              "summary": "Summary",
              "text": "Text"
            },
            "userTalkPage": {
              "summary": "Talk summary",
              "texts": [
                {
                  "addedByDefault": true,
                  "label": "Notice",
                  "text": "Notice text"
                }
              ]
            }
          }
          """;
      Files.writeString(configFile, validConfig, StandardCharsets.UTF_8);

      runOnJavaFx(
          () -> {
            final JavaFxRecentChangesWindowServices services =
                BDDMockito.mock(JavaFxRecentChangesWindowServices.class);
            final ImageLoader imageLoader = BDDMockito.mock(ImageLoader.class);
            final JavaFxActionServices actionServices = BDDMockito.mock(JavaFxActionServices.class);
            final JavaFxWindowsRegistry windowsRegistry =
                BDDMockito.mock(JavaFxWindowsRegistry.class);
            final CurrentUserService currentUserService = BDDMockito.mock(CurrentUserService.class);
            final ConnectedUser connectedUser = BDDMockito.mock(ConnectedUser.class);
            final WikiDefinition wiki = BDDMockito.mock(WikiDefinition.class);
            final NamespaceRepository namespaceRepository =
                BDDMockito.mock(NamespaceRepository.class);
            final ApiRevisions apiRevisions = BDDMockito.mock(ApiRevisions.class);
            final PageSyntaxColorizer colorizer =
                new PageSyntaxColorizer(
                    List.of(),
                    new JavaFxStylePropertiesRegistry(new StylePropertiesRegistry(List.of())));
            final PageAnalysisFactory pageAnalysisFactory =
                new PageAnalysisFactory(
                    new InterwikiRepository(), new NamespaceRepository(), new ProtocolRepository());

            BDDMockito.given(services.imageLoader()).willReturn(imageLoader);
            BDDMockito.given(services.actionServices()).willReturn(actionServices);
            BDDMockito.given(services.windowsRegistry()).willReturn(windowsRegistry);
            BDDMockito.given(services.user()).willReturn(currentUserService);
            BDDMockito.given(currentUserService.getCurrentUser()).willReturn(connectedUser);
            BDDMockito.given(connectedUser.wiki()).willReturn(wiki);
            BDDMockito.given(wiki.code()).willReturn(wikiCode);
            BDDMockito.given(services.namespaceRepository()).willReturn(namespaceRepository);
            BDDMockito.given(services.apiRevisions()).willReturn(apiRevisions);
            BDDMockito.given(services.colorizer()).willReturn(colorizer);
            BDDMockito.given(services.pageAnalysisFactory()).willReturn(pageAnalysisFactory);

            final Namespace userNs =
                new Namespace(
                    CommonNamespaces.USER.id, "User", "User", List.of(), CaseType.FIRST_LETTER);
            final Namespace userTalkNs =
                new Namespace(
                    CommonNamespaces.USER_TALK.id,
                    "User talk",
                    "User talk",
                    List.of(),
                    CaseType.FIRST_LETTER);
            BDDMockito.given(namespaceRepository.getNamespaces())
                .willReturn(List.of(userNs, userTalkNs));

            final Revision userRevision =
                new Revision(
                    null,
                    false,
                    null,
                    null,
                    null,
                    null,
                    null,
                    Map.of("main", new RevisionSlot("User content", null, null, null)),
                    List.of(),
                    null,
                    null,
                    null);
            final Page userPage =
                new Page(false, CommonNamespaces.USER.id, 1, List.of(userRevision), "User:NicoV");

            final Revision talkRevision =
                new Revision(
                    null,
                    false,
                    null,
                    null,
                    null,
                    null,
                    null,
                    Map.of("main", new RevisionSlot("Talk content", null, null, null)),
                    List.of(),
                    null,
                    null,
                    null);
            final Page talkPage =
                new Page(
                    false,
                    CommonNamespaces.USER_TALK.id,
                    2,
                    List.of(talkRevision),
                    "User talk:NicoV");

            BDDMockito.given(
                    apiRevisions.retrieveRevisionsByTitle(
                        BDDMockito.eq(wiki),
                        BDDMockito.eq(List.of("User:NicoV", "User talk:NicoV")),
                        BDDMockito.any()))
                .willReturn(List.of(userPage, talkPage));

            final JavaFxWindow<?> owner = BDDMockito.mock(JavaFxWindow.class);
            final Stage ownerStage = new Stage();
            BDDMockito.given(owner.getStage()).willReturn(ownerStage);
            BDDMockito.willAnswer(
                    invocation -> {
                      final LongRunningTask<?> task = invocation.getArgument(0);
                      final Consumer<Object> afterTask = invocation.getArgument(1);
                      final Object result = task.call(new ProgressTracker(_ -> {}));
                      afterTask.accept(result);
                      return null;
                    })
                .given(owner)
                .executeAsync(BDDMockito.any(), BDDMockito.any(), BDDMockito.any());

            final UserPageHostingAction action = new UserPageHostingAction(services, owner);
            final FilteredRecentChange userPageRc =
                createRecentChange(CommonNamespaces.USER.id, "User:NicoV");

            action.apply(userPageRc);

            BDDMockito.then(apiRevisions)
                .should(BDDMockito.times(1))
                .retrieveRevisionsByTitle(
                    BDDMockito.eq(wiki),
                    BDDMockito.eq(List.of("User:NicoV", "User talk:NicoV")),
                    BDDMockito.any());
            BDDMockito.then(apiRevisions).shouldHaveNoMoreInteractions();

            final ArgumentCaptor<JavaFxWindow<?>> captor = ArgumentCaptor.captor();
            BDDMockito.then(windowsRegistry).should().register(captor.capture());
            Assertions.assertThat(captor.getValue())
                .isInstanceOf(UserPageHostingActionWindow.class);
            captor.getValue().getStage().close();
            ownerStage.close();
          });
    } finally {
      Files.deleteIfExists(configFile);
    }
  }

  private FilteredRecentChange createRecentChange(final Integer ns, final String title) {
    return new FilteredRecentChange(
        "comment",
        0,
        URI.create("https://diff"),
        BDDMockito.mock(RecentChangesFilter.class),
        ns,
        URI.create("https://page"),
        1,
        2,
        3,
        List.of(),
        null,
        title,
        "User");
  }
}
