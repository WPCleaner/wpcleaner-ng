package org.wpcleaner.gui.core.url;

/*
 * SPDX-FileCopyrightText: © 2024 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.springframework.stereotype.Service;

@Service
public class ReportBugService {

  public String url() {
    final String description =
        """
                **Steps to replicate the issue** (include links if applicable):

                *
                *
                *

                **What happens?**:


                **What should have happened instead?**:


                **Other information** (browser name/version, screenshots, etc.):


                **Information provided by WPCleaner**:
                * Java version: %s
                * Java vendor: %s
                * Operating system: %s
                """
            .formatted(
                System.getProperty("java.version"),
                System.getProperty("java.vendor"),
                System.getProperty("os.name"));
    return "https://phabricator.wikimedia.org/maniphest/task/edit/form/43/?projects=%s&subscribers=%s&description=%s"
        .formatted("wpcleaner", "NicoV", URLEncoder.encode(description, StandardCharsets.UTF_8));
  }
}
