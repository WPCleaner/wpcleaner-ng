package org.wpcleaner.api.utils;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.lang.reflect.UndeclaredThrowableException;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import org.jspecify.annotations.Nullable;

@SuppressWarnings("PMD.AvoidCatchingGenericException")
public final class AutoCatch {

  private AutoCatch() {
    // Utility class
  }

  public static <T> T run(final Callable<T> callable) {
    Objects.requireNonNull(callable, "callable cannot be null");

    try {
      return callable.call();
    } catch (final RuntimeException e) {
      throw e;
    } catch (final Exception e) {
      throw new UndeclaredThrowableException(e, e.getMessage());
    }
  }

  @Nullable
  public static <T> T runOrNull(final Callable<T> callable) {
    Objects.requireNonNull(callable, "callable cannot be null");

    try {
      return callable.call();
    } catch (final Exception _) {
      return null;
    }
  }

  @SuppressWarnings("PMD.EmptyCatchBlock")
  public static <T> T runOrDefault(
      final Callable<T> callable, final T defaultValue, final Consumer<Exception> consumer) {
    Objects.requireNonNull(callable, "callable cannot be null");

    try {
      return callable.call();
    } catch (final Exception e) {
      try {
        consumer.accept(e);
      } catch (final Exception _) {
        // Nothing to do
      }
      return defaultValue;
    }
  }

  public static void run(final RunnableThrowingException<?> runnable) {
    Objects.requireNonNull(runnable, "runnable cannot be null");

    try {
      runnable.run();
    } catch (final RuntimeException e) {
      throw e;
    } catch (final Exception e) {
      throw new UndeclaredThrowableException(e, e.getMessage());
    }
  }
}
