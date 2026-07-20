package org.wpcleaner.application.gui.swing.recentchanges;

/*
 * SPDX-FileCopyrightText: © 2025 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.awt.EventQueue;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.wpcleaner.api.api.query.list.tags.TagsParameters;
import org.wpcleaner.api.api.query.meta.siteinfo.SiteInfoParameters;
import org.wpcleaner.api.hook.login.LoginHook;

@Service
public class SwingRecentChangesWindowFactory {

  private final SwingRecentChangesWindowServices services;

  public SwingRecentChangesWindowFactory(
      final LoginHook loginHook, final SwingRecentChangesWindowServices services) {
    this.services = services;
    loginHook.addSiteInfoProperties(Set.of(SiteInfoParameters.Properties.NAMESPACES));
    loginHook.addTagsProperties(
        Set.of(
            TagsParameters.Properties.ACTIVE,
            TagsParameters.Properties.DEFINED,
            TagsParameters.Properties.DESCRIPTION,
            TagsParameters.Properties.DISPLAY_NAME));
  }

  public void displayRecentChangesWindow() {
    EventQueue.invokeLater(() -> SwingRecentChangesWindow.create(services));
  }
}
