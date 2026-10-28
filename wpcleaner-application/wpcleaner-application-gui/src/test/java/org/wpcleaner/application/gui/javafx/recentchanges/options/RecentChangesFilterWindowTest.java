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
class RecentChangesFilterWindowTest extends JavaFxTest {

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

  @DisplayName("RecentChangesFilterWindow initializes controls with correct values")
  @Test
  void recentChangesFilterWindowInitialization()
      throws InterruptedException, ExecutionException, TimeoutException {
    runOnJavaFx(
        () -> {
          final Stage ownerStage = new Stage();

          final RecentChangesFilter filterWithSeverity =
              new RecentChangesFilter(
                  "My Filter",
                  Set.of(0),
                  Severity.ALERT_4,
                  Set.of("tag1"),
                  Set.of(RecentChangesFilter.Type.EDIT),
                  Set.of("User1", "User2"),
                  null);

          final RecentChangesFilterWindow windowWithSeverity =
              new RecentChangesFilterWindow(services, ownerStage, filterWithSeverity, _ -> {});

          Assertions.assertThat(windowWithSeverity.getStage().getTitle())
              .isEqualTo("Recent changes filter");
          Assertions.assertThat(windowWithSeverity.getName()).isEqualTo("recentChangesFilter");
          Assertions.assertThat(windowWithSeverity.getStage().getScene()).isNotNull();
          Assertions.assertThat(windowWithSeverity.getStage().isShowing()).isTrue();
          Assertions.assertThat(windowWithSeverity.isRejectSelected()).isFalse();
          Assertions.assertThat(windowWithSeverity.getSelectedSeverity())
              .isEqualTo(Severity.ALERT_4);
          Assertions.assertThat(windowWithSeverity.getSelectedUsers())
              .containsExactlyInAnyOrder("User1", "User2");

          windowWithSeverity.getStage().close();

          final RecentChangesFilter filterWithReject =
              new RecentChangesFilter(
                  "My Filter Reject",
                  Set.of(0),
                  true,
                  null,
                  Set.of("tag1"),
                  Set.of(RecentChangesFilter.Type.EDIT),
                  Set.of(),
                  null);

          final RecentChangesFilterWindow windowWithReject =
              new RecentChangesFilterWindow(services, ownerStage, filterWithReject, _ -> {});

          Assertions.assertThat(windowWithReject.isRejectSelected()).isTrue();
          Assertions.assertThat(windowWithReject.getSelectedSeverity()).isNull();
          Assertions.assertThat(windowWithReject.getSelectedUsers()).isEmpty();

          windowWithReject.getStage().close();

          final RecentChangesFilter filterWithNullSeverity =
              new RecentChangesFilter(
                  "My Filter Null",
                  Set.of(0),
                  null,
                  Set.of("tag1"),
                  Set.of(RecentChangesFilter.Type.EDIT),
                  Set.of(),
                  null);

          final RecentChangesFilterWindow windowWithNullSeverity =
              new RecentChangesFilterWindow(services, ownerStage, filterWithNullSeverity, _ -> {});

          Assertions.assertThat(windowWithNullSeverity.isRejectSelected()).isFalse();
          Assertions.assertThat(windowWithNullSeverity.getSelectedSeverity()).isNull();
          Assertions.assertThat(windowWithNullSeverity.getSelectedUsers()).isEmpty();

          windowWithNullSeverity.getStage().close();
          ownerStage.close();
        });
  }

  @DisplayName("RecentChangesFilterWindow is modal relative to owner stage")
  @Test
  void windowModalRelativelyToOwner()
      throws InterruptedException, ExecutionException, TimeoutException {
    runOnJavaFx(
        () -> {
          final Stage ownerStage = new Stage();
          final RecentChangesFilterWindow window =
              new RecentChangesFilterWindow(services, ownerStage, null, _ -> {});

          Assertions.assertThat(window.getStage().getModality()).isEqualTo(Modality.WINDOW_MODAL);
          Assertions.assertThat(window.getStage().getOwner()).isEqualTo(ownerStage);

          window.getStage().close();
          ownerStage.close();
        });
  }

  @DisplayName("OK passes the filter with the namespaces and tags known for the wiki")
  @Test
  void okPassesFilter() throws InterruptedException, ExecutionException, TimeoutException {
    final RecentChangesFilter filter =
        new RecentChangesFilter(
            "My Filter",
            Set.of(1),
            false,
            Severity.ALERT_4,
            Set.of("tag1"),
            Set.of(RecentChangesFilter.Type.EDIT, RecentChangesFilter.Type.NEW),
            Set.of("User1", "User2"),
            RecentChangesFilter.SubPages.TOP_PAGES);
    final AtomicReference<RecentChangesFilter> validated = new AtomicReference<>();

    runOnJavaFx(
        () -> {
          final RecentChangesFilterWindow window =
              new RecentChangesFilterWindow(services, new Stage(), filter, validated::set);
          lookup(window, RecentChangesFilterWindow.OK_ID, Button.class).fire();
          Assertions.assertThat(window.getStage().isShowing()).isFalse();
        });

    Assertions.assertThat(validated.get()).isEqualTo(filter);
  }

  @DisplayName("OK shows an error and keeps the window open when the filter name is empty")
  @Test
  void okWithEmptyNameShowsError()
      throws InterruptedException, ExecutionException, TimeoutException {
    final AtomicReference<RecentChangesFilter> validated = new AtomicReference<>();
    final RecentChangesFilterWindow window =
        callOnJavaFx(
            () -> new RecentChangesFilterWindow(services, new Stage(), null, validated::set));

    Platform.runLater(() -> lookup(window, RecentChangesFilterWindow.OK_ID, Button.class).fire());

    Assertions.assertThat(closeAlert(window))
        .isEqualTo("The filter name cannot be empty or blank.");
    Assertions.assertThat(callOnJavaFx(() -> window.getStage().isShowing())).isTrue();
    Assertions.assertThat(validated.get()).isNull();
  }
}
