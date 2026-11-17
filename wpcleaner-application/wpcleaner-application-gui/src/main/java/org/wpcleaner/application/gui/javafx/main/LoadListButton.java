package org.wpcleaner.application.gui.javafx.main;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceDialog;
import javafx.stage.Window;
import org.wpcleaner.api.analysis.PageAnalysisFactory;
import org.wpcleaner.api.analysis.internallink.InternalLinkElement;
import org.wpcleaner.api.progress.LongRunningTask;
import org.wpcleaner.api.progress.ProgressStep;
import org.wpcleaner.api.progress.ProgressTracker;
import org.wpcleaner.api.utils.GT;
import org.wpcleaner.api.wiki.definition.WikiDefinition;
import org.wpcleaner.application.gui.javafx.JavaFxImageLoader;
import org.wpcleaner.lib.image.ImageCollection;
import org.wpcleaner.lib.image.ImageSize;

final class LoadListButton extends Button {

  static final String LOAD_LIST_ID = "load-list";

  private final JavaFxMainWindowServices services;

  LoadListButton(final JavaFxMainWindowServices services, final JavaFxImageLoader imageLoader) {
    super(GT._T("Load list from drive"));
    this.services = services;
    setId(LOAD_LIST_ID);
    setMaxWidth(Double.MAX_VALUE);
    imageLoader
        .getImageView(ImageCollection.DRIVE_HARDDISK, ImageSize.BUTTON)
        .ifPresent(this::setGraphic);
    setOnAction(_ -> loadList());
  }

  private void loadList() {
    final Window owner = getScene().getWindow();
    services
        .fileChooser()
        .chooseTextFile(owner)
        .ifPresent(file -> chooseFormat(owner).ifPresent(format -> displayPages(file, format)));
  }

  private static Optional<PageListFileFormat> chooseFormat(final Window owner) {
    final ChoiceDialog<PageListFileFormat> dialog =
        new ChoiceDialog<>(PageListFileFormat.PAGE_NAMES, PageListFileFormat.values());
    dialog.initOwner(owner);
    dialog.setTitle(GT._T("Load list from drive"));
    dialog.setHeaderText(GT._T("The file must be encoded in UTF-8 to be read correctly."));
    dialog.setContentText(GT._T("In which format is the file?"));
    return dialog.showAndWait();
  }

  private void displayPages(final Path file, final PageListFileFormat format) {
    final String fileName = String.valueOf(file.getFileName());
    services
        .pageListWindowFactory()
        .displayPageListWindow(
            GT._T("List: %s", fileName),
            new FilePagesTask(
                services.analysisFactory(),
                services.user().getCurrentUser().wiki(),
                file,
                fileName,
                format),
            page -> services.analysisWindowFactory().displayAnalysisWindow(page));
  }

  private record FilePagesTask(
      PageAnalysisFactory analysisFactory,
      WikiDefinition wiki,
      Path file,
      String fileName,
      PageListFileFormat format)
      implements LongRunningTask<List<String>> {

    @Override
    public List<String> call(final ProgressTracker tracker) {
      try (ProgressStep _ = tracker.start(GT._T("Reading pages from %s", fileName))) {
        final String content = Files.readString(file, StandardCharsets.UTF_8);
        return switch (format) {
          case PAGE_NAMES -> extractPageNames(content);
          case INTERNAL_LINKS -> extractInternalLinks(content);
        };
      } catch (final IOException e) {
        throw new UncheckedIOException(e);
      }
    }

    private static List<String> extractPageNames(final String content) {
      return content.lines().map(String::strip).filter(line -> !line.isEmpty()).distinct().toList();
    }

    private List<String> extractInternalLinks(final String content) {
      return analysisFactory.analysis(wiki, fileName, content).getInternalLinks().stream()
          .map(InternalLinkElement::target)
          .map(String::strip)
          .filter(target -> !target.isEmpty())
          .distinct()
          .toList();
    }
  }
}
