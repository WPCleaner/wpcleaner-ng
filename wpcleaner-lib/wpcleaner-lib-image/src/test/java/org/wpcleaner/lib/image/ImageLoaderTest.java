package org.wpcleaner.lib.image;
/*
 * SPDX-FileCopyrightText: © 2024 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.io.Resource;

import java.util.Optional;

@SpringBootTest(classes = ImageLoaderTest.TestConfig.class)
class ImageLoaderTest {

    @Autowired
    private ImageLoader imageLoader;

    @EnableAutoConfiguration
    @ComponentScan(basePackageClasses = ImageLoader.class)
    static class TestConfig {

    }

    @Test
    @DisplayName("Load an existing image")
    void loadExistingImage() {
        // WHEN
        final Optional<Resource> resource =
                imageLoader.getImageResource(ImageCollection.LOGO_WPCLEANER,
                        ImageSize.ICON);

        // THEN
        Assertions.assertThat(resource)
                  .isNotEmpty();
    }

    @Test
    @DisplayName("Load an unknown image")
    void loadUnknownImage() {
        // WHEN
        final Optional<Resource> resource =
                imageLoader.getImageResource(ImageCollection.HELP, ImageSize.ICON);

        // THEN
        Assertions.assertThat(resource)
                  .isEmpty();
    }
}
