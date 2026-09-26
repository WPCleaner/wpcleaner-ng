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
import org.mockito.Mockito;
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
              Mockito.mock(JavaFxRecentChangesWindowServices.class);
          final ImageLoader imageLoader = Mockito.mock(ImageLoader.class);
          final JavaFxActionServices actionServices = Mockito.mock(JavaFxActionServices.class);
          final JavaFxWindowsRegistry windowsRegistry = Mockito.mock(JavaFxWindowsRegistry.class);
          final NamespaceRepository namespaceRepository = Mockito.mock(NamespaceRepository.class);
          final TagRepository tagRepository = Mockito.mock(TagRepository.class);

          Mockito.when(services.imageLoader()).thenReturn(imageLoader);
          Mockito.when(services.actionServices()).thenReturn(actionServices);
          Mockito.when(services.windowsRegistry()).thenReturn(windowsRegistry);
          Mockito.when(services.namespaceRepository()).thenReturn(namespaceRepository);
          Mockito.when(services.tagRepository()).thenReturn(tagRepository);

          final Namespace ns0 = new Namespace(0, "Main", "Main", List.of(), CaseType.FIRST_LETTER);
          final Namespace ns1 = new Namespace(1, "Talk", "Talk", List.of(), CaseType.FIRST_LETTER);
          final Tag tag1 = new Tag(false, false, null, null, null, "tag1", List.of());

          Mockito.when(namespaceRepository.getNamespaces()).thenReturn(List.of(ns0, ns1));
          Mockito.when(tagRepository.getTags()).thenReturn(List.of(tag1));

          final Stage ownerStage = new Stage();

          final RecentChangesFilter filterWithSeverity =
              new RecentChangesFilter(
                  "My Filter",
                  Set.of(0),
                  Severity.ALERT_4,
                  Set.of("tag1"),
                  Set.of(RecentChangesFilter.Type.EDIT),
                  null);

          final RecentChangesFilterWindow windowWithSeverity =
              new RecentChangesFilterWindow(services, ownerStage, filterWithSeverity, _ -> {});

          Assertions.assertThat(windowWithSeverity.getStage().getTitle())
              .isEqualTo("Recent changes filter");
          Assertions.assertThat(windowWithSeverity.getName()).isEqualTo("recentChangesFilter");
          Assertions.assertThat(windowWithSeverity.getStage().getScene()).isNotNull();
          Assertions.assertThat(windowWithSeverity.getStage().isShowing()).isTrue();
          Assertions.assertThat(windowWithSeverity.getSelectedSeverity())
              .isEqualTo(Severity.ALERT_4);

          windowWithSeverity.getStage().close();

          final RecentChangesFilter filterWithNullSeverity =
              new RecentChangesFilter(
                  "My Filter Null",
                  Set.of(0),
                  null,
                  Set.of("tag1"),
                  Set.of(RecentChangesFilter.Type.EDIT),
                  null);

          final RecentChangesFilterWindow windowWithNullSeverity =
              new RecentChangesFilterWindow(services, ownerStage, filterWithNullSeverity, _ -> {});

          Assertions.assertThat(windowWithNullSeverity.getSelectedSeverity()).isNull();

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
              Mockito.mock(JavaFxRecentChangesWindowServices.class);
          final ImageLoader imageLoader = Mockito.mock(ImageLoader.class);
          final JavaFxActionServices actionServices = Mockito.mock(JavaFxActionServices.class);
          final JavaFxWindowsRegistry windowsRegistry = Mockito.mock(JavaFxWindowsRegistry.class);
          final NamespaceRepository namespaceRepository = Mockito.mock(NamespaceRepository.class);
          final TagRepository tagRepository = Mockito.mock(TagRepository.class);

          Mockito.when(services.imageLoader()).thenReturn(imageLoader);
          Mockito.when(services.actionServices()).thenReturn(actionServices);
          Mockito.when(services.windowsRegistry()).thenReturn(windowsRegistry);
          Mockito.when(services.namespaceRepository()).thenReturn(namespaceRepository);
          Mockito.when(services.tagRepository()).thenReturn(tagRepository);

          Mockito.when(namespaceRepository.getNamespaces()).thenReturn(List.of());
          Mockito.when(tagRepository.getTags()).thenReturn(List.of());

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
