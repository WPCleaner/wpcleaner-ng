package org.wpcleaner.application.bot.task;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.io.IOException;
import org.springframework.stereotype.Service;
import org.wpcleaner.application.bot.script.BotTask;
import org.wpcleaner.application.bot.script.ProcessContentCurrentTask;
import org.wpcleaner.application.bot.task.contentcurrent.ProcessContentCurrentExecutor;

@Service
public class TaskExecutor {

  private final ProcessContentCurrentExecutor processContentCurrentExecutor;

  public TaskExecutor(final ProcessContentCurrentExecutor processContentCurrentExecutor) {
    this.processContentCurrentExecutor = processContentCurrentExecutor;
  }

  public void execute(final BotTask task, final TaskContext context)
      throws IOException, InterruptedException {
    switch (task) {
      case ProcessContentCurrentTask processContentCurrent ->
          processContentCurrentExecutor.execute(processContentCurrent, context);
    }
  }
}
