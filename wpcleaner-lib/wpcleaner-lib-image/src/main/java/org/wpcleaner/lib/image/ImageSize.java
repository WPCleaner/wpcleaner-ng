package org.wpcleaner.lib.image;
/*
 * SPDX-FileCopyrightText: © 2024 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;

@SuppressWarnings("ImmutableEnumChecker")
public enum ImageSize {

    BUTTON(List.of("24px/square",
            "24px/width")), ICON(List.of("64px/square")), LABEL(List.of("24px/square",
            "24px/width")), MENU(List.of("20px/square", "20px/height")), TOOLBAR(List.of(
            "20px/square",
            "20px/height"));

    private final List<String> folders;

    ImageSize(final List<String> folders) {
        this.folders = folders;
    }

    public List<String> getFolders() {
        return folders;
    }
}
