package org.wpcleaner.lib.image;

/*
 * SPDX-FileCopyrightText: © 2024 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.io.Resource;

@SpringBootTest(classes = ImageLoaderTest.TestConfig.class)
class ImageLoaderTest {

  @Autowired private ImageLoader imageLoader;

  @ComponentScan(basePackageClasses = ImageLoader.class)
  @EnableAutoConfiguration
  static class TestConfig {}

  @DisplayName("Load an existing image")
  @Test
  void loadExistingImage() {
    // WHEN
    final Optional<Resource> resource =
        imageLoader.getImageResource(ImageCollection.LOGO_WPCLEANER, ImageSize.ICON);

    // THEN
    Assertions.assertThat(resource).isNotEmpty();
  }

  @DisplayName("Load an unknown image")
  @Test
  void loadUnknownImage() {
    // WHEN
    final Optional<Resource> resource =
        imageLoader.getImageResource(ImageCollection.HELP, ImageSize.ICON);

    // THEN
    Assertions.assertThat(resource).isEmpty();
  }
}
