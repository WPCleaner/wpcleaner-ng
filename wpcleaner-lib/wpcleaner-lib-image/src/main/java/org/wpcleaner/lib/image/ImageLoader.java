package org.wpcleaner.lib.image;
/*
 * SPDX-FileCopyrightText: © 2024 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ImageLoader {

    private final ResourceLoader resourceLoader;

    public ImageLoader(final ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    public Optional<Resource> getImageResource(final ImageCollection image,
            final ImageSize preferredSize) {
        return getImageResource(image.getFilename(), preferredSize);
    }

    private Optional<Resource> getImageResource(final String name,
            final ImageSize preferredSize) {
        return preferredSize.getFolders()
                            .stream()
                            .map(folder -> "classpath:images/%s/%s".formatted(folder,
                                    name))
                            .map(resourceLoader::getResource)
                            .filter(Resource::exists)
                            .filter(Resource::isReadable)
                            .findFirst();
    }
}
