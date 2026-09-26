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
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.wpcleaner.api.api.query.list.recentchanges.RecentChangesParameters;
import org.wpcleaner.api.api.query.list.tags.Tag;
import org.wpcleaner.api.repository.namespace.Namespace;
import org.wpcleaner.api.repository.namespace.NamespaceRepository;
import org.wpcleaner.api.repository.tag.TagRepository;
import org.wpcleaner.application.gui.javafx.JavaFxTest;
import org.wpcleaner.application.gui.javafx.JavaFxWindowsRegistry;
import org.wpcleaner.application.gui.javafx.core.action.JavaFxActionServices;
import org.wpcleaner.application.gui.javafx.recentchanges.JavaFxRecentChangesWindowServices;
import org.wpcleaner.lib.image.ImageLoader;

class RecentChangesOptionsWindowTest extends JavaFxTest {

  @DisplayName("RecentChangesOptionsWindow initializes controls and handles filters correctly")
  @Test
  void testRecentChangesOptionsWindowInitialization()
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

          final Namespace ns0 = Namespace.MAIN;
          final Tag tag1 = new Tag(false, false, null, null, null, "tag1", List.of());

          Mockito.when(namespaceRepository.getNamespaces()).thenReturn(List.of(ns0));
          Mockito.when(tagRepository.getTags()).thenReturn(List.of(tag1));

          final Stage ownerStage = new Stage();
          final RecentChangesOptions options = getRecentChangesOptions();

          final RecentChangesOptionsWindow window =
              new RecentChangesOptionsWindow(services, ownerStage, options, _ -> {});

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

          final RecentChangesOptions options = getRecentChangesOptions();
          final Stage ownerStage = new Stage();
          final RecentChangesOptionsWindow window =
              new RecentChangesOptionsWindow(services, ownerStage, options, _ -> {});

          Assertions.assertThat(window.getStage().getModality()).isEqualTo(Modality.WINDOW_MODAL);
          Assertions.assertThat(window.getStage().getOwner()).isEqualTo(ownerStage);

          window.getStage().close();
          ownerStage.close();
        });
  }

  private static @NonNull RecentChangesOptions getRecentChangesOptions() {
    final RecentChangesFilter filter =
        new RecentChangesFilter(
            "Filter1",
            Set.of(0),
            null,
            Set.of("tag1"),
            Set.of(RecentChangesFilter.Type.EDIT),
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
