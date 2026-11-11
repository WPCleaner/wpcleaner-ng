package org.wpcleaner.launcher.runtime;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.nio.file.Path;

public record JavaRuntime(Path home, int version, Path executable) {}
