package org.wpcleaner.application.gui.javafx.recentchanges;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.wpcleaner.api.api.query.list.recentchanges.RecentChangesParameters;
import org.wpcleaner.api.api.query.list.tags.Tag;
import org.wpcleaner.api.repository.namespace.Namespace;
import org.wpcleaner.application.gui.javafx.JavaFxImageLoader;
import org.wpcleaner.application.gui.javafx.JavaFxInitializer;

class RecentChangesDialogsTest {

  @BeforeAll
  static void setUpClass() {
    JavaFxInitializer.initialize();
  }

  @SuppressWarnings("PMD.AvoidCatchingGenericException")
  private void runOnJavaFx(final Runnable runnable)
      throws InterruptedException,
          java.util.concurrent.ExecutionException,
          java.util.concurrent.TimeoutException {
    final CompletableFuture<Void> future = new CompletableFuture<>();
    Platform.runLater(
        () -> {
          try {
            runnable.run();
            future.complete(null);
          } catch (final AssertionError | Exception e) {
            future.completeExceptionally(e);
          }
        });
    future.get(5, TimeUnit.SECONDS);
  }

  @DisplayName("RecentChangesFilterDialog initializes controls with correct values")
  @Test
  void testRecentChangesFilterDialogInitialization()
      throws InterruptedException,
          java.util.concurrent.ExecutionException,
          java.util.concurrent.TimeoutException {
    runOnJavaFx(
        () -> {
          final Namespace ns0 = new Namespace(0, "Main", "Main");
          final Namespace ns1 = new Namespace(1, "Talk", "Talk");
          final Tag tag1 = new Tag(null, null, null, null, null, "tag1", List.of());
          final RecentChangesFilter filterWithSeverity =
              new RecentChangesFilter(
                  "My Filter",
                  Set.of(0),
                  Severity.ALERT_4,
                  Set.of("tag1"),
                  Set.of(RecentChangesParameters.Type.EDIT));

          final JavaFxImageLoader mockImageLoader =
              org.mockito.Mockito.mock(JavaFxImageLoader.class);
          org.mockito.Mockito.when(
                  mockImageLoader.getImageView(
                      org.mockito.Mockito.any(), org.mockito.Mockito.any()))
              .thenReturn(java.util.Optional.empty());

          final RecentChangesFilterDialog dialogWithSeverity =
              new RecentChangesFilterDialog(
                  null, mockImageLoader, List.of(ns0, ns1), List.of(tag1), filterWithSeverity);

          Assertions.assertThat(dialogWithSeverity.getTitle()).isEqualTo("Recent changes filter");
          Assertions.assertThat(dialogWithSeverity.getDialogPane().getContent()).isNotNull();
          Assertions.assertThat(dialogWithSeverity.getSelectedSeverity())
              .isEqualTo(Severity.ALERT_4);

          final RecentChangesFilter filterWithNullSeverity =
              new RecentChangesFilter(
                  "My Filter Null",
                  Set.of(0),
                  null,
                  Set.of("tag1"),
                  Set.of(RecentChangesParameters.Type.EDIT));

          final RecentChangesFilterDialog dialogWithNullSeverity =
              new RecentChangesFilterDialog(
                  null, mockImageLoader, List.of(ns0, ns1), List.of(tag1), filterWithNullSeverity);

          Assertions.assertThat(dialogWithNullSeverity.getSelectedSeverity()).isNull();
        });
  }

  @DisplayName("RecentChangesOptionsDialog initializes controls and handles filters correctly")
  @Test
  void testRecentChangesOptionsDialogInitialization()
      throws InterruptedException,
          java.util.concurrent.ExecutionException,
          java.util.concurrent.TimeoutException {
    runOnJavaFx(
        () -> {
          final Namespace ns0 = new Namespace(0, "Main", "Main");
          final Tag tag1 = new Tag(null, null, null, null, null, "tag1", List.of());
          final RecentChangesFilter filter =
              new RecentChangesFilter(
                  "Filter1",
                  Set.of(0),
                  null,
                  Set.of("tag1"),
                  Set.of(RecentChangesParameters.Type.EDIT));
          final RecentChangesOptions options =
              new RecentChangesOptions(
                  "My Options",
                  Set.of(0),
                  Set.of(RecentChangesParameters.Show.NOT_BOT),
                  "tag1",
                  Set.of(RecentChangesParameters.Type.EDIT),
                  true,
                  List.of(filter));

          final JavaFxImageLoader mockImageLoader =
              org.mockito.Mockito.mock(JavaFxImageLoader.class);
          org.mockito.Mockito.when(
                  mockImageLoader.getImageView(
                      org.mockito.Mockito.any(), org.mockito.Mockito.any()))
              .thenReturn(java.util.Optional.empty());

          final RecentChangesOptionsDialog dialog =
              new RecentChangesOptionsDialog(
                  null, mockImageLoader, List.of(ns0), List.of(tag1), options);

          Assertions.assertThat(dialog.getTitle()).isEqualTo("Recent changes options");
          Assertions.assertThat(dialog.getDialogPane().getContent()).isNotNull();
        });
  }
}
