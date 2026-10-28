package org.wpcleaner.application.gui.javafx.recentchanges.options;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.wpcleaner.api.api.CurrentUserService;
import org.wpcleaner.api.api.query.list.recentchanges.RecentChangesParameters;
import org.wpcleaner.api.api.query.list.tags.Tag;
import org.wpcleaner.api.api.query.meta.siteinfo.Namespace;
import org.wpcleaner.api.wiki.definition.WikiDefinition;
import org.wpcleaner.api.wiki.definition.WikipediaDefinitions;
import org.wpcleaner.application.gui.GuiLogin;
import org.wpcleaner.application.gui.GuiSpringBootTest;
import org.wpcleaner.application.gui.javafx.JavaFxTest;
import org.wpcleaner.application.gui.javafx.JavaFxWindowsRegistry;
import org.wpcleaner.application.gui.javafx.recentchanges.JavaFxRecentChangesWindowServices;

@GuiSpringBootTest
class RecentChangesOptionsWindowTest extends JavaFxTest {

  private static final WikiDefinition WIKI = WikipediaDefinitions.EN;
  private static final GuiLogin.LoginData LOGIN_DATA =
      new GuiLogin.LoginData(
          List.of(
              new Namespace(null, "first-letter", true, 0, "", "", false),
              new Namespace("Talk", "first-letter", false, 1, "Talk", "Talk", true)),
          List.of(new Tag(true, true, null, null, null, "tag1", List.of())),
          List.of(),
          List.of());

  @Autowired private JavaFxRecentChangesWindowServices services;
  @Autowired private CurrentUserService currentUserService;
  @Autowired private GuiLogin guiLogin;
  @Autowired private JavaFxWindowsRegistry windowsRegistry;

  @BeforeEach
  void setUp() {
    guiLogin.login(WIKI, "Bot", LOGIN_DATA);
  }

  @AfterEach
  void tearDown() throws InterruptedException, ExecutionException, TimeoutException {
    runOnJavaFx(windowsRegistry::closeAllWindows);
    currentUserService.logout();
  }

  @DisplayName("RecentChangesOptionsWindow initializes controls and handles filters correctly")
  @Test
  void recentChangesOptionsWindowInitialization()
      throws InterruptedException, ExecutionException, TimeoutException {
    runOnJavaFx(
        () -> {
          final Stage ownerStage = new Stage();
          final RecentChangesOptionsWindow window =
              new RecentChangesOptionsWindow(
                  services, ownerStage, getRecentChangesOptions(), _ -> {});

          Assertions.assertThat(window.getStage().getTitle()).isEqualTo("Recent changes options");
          Assertions.assertThat(window.getName()).isEqualTo("recentChangesOptions");
          Assertions.assertThat(window.getStage().getScene()).isNotNull();
          Assertions.assertThat(window.getStage().isShowing()).isTrue();

          window.getStage().close();
          ownerStage.close();
        });
  }

  @DisplayName("RecentChangesOptionsWindow is modal relative to owner stage")
  @Test
  void windowModalRelativelyToOwner()
      throws InterruptedException, ExecutionException, TimeoutException {
    runOnJavaFx(
        () -> {
          final Stage ownerStage = new Stage();
          final RecentChangesOptionsWindow window =
              new RecentChangesOptionsWindow(
                  services, ownerStage, getRecentChangesOptions(), _ -> {});

          Assertions.assertThat(window.getStage().getModality()).isEqualTo(Modality.WINDOW_MODAL);
          Assertions.assertThat(window.getStage().getOwner()).isEqualTo(ownerStage);

          window.getStage().close();
          ownerStage.close();
        });
  }

  @DisplayName("OK passes the options with the namespaces and tag known for the wiki")
  @Test
  void okPassesOptions() throws InterruptedException, ExecutionException, TimeoutException {
    final RecentChangesOptions options = getRecentChangesOptions();
    final AtomicReference<RecentChangesOptions> validated = new AtomicReference<>();

    runOnJavaFx(
        () -> {
          final RecentChangesOptionsWindow window =
              new RecentChangesOptionsWindow(services, new Stage(), options, validated::set);
          lookup(window, RecentChangesOptionsWindow.OK_ID, Button.class).fire();
          Assertions.assertThat(window.getStage().isShowing()).isFalse();
        });

    Assertions.assertThat(validated.get()).isEqualTo(options);
  }

  @DisplayName("OK shows an error and keeps the window open when the options name is empty")
  @Test
  void okWithEmptyNameShowsError()
      throws InterruptedException, ExecutionException, TimeoutException {
    final RecentChangesOptions options = getRecentChangesOptions();
    final AtomicReference<RecentChangesOptions> validated = new AtomicReference<>();
    final RecentChangesOptionsWindow window =
        callOnJavaFx(
            () ->
                new RecentChangesOptionsWindow(
                    services,
                    new Stage(),
                    new RecentChangesOptions(
                        " ",
                        options.namespace(),
                        options.show(),
                        options.tag(),
                        options.type(),
                        options.topOnly(),
                        options.filters()),
                    validated::set));

    Platform.runLater(() -> lookup(window, RecentChangesOptionsWindow.OK_ID, Button.class).fire());

    Assertions.assertThat(closeAlert(window))
        .isEqualTo("The option name cannot be empty or blank.");
    Assertions.assertThat(callOnJavaFx(() -> window.getStage().isShowing())).isTrue();
    Assertions.assertThat(validated.get()).isNull();
  }

  private static RecentChangesOptions getRecentChangesOptions() {
    final RecentChangesFilter filter =
        new RecentChangesFilter(
            "Filter1",
            Set.of(0),
            null,
            Set.of("tag1"),
            Set.of(RecentChangesFilter.Type.EDIT),
            Set.of(),
            null);
    return new RecentChangesOptions(
        "My Options",
        Set.of(0),
        Set.of(RecentChangesParameters.Show.NOT_BOT),
        "tag1",
        Set.of(RecentChangesParameters.Type.EDIT),
        true,
        List.of(filter));
  }
}
