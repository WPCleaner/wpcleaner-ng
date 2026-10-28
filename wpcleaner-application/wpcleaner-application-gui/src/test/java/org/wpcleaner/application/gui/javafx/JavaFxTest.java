package org.wpcleaner.application.gui.javafx;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;
import java.util.stream.Stream;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToolBar;
import javafx.stage.Window;
import org.junit.jupiter.api.BeforeAll;
import org.wpcleaner.application.gui.javafx.core.window.JavaFxWindow;

public class JavaFxTest {

  private static final long TIMEOUT_SECONDS = 15;
  private static final long POLL_MILLISECONDS = 50;

  @BeforeAll
  static void setUpClass() {
    JavaFxInitializer.initialize();
  }

  protected void runOnJavaFx(final Runnable runnable)
      throws InterruptedException, ExecutionException, TimeoutException {
    callOnJavaFx(
        () -> {
          runnable.run();
          return Boolean.TRUE;
        });
  }

  @SuppressWarnings("PMD.AvoidCatchingThrowable")
  protected <T> T callOnJavaFx(final Supplier<T> supplier)
      throws InterruptedException, ExecutionException, TimeoutException {
    final CompletableFuture<T> future = new CompletableFuture<>();
    Platform.runLater(
        () -> {
          try {
            future.complete(supplier.get());
          } catch (final Throwable e) {
            future.completeExceptionally(e);
          }
        });
    return future.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
  }

  protected <T> T waitForJavaFx(final Supplier<Optional<T>> lookup)
      throws InterruptedException, ExecutionException, TimeoutException {
    final long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(TIMEOUT_SECONDS);
    while (System.nanoTime() < deadline) {
      final Optional<T> result = callOnJavaFx(lookup);
      if (result.isPresent()) {
        return result.get();
      }
      TimeUnit.MILLISECONDS.sleep(POLL_MILLISECONDS);
    }
    throw new TimeoutException("Nothing found on JavaFX thread");
  }

  protected static <T extends Node> T lookup(
      final JavaFxWindow<?> window, final String id, final Class<T> type) {
    return lookup(window.getStage().getScene().getRoot(), id, type);
  }

  protected static <T extends Node> T lookup(
      final Node root, final String id, final Class<T> type) {
    return findById(root, id)
        .map(type::cast)
        .orElseThrow(() -> new AssertionError("No node with id " + id));
  }

  private static Optional<Node> findById(final Node node, final String id) {
    if (id.equals(node.getId())) {
      return Optional.of(node);
    }
    return getInnerNodes(node)
        .map(innerNode -> findById(innerNode, id))
        .flatMap(Optional::stream)
        .findFirst();
  }

  private static Stream<Node> getInnerNodes(final Node node) {
    return switch (node) {
      case TitledPane titledPane -> Stream.ofNullable(titledPane.getContent());
      case ScrollPane scrollPane -> Stream.ofNullable(scrollPane.getContent());
      case ToolBar toolBar -> toolBar.getItems().stream();
      case Parent parent -> parent.getChildrenUnmodifiable().stream();
      default -> Stream.empty();
    };
  }

  protected String closeAlert() throws InterruptedException, ExecutionException, TimeoutException {
    return waitForJavaFx(
        () ->
            Window.getWindows().stream()
                .map(Window::getScene)
                .filter(Objects::nonNull)
                .map(Scene::getRoot)
                .filter(DialogPane.class::isInstance)
                .map(DialogPane.class::cast)
                .findFirst()
                .map(
                    dialogPane -> {
                      final String content = dialogPane.getContentText();
                      ((Button) dialogPane.lookupButton(ButtonType.OK)).fire();
                      return content;
                    }));
  }
}
