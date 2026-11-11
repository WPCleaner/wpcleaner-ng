package org.wpcleaner.launcher.install;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.nio.file.Path;
import org.wpcleaner.launcher.release.ReleaseManifest;

public record InstalledRelease(ReleaseManifest manifest, Path jar) {}
