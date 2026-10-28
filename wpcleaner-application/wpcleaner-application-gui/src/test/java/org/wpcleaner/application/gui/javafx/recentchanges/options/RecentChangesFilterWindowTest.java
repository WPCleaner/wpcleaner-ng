package org.wpcleaner.application.gui.javafx.recentchanges.options;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.wpcleaner.api.api.query.list.tags.Tag;
import org.wpcleaner.api.repository.CaseType;
import org.wpcleaner.api.repository.namespace.Namespace;
import org.wpcleaner.api.repository.namespace.NamespaceRepository;
import org.wpcleaner.api.repository.tag.TagRepository;
import org.wpcleaner.application.gui.javafx.JavaFxTest;
import org.wpcleaner.application.gui.javafx.JavaFxWindowsRegistry;
import org.wpcleaner.application.gui.javafx.core.action.JavaFxActionServices;
import org.wpcleaner.application.gui.javafx.recentchanges.JavaFxRecentChangesWindowServices;
import org.wpcleaner.lib.image.ImageLoader;

class RecentChangesFilterWindowTest extends JavaFxTest {

  @DisplayName("RecentChangesFilterWindow initializes controls with correct values")
  @Test
  void testRecentChangesFilterWindowInitialization()
      throws InterruptedException, ExecutionException, TimeoutException {
    runOnJavaFx(
        () -> {
          final JavaFxRecentChangesWindowServices services =
              BDDMockito.mock(JavaFxRecentChangesWindowServices.class);
          final ImageLoader imageLoader = BDDMockito.mock(ImageLoader.class);
          final JavaFxActionServices actionServices = BDDMockito.mock(JavaFxActionServices.class);
          final JavaFxWindowsRegistry windowsRegistry =
              BDDMockito.mock(JavaFxWindowsRegistry.class);
          final NamespaceRepository namespaceRepository =
              BDDMockito.mock(NamespaceRepository.class);
          final TagRepository tagRepository = BDDMockito.mock(TagRepository.class);

          BDDMockito.given(services.imageLoader()).willReturn(imageLoader);
          BDDMockito.given(services.actionServices()).willReturn(actionServices);
          BDDMockito.given(services.windowsRegistry()).willReturn(windowsRegistry);
          BDDMockito.given(services.namespaceRepository()).willReturn(namespaceRepository);
          BDDMockito.given(services.tagRepository()).willReturn(tagRepository);

          final Namespace ns0 = new Namespace(0, "Main", "Main", List.of(), CaseType.FIRST_LETTER);
          final Namespace ns1 = new Namespace(1, "Talk", "Talk", List.of(), CaseType.FIRST_LETTER);
          final Tag tag1 = new Tag(false, false, null, null, null, "tag1", List.of());

          BDDMockito.given(namespaceRepository.getNamespaces()).willReturn(List.of(ns0, ns1));
          BDDMockito.given(tagRepository.getTags()).willReturn(List.of(tag1));

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
  void testWindowModalRelativelyToOwner()
      throws InterruptedException, ExecutionException, TimeoutException {
    runOnJavaFx(
        () -> {
          final JavaFxRecentChangesWindowServices services =
              BDDMockito.mock(JavaFxRecentChangesWindowServices.class);
          final ImageLoader imageLoader = BDDMockito.mock(ImageLoader.class);
          final JavaFxActionServices actionServices = BDDMockito.mock(JavaFxActionServices.class);
          final JavaFxWindowsRegistry windowsRegistry =
              BDDMockito.mock(JavaFxWindowsRegistry.class);
          final NamespaceRepository namespaceRepository =
              BDDMockito.mock(NamespaceRepository.class);
          final TagRepository tagRepository = BDDMockito.mock(TagRepository.class);

          BDDMockito.given(services.imageLoader()).willReturn(imageLoader);
          BDDMockito.given(services.actionServices()).willReturn(actionServices);
          BDDMockito.given(services.windowsRegistry()).willReturn(windowsRegistry);
          BDDMockito.given(services.namespaceRepository()).willReturn(namespaceRepository);
          BDDMockito.given(services.tagRepository()).willReturn(tagRepository);

          BDDMockito.given(namespaceRepository.getNamespaces()).willReturn(List.of());
          BDDMockito.given(tagRepository.getTags()).willReturn(List.of());

          final Stage ownerStage = new Stage();
          final RecentChangesFilterWindow window =
              new RecentChangesFilterWindow(services, ownerStage, null, _ -> {});

          Assertions.assertThat(window.getStage().getModality()).isEqualTo(Modality.WINDOW_MODAL);
          Assertions.assertThat(window.getStage().getOwner()).isEqualTo(ownerStage);

          window.getStage().close();
          ownerStage.close();
        });
  }
}
