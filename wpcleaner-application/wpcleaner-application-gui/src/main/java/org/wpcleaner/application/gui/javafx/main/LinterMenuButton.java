package org.wpcleaner.application.gui.javafx.main;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import javafx.geometry.Pos;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import org.jspecify.annotations.Nullable;
import org.wpcleaner.api.api.Limit;
import org.wpcleaner.api.api.query.list.linterrors.ApiLintErrors;
import org.wpcleaner.api.api.query.list.linterrors.LintErrorsQuery;
import org.wpcleaner.api.api.query.list.linterrors.PageLintError;
import org.wpcleaner.api.progress.LongRunningTask;
import org.wpcleaner.api.progress.ProgressStep;
import org.wpcleaner.api.progress.ProgressTracker;
import org.wpcleaner.api.repository.linter.LinterCategory;
import org.wpcleaner.api.repository.linter.LinterPriority;
import org.wpcleaner.api.repository.namespace.Namespace;
import org.wpcleaner.api.utils.GT;
import org.wpcleaner.api.wiki.definition.WikiDefinition;
import org.wpcleaner.application.gui.javafx.JavaFxImageLoader;
import org.wpcleaner.lib.image.ImageCollection;
import org.wpcleaner.lib.image.ImageSize;

final class LinterMenuButton extends MenuButton {

  static final String LINTER_ID = "linter";

  private final JavaFxMainWindowServices services;
  private final WikiDefinition wiki;

  LinterMenuButton(final JavaFxMainWindowServices services, final JavaFxImageLoader imageLoader) {
    super(GT._T("Linter"));
    this.services = services;
    this.wiki = services.user().getCurrentUser().wiki();
    setId(LINTER_ID);
    setMaxWidth(Double.MAX_VALUE);
    setAlignment(Pos.CENTER);
    imageLoader.getImageView(ImageCollection.LINTER, ImageSize.BUTTON).ifPresent(this::setGraphic);
    final List<LinterCategory> categories = services.linterCategories().getCategories(wiki);
    Arrays.stream(LinterPriority.values())
        .map(priority -> createPriorityMenu(priority, categories))
        .filter(menu -> !menu.getItems().isEmpty())
        .forEach(getItems()::add);
    setDisable(getItems().isEmpty());
  }

  private Menu createPriorityMenu(
      final LinterPriority priority, final List<LinterCategory> categories) {
    final Menu menu = new Menu(getPriorityName(priority));
    categories.stream()
        .filter(category -> category.priority() == priority)
        .map(this::createCategoryMenu)
        .forEach(menu.getItems()::add);
    return menu;
  }

  private static String getPriorityName(final LinterPriority priority) {
    return switch (priority) {
      case HIGH -> GT._T("High priority");
      case MEDIUM -> GT._T("Medium priority");
      case LOW -> GT._T("Low priority");
    };
  }

  private Menu createCategoryMenu(final LinterCategory category) {
    final String name = Objects.requireNonNullElse(category.label(), category.name());
    final Menu menu = new Menu(name);
    menu.getItems().addAll(createItem(category, name, null), new SeparatorMenuItem());
    services.namespaceRepository().getNamespaces(wiki).stream()
        .filter(namespace -> namespace.id() >= 0)
        .map(namespace -> createItem(category, name, namespace))
        .forEach(menu.getItems()::add);
    return menu;
  }

  private MenuItem createItem(
      final LinterCategory category, final String name, @Nullable final Namespace namespace) {
    final MenuItem item = new MenuItem(getNamespaceName(namespace));
    item.setOnAction(
        _ ->
            services
                .pageListWindowFactory()
                .displayPageListWindow(
                    GT._T("Linter: %s", name),
                    new LintErrorPagesTask(
                        services.apiLintErrors(), wiki, category, name, namespace),
                    page -> services.analysisWindowFactory().displayAnalysisWindow(page)));
    return item;
  }

  private static String getNamespaceName(@Nullable final Namespace namespace) {
    if (namespace == null) {
      return GT._T("All namespaces");
    }
    return namespace.name().isEmpty() ? GT._T("Main") : namespace.name();
  }

  private record LintErrorPagesTask(
      ApiLintErrors apiLintErrors,
      WikiDefinition wiki,
      LinterCategory category,
      String name,
      @Nullable Namespace namespace)
      implements LongRunningTask<List<String>> {

    @Override
    public List<String> call(final ProgressTracker tracker) {
      try (ProgressStep _ = tracker.start(GT._T("Retrieving pages for %s", name))) {
        return apiLintErrors
            .retrieveLintErrors(
                wiki,
                LintErrorsQuery.emptyBuilder()
                    .categories(Set.of(category.name()))
                    .namespace(namespace == null ? null : Set.of(namespace))
                    .limit(Limit.max())
                    .build())
            .stream()
            .sorted(
                Comparator.comparingInt(PageLintError::namespace)
                    .thenComparing(PageLintError::title))
            .map(PageLintError::title)
            .distinct()
            .toList();
      }
    }
  }
}
