package org.wpcleaner.launcher.start;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

@FunctionalInterface
public interface ProcessStarter {

  StartedProcess start(List<String> command, Path workingDirectory, Path output) throws IOException;
}
