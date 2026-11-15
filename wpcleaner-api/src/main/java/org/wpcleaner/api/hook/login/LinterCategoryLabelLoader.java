package org.wpcleaner.api.hook.login;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import org.wpcleaner.api.api.query.meta.allmessages.AllMessagesQuery;
import org.wpcleaner.api.api.query.meta.allmessages.ApiAllMessages;
import org.wpcleaner.api.api.query.meta.allmessages.Message;
import org.wpcleaner.api.progress.ProgressStep;
import org.wpcleaner.api.progress.ProgressTracker;
import org.wpcleaner.api.repository.linter.LinterCategoryRepository;
import org.wpcleaner.api.utils.GT;
import org.wpcleaner.api.wiki.definition.WikiDefinition;

@Order(Ordered.HIGHEST_PRECEDENCE)
@Service
public class LinterCategoryLabelLoader implements LoginStep {

  public static final String MESSAGE_PREFIX = "linter-category-";

  private final ApiAllMessages apiAllMessages;
  private final LinterCategoryRepository repository;

  public LinterCategoryLabelLoader(
      final ApiAllMessages apiAllMessages, final LinterCategoryRepository repository) {
    this.apiAllMessages = apiAllMessages;
    this.repository = repository;
  }

  @Override
  public List<LoginWarning> execute(final WikiDefinition wiki, final ProgressTracker tracker) {
    if (!repository.hasCategories(wiki)) {
      return List.of();
    }
    try (ProgressStep _ = tracker.start(GT._T("Retrieving Linter categories"))) {
      final List<Message> messages =
          apiAllMessages.retrieveMessages(
              wiki, AllMessagesQuery.emptyBuilder().prefix(MESSAGE_PREFIX).build());
      final Map<String, String> labels =
          messages.stream()
              .filter(message -> message.content() != null)
              .collect(
                  Collectors.toMap(
                      message -> message.name().substring(MESSAGE_PREFIX.length()),
                      Message::content,
                      (first, _) -> first));
      repository.setLabels(wiki, labels);
    }
    return List.of();
  }
}
