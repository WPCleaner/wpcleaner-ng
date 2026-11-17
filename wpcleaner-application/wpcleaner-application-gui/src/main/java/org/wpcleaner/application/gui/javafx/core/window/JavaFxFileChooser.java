package org.wpcleaner.application.gui.javafx.core.window;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.io.File;
import java.nio.file.Path;
import java.util.Optional;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import org.springframework.stereotype.Service;
import org.wpcleaner.api.utils.GT;

@Service
public class JavaFxFileChooser {

  public Optional<Path> chooseTextFile(final Window owner) {
    final FileChooser fileChooser = new FileChooser();
    fileChooser
        .getExtensionFilters()
        .add(new FileChooser.ExtensionFilter(GT._T("Text files"), "*.txt"));
    final File file = fileChooser.showOpenDialog(owner);
    return Optional.ofNullable(file).map(File::toPath);
  }
}
