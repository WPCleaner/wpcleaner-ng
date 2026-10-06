package org.wpcleaner.application.base.utils;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.wpcleaner.api.api.edit.ApiEditWrapper;

class ApiEditWrapperCustomizerTest {

  private final ApplicationContextRunner contextRunner =
      new ApplicationContextRunner()
          .withUserConfiguration(ApiEditWrapperCustomizer.class)
          .withBean(ApiEditWrapper.class, () -> Mockito.mock(ApiEditWrapper.class));

  @DisplayName("ApiEditWrapperCustomizer configures prefixes and tags directly")
  @Test
  void testDirectCustomization() {
    final ApiEditWrapper wrapper = Mockito.mock(ApiEditWrapper.class);
    final String versionPrefix = "0.0.3";

    new ApiEditWrapperCustomizer(wrapper, versionPrefix);

    Mockito.verify(wrapper).setPrefixes("WPCleanerNG v" + versionPrefix, "v" + versionPrefix);
    Mockito.verify(wrapper).setTags(List.of("wpcleanerng", "wpcleaner-ng", "wpcleaner"));
  }

  @DisplayName(
      "ApiEditWrapperCustomizer loads version.prefix from property source in Spring context")
  @Test
  void testSpringContextCustomization() {
    contextRunner.run(
        context -> {
          Assertions.assertThat(context).hasSingleBean(ApiEditWrapperCustomizer.class);
          final ApiEditWrapper wrapper = context.getBean(ApiEditWrapper.class);
          Mockito.verify(wrapper)
              .setPrefixes(Mockito.startsWith("WPCleanerNG v"), Mockito.startsWith(""));
          Mockito.verify(wrapper).setTags(List.of("wpcleanerng", "wpcleaner-ng", "wpcleaner"));
        });
  }
}
