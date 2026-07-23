package org.wpcleaner.application.gui.javafx.recentchanges;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.Set;
import javafx.application.Platform;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.wpcleaner.api.api.query.list.tags.TagsParameters;
import org.wpcleaner.api.api.query.meta.siteinfo.SiteInfoParameters;
import org.wpcleaner.api.hook.login.LoginHook;
import org.wpcleaner.application.gui.core.factory.RecentChangesWindowFactory;
import org.wpcleaner.application.gui.javafx.JavaFxInitializer;

@ConditionalOnProperty(name = "gui", havingValue = "javafx")
@Service
public class JavaFxRecentChangesWindowFactory implements RecentChangesWindowFactory {

  private final JavaFxRecentChangesWindowServices services;

  public JavaFxRecentChangesWindowFactory(
      final LoginHook loginHook, final JavaFxRecentChangesWindowServices services) {
    this.services = services;
    loginHook.addSiteInfoProperties(Set.of(SiteInfoParameters.Properties.NAMESPACES));
    loginHook.addTagsProperties(
        Set.of(
            TagsParameters.Properties.ACTIVE,
            TagsParameters.Properties.DEFINED,
            TagsParameters.Properties.DESCRIPTION,
            TagsParameters.Properties.DISPLAY_NAME));
  }

  @Override
  public void displayRecentChangesWindow() {
    JavaFxInitializer.initialize();
    Platform.runLater(
        () -> {
          final JavaFxRecentChangesWindow window = new JavaFxRecentChangesWindow(services);
          window.show();
        });
  }
}
