package org.wpcleaner.application.base.utils;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import org.wpcleaner.api.api.edit.ApiEditWrapper;

@Component
@PropertySource("classpath:gradle.properties")
public class ApiEditWrapperCustomizer {

  public ApiEditWrapperCustomizer(
      final ApiEditWrapper wrapper, @Value("${version.prefix}") final String versionPrefix) {
    wrapper.setPrefixes("WPCleanerNG v" + versionPrefix, "v" + versionPrefix);
    wrapper.setTags(List.of("wpcleanerng", "wpcleaner-ng", "wpcleaner"));
  }
}
