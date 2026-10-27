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
import org.mockito.BDDMockito;
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

          final Namespace ns0 = Namespace.MAIN;
          final Tag tag1 = new Tag(false, false, null, null, null, "tag1", List.of());

          BDDMockito.given(namespaceRepository.getNamespaces()).willReturn(List.of(ns0));
          BDDMockito.given(tagRepository.getTags()).willReturn(List.of(tag1));

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
