package org.wpcleaner.application.gui.javafx.core.pageanalysis;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.IndexRange;
import javafx.scene.control.Labeled;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TitledPane;
import javafx.scene.image.ImageView;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.wpcleaner.api.analysis.PageAnalysisTestFactory;
import org.wpcleaner.api.analysis.pagetitle.PageTitle;
import org.wpcleaner.application.gui.GuiSpringBootTest;
import org.wpcleaner.application.gui.core.style.PageAnalysisStylePropertiesInitializer;
import org.wpcleaner.application.gui.javafx.JavaFxTest;
import org.wpcleaner.application.gui.javafx.core.style.JavaFxStylePropertiesRegistry;
import org.wpcleaner.lib.check.CheckAlgorithm;

@GuiSpringBootTest
@Import(PageAnalysisCheckPaneFactory.class)
class PageAnalysisCheckPaneTest extends JavaFxTest {

  private static final String TITLE = "Paris";
  private static final String FIRST_LINK = "[[Paris]]";
  private static final String TEXT =
      FIRST_LINK + " est une ville.\n== Histoire ==\nVoir [[Paris]] et [[Lyon]].";
  private static final int SECOND_LINK_BEGIN = TEXT.lastIndexOf(FIRST_LINK);
  private static final int OTHER_LINK_BEGIN = TEXT.indexOf("[[Lyon]]");
  private static final String AUTOMATIC_TEXT =
      "== Histoire ==\n" + FIRST_LINK + " et " + FIRST_LINK + ".";
  private static final String INTRODUCTION = "Intro ";
  private static final Paint FIXED_BACKGROUND = Color.web("#CCFFCC");

  @Autowired private PageAnalysisCheckPaneFactory paneFactory;
  @Autowired private JavaFxStylePropertiesRegistry styleRegistry;

  @BeforeEach
  void setUp() {
    paneFactory.givenCheckWikiConfiguration(Map.of("error_048_prio_frwiki", "3"));
  }

  @DisplayName("should list only the algorithms detected in the page")
  @Test
  void listDetectedAlgorithms() throws ExecutionException, InterruptedException, TimeoutException {
    // GIVEN
    final PageAnalysisCheckPane pane = createPane();

    // WHEN
    runOnJavaFx(() -> pane.updateAnalysis(PageAnalysisTestFactory.createPageAnalysis(TITLE, TEXT)));

    // THEN
    Assertions.assertThat(pane.getErrorsListView().getItems())
        .singleElement()
        .satisfies(
            algorithmErrors -> {
              Assertions.assertThat(algorithmErrors.algorithm().name()).isEqualTo("Error No. 48");
              Assertions.assertThat(algorithmErrors.errors()).hasSize(2);
            });
  }

  @DisplayName("should display the list of errors with a title")
  @Test
  void displayTitle() throws ExecutionException, InterruptedException, TimeoutException {
    // GIVEN
    final PageAnalysisCheckPane pane = createPane();

    // WHEN
    final TitledPane errorsPane = (TitledPane) pane.getItems().getFirst();

    // THEN
    Assertions.assertThat(errorsPane.getText()).isEqualTo("Check Wiki errors");
    Assertions.assertThat(((VBox) errorsPane.getContent()).getChildren())
        .contains(pane.getErrorsListView());
  }

  @DisplayName("should display the number of errors after the algorithm name")
  @Test
  void displayErrorsCount() throws ExecutionException, InterruptedException, TimeoutException {
    // GIVEN
    final PageAnalysisCheckPane pane = createPane();

    // WHEN
    runOnJavaFx(() -> pane.updateAnalysis(PageAnalysisTestFactory.createPageAnalysis(TITLE, TEXT)));

    // THEN
    Assertions.assertThat(callOnJavaFx(() -> displayedTexts(pane)))
        .containsExactly("Error No. 48 (2)");
  }

  @DisplayName("should display only the algorithm name for a single error")
  @Test
  void displaySingleError() throws ExecutionException, InterruptedException, TimeoutException {
    // GIVEN
    final PageAnalysisCheckPane pane = createPane();

    // WHEN
    runOnJavaFx(
        () ->
            pane.updateAnalysis(
                PageAnalysisTestFactory.createPageAnalysis(TITLE, FIRST_LINK + " est une ville.")));

    // THEN
    Assertions.assertThat(callOnJavaFx(() -> displayedTexts(pane))).containsExactly("Error No. 48");
  }

  @DisplayName("should display the short description of the algorithm on the wiki")
  @Test
  void displayShortDescription() throws ExecutionException, InterruptedException, TimeoutException {
    // GIVEN
    paneFactory.givenCheckWikiConfiguration(
        Map.of("error_048_prio_frwiki", "3", "error_048_head_frwiki", "Lien vers l’article"));
    final PageAnalysisCheckPane pane = createPane();

    // WHEN
    runOnJavaFx(() -> pane.updateAnalysis(PageAnalysisTestFactory.createPageAnalysis(TITLE, TEXT)));

    // THEN
    Assertions.assertThat(callOnJavaFx(() -> displayedTexts(pane)))
        .containsExactly("Lien vers l’article (2)");
  }

  @DisplayName("should not run the algorithms deactivated on the wiki")
  @Test
  void listDeactivatedAlgorithm()
      throws ExecutionException, InterruptedException, TimeoutException {
    // GIVEN
    paneFactory.givenCheckWikiConfiguration(Map.of("error_048_prio_frwiki", "0"));
    final PageAnalysisCheckPane pane = createPane();

    // WHEN
    runOnJavaFx(() -> pane.updateAnalysis(PageAnalysisTestFactory.createPageAnalysis(TITLE, TEXT)));

    // THEN
    Assertions.assertThat(pane.getErrorsListView().getItems()).isEmpty();
  }

  @DisplayName("should not run the algorithms whose whitelist contains the page")
  @Test
  void listWhitelistedPage() throws ExecutionException, InterruptedException, TimeoutException {
    // GIVEN
    paneFactory.givenCheckWikiConfiguration(
        Map.of("error_048_prio_frwiki", "3"), Set.of(new PageTitle(0, TITLE)));
    final PageAnalysisCheckPane pane = createPane();

    // WHEN
    runOnJavaFx(() -> pane.updateAnalysis(PageAnalysisTestFactory.createPageAnalysis(TITLE, TEXT)));

    // THEN
    Assertions.assertThat(pane.getErrorsListView().getItems()).isEmpty();
  }

  @DisplayName("should list no algorithm when no error is detected in the page")
  @Test
  void listNoAlgorithm() throws ExecutionException, InterruptedException, TimeoutException {
    // GIVEN
    final PageAnalysisCheckPane pane = createPane();

    // WHEN
    runOnJavaFx(
        () -> pane.updateAnalysis(PageAnalysisTestFactory.createPageAnalysis("Marseille", TEXT)));

    // THEN
    Assertions.assertThat(pane.getErrorsListView().getItems()).isEmpty();
  }

  @DisplayName(
      "should highlight the errors of the selected algorithm over the syntax and select the first one")
  @Test
  void selectAlgorithm() throws ExecutionException, InterruptedException, TimeoutException {
    // GIVEN
    final PageAnalysisCheckPane pane = createPane();
    runOnJavaFx(() -> pane.updateAnalysis(PageAnalysisTestFactory.createPageAnalysis(TITLE, TEXT)));

    // WHEN
    runOnJavaFx(() -> pane.getErrorsListView().getSelectionModel().selectFirst());

    // THEN
    final PageAnalysisArea area = pane.getArea();
    final String errorStyle =
        styleRegistry.getStyle(PageAnalysisStylePropertiesInitializer.INTERNAL_LINK)
            + styleRegistry.getStyle(PageAnalysisStylePropertiesInitializer.CHECK_ERROR);
    Assertions.assertThat(callOnJavaFx(area::getSelection))
        .isEqualTo(new IndexRange(0, FIRST_LINK.length()));
    Assertions.assertThat(callOnJavaFx(() -> area.getStyleOfChar(0))).isEqualTo(errorStyle);
    Assertions.assertThat(callOnJavaFx(() -> area.getStyleOfChar(SECOND_LINK_BEGIN)))
        .isEqualTo(errorStyle);
    Assertions.assertThat(callOnJavaFx(() -> area.getStyleOfChar(OTHER_LINK_BEGIN)))
        .isEqualTo(styleRegistry.getStyle(PageAnalysisStylePropertiesInitializer.INTERNAL_LINK));
  }

  @DisplayName("should select the first error again when double-clicking on the algorithm")
  @Test
  void doubleClickAlgorithm() throws ExecutionException, InterruptedException, TimeoutException {
    // GIVEN
    final PageAnalysisCheckPane pane = createPane();
    runOnJavaFx(
        () -> {
          pane.updateAnalysis(PageAnalysisTestFactory.createPageAnalysis(TITLE, TEXT));
          pane.getErrorsListView().getSelectionModel().selectFirst();
          pane.getArea().selectRange(OTHER_LINK_BEGIN, OTHER_LINK_BEGIN);
        });

    // WHEN
    runOnJavaFx(
        () ->
            PageAnalysisCheckPaneFactory.displayedCells(pane)
                .getFirst()
                .fireEvent(createClickEvent(2)));

    // THEN
    Assertions.assertThat(callOnJavaFx(pane.getArea()::getSelection))
        .isEqualTo(new IndexRange(0, FIRST_LINK.length()));
  }

  @DisplayName("should not select the first error again when clicking once on the algorithm")
  @Test
  void clickAlgorithm() throws ExecutionException, InterruptedException, TimeoutException {
    // GIVEN
    final PageAnalysisCheckPane pane = createPane();
    runOnJavaFx(
        () -> {
          pane.updateAnalysis(PageAnalysisTestFactory.createPageAnalysis(TITLE, TEXT));
          pane.getErrorsListView().getSelectionModel().selectFirst();
          pane.getArea().selectRange(OTHER_LINK_BEGIN, OTHER_LINK_BEGIN);
        });

    // WHEN
    runOnJavaFx(
        () ->
            PageAnalysisCheckPaneFactory.displayedCells(pane)
                .getFirst()
                .fireEvent(createClickEvent(1)));

    // THEN
    Assertions.assertThat(callOnJavaFx(pane.getArea()::getSelection))
        .isEqualTo(new IndexRange(OTHER_LINK_BEGIN, OTHER_LINK_BEGIN));
  }

  @DisplayName("should remove the highlight when no algorithm is selected")
  @Test
  void clearSelection() throws ExecutionException, InterruptedException, TimeoutException {
    // GIVEN
    final PageAnalysisCheckPane pane = createPane();
    runOnJavaFx(
        () -> {
          pane.updateAnalysis(PageAnalysisTestFactory.createPageAnalysis(TITLE, TEXT));
          pane.getErrorsListView().getSelectionModel().selectFirst();
        });

    // WHEN
    runOnJavaFx(() -> pane.getErrorsListView().getSelectionModel().clearSelection());

    // THEN
    Assertions.assertThat(callOnJavaFx(() -> pane.getArea().getStyleOfChar(0)))
        .isEqualTo(styleRegistry.getStyle(PageAnalysisStylePropertiesInitializer.INTERNAL_LINK));
  }

  @DisplayName("should analyze the text again after it is modified")
  @Test
  void modifyText() throws ExecutionException, InterruptedException, TimeoutException {
    // GIVEN
    final PageAnalysisCheckPane pane = createPane();
    runOnJavaFx(() -> pane.updateAnalysis(PageAnalysisTestFactory.createPageAnalysis(TITLE, TEXT)));

    // WHEN
    runOnJavaFx(() -> pane.getArea().replaceText(TITLE + " est une ville."));

    // THEN
    Assertions.assertThat(
            waitForJavaFx(
                () ->
                    pane.getErrorsListView().getItems().stream()
                        .findFirst()
                        .filter(algorithmErrors -> algorithmErrors.errors().isEmpty())))
        .extracting(algorithmErrors -> algorithmErrors.algorithm().name())
        .isEqualTo("Error No. 48");
  }

  @DisplayName("should keep the selected algorithm and the caret when the text is analyzed again")
  @Test
  void modifyTextWithSelectedAlgorithm()
      throws ExecutionException, InterruptedException, TimeoutException {
    // GIVEN
    final PageAnalysisCheckPane pane = createPane();
    runOnJavaFx(
        () -> {
          pane.updateAnalysis(PageAnalysisTestFactory.createPageAnalysis(TITLE, TEXT));
          pane.getErrorsListView().getSelectionModel().selectFirst();
        });

    // WHEN
    runOnJavaFx(
        () -> {
          pane.getArea().insertText(0, INTRODUCTION);
          pane.getArea().moveTo(OTHER_LINK_BEGIN);
        });

    // THEN
    final CheckAlgorithmErrors selectedErrors =
        waitForJavaFx(
            () ->
                Optional.ofNullable(pane.getErrorsListView().getSelectionModel().getSelectedItem())
                    .filter(errors -> errors.errors().getFirst().begin() == INTRODUCTION.length()));
    Assertions.assertThat(selectedErrors.errors()).hasSize(2);
    Assertions.assertThat(callOnJavaFx(pane.getArea()::getCaretPosition))
        .isEqualTo(OTHER_LINK_BEGIN);
    Assertions.assertThat(callOnJavaFx(() -> pane.getArea().getStyleOfChar(INTRODUCTION.length())))
        .isEqualTo(
            styleRegistry.getStyle(PageAnalysisStylePropertiesInitializer.INTERNAL_LINK)
                + styleRegistry.getStyle(PageAnalysisStylePropertiesInitializer.CHECK_ERROR));
  }

  @DisplayName("should suggest the replacements of the errors at the position")
  @Test
  void createContextMenu() throws ExecutionException, InterruptedException, TimeoutException {
    // GIVEN
    final PageAnalysisCheckPane pane = createPane();
    runOnJavaFx(() -> pane.updateAnalysis(PageAnalysisTestFactory.createPageAnalysis(TITLE, TEXT)));

    // WHEN
    final List<MenuItem> items =
        callOnJavaFx(() -> pane.createContextMenu(SECOND_LINK_BEGIN + 1).getItems());

    // THEN
    Assertions.assertThat(items)
        .extracting(MenuItem::getText)
        .containsExactly("Error No. 48", TITLE, "'''" + TITLE + "'''");
    Assertions.assertThat(items)
        .extracting(MenuItem::isDisable)
        .containsExactly(true, false, false);
    Assertions.assertThat(items.get(1).getStyle()).contains("bold");
    Assertions.assertThat(items.get(2).getStyle()).isNullOrEmpty();
  }

  @DisplayName("should suggest nothing outside of the errors")
  @Test
  void createContextMenuOutsideErrors()
      throws ExecutionException, InterruptedException, TimeoutException {
    // GIVEN
    final PageAnalysisCheckPane pane = createPane();
    runOnJavaFx(() -> pane.updateAnalysis(PageAnalysisTestFactory.createPageAnalysis(TITLE, TEXT)));

    // WHEN
    final List<MenuItem> items =
        callOnJavaFx(() -> pane.createContextMenu(OTHER_LINK_BEGIN).getItems());

    // THEN
    Assertions.assertThat(items).isEmpty();
  }

  @DisplayName("should hide the suggestions when the mouse is pressed elsewhere")
  @Test
  void hideContextMenuOnMousePress()
      throws ExecutionException, InterruptedException, TimeoutException {
    // GIVEN
    final PageAnalysisCheckPane pane = createPane();
    final Stage stage = callOnJavaFx(() -> showInStage(pane));
    try {
      runOnJavaFx(() -> requestContextMenu(pane, SECOND_LINK_BEGIN + 1));
      Assertions.assertThat(callOnJavaFx(PageAnalysisCheckPaneTest::showingContextMenus))
          .hasSize(1);

      // WHEN
      runOnJavaFx(() -> pressMouse(pane, OTHER_LINK_BEGIN));

      // THEN
      Assertions.assertThat(callOnJavaFx(PageAnalysisCheckPaneTest::showingContextMenus)).isEmpty();
    } finally {
      runOnJavaFx(stage::close);
    }
  }

  @DisplayName("should hide the suggestions when the escape key is pressed")
  @Test
  void hideContextMenuOnEscape() throws ExecutionException, InterruptedException, TimeoutException {
    // GIVEN
    final PageAnalysisCheckPane pane = createPane();
    final Stage stage = callOnJavaFx(() -> showInStage(pane));
    try {
      runOnJavaFx(() -> requestContextMenu(pane, SECOND_LINK_BEGIN + 1));
      Assertions.assertThat(callOnJavaFx(PageAnalysisCheckPaneTest::showingContextMenus))
          .hasSize(1);

      // WHEN
      runOnJavaFx(
          () ->
              pane.getArea()
                  .fireEvent(
                      new KeyEvent(
                          KeyEvent.KEY_PRESSED,
                          "",
                          "",
                          KeyCode.ESCAPE,
                          false,
                          false,
                          false,
                          false)));

      // THEN
      Assertions.assertThat(callOnJavaFx(PageAnalysisCheckPaneTest::showingContextMenus)).isEmpty();
    } finally {
      runOnJavaFx(stage::close);
    }
  }

  @DisplayName("should apply the chosen replacement and analyze the text again")
  @Test
  void applyReplacement() throws ExecutionException, InterruptedException, TimeoutException {
    // GIVEN
    final PageAnalysisCheckPane pane = createPane();
    runOnJavaFx(() -> pane.updateAnalysis(PageAnalysisTestFactory.createPageAnalysis(TITLE, TEXT)));
    final MenuItem item =
        callOnJavaFx(() -> pane.createContextMenu(SECOND_LINK_BEGIN).getItems().get(2));

    // WHEN
    runOnJavaFx(item::fire);

    // THEN
    Assertions.assertThat(callOnJavaFx(() -> pane.getArea().getText()))
        .isEqualTo(TEXT.replace("Voir " + FIRST_LINK, "Voir '''" + TITLE + "'''"));
    Assertions.assertThat(pane.getErrorsListView().getItems())
        .singleElement()
        .satisfies(errors -> Assertions.assertThat(errors.errors()).hasSize(1));
  }

  @DisplayName("should apply all the automatic replacements")
  @Test
  void applyAutomaticFixes() throws ExecutionException, InterruptedException, TimeoutException {
    // GIVEN
    final PageAnalysisCheckPane pane = createPane();
    runOnJavaFx(
        () ->
            pane.updateAnalysis(PageAnalysisTestFactory.createPageAnalysis(TITLE, AUTOMATIC_TEXT)));
    final Button button =
        callOnJavaFx(
            () -> {
              PageAnalysisCheckPaneFactory.show(pane);
              return lookup(pane, CheckErrorsPane.AUTOMATIC_FIXES_ID, Button.class);
            });
    Assertions.assertThat(button.isDisable()).isFalse();

    // WHEN
    runOnJavaFx(button::fire);

    // THEN
    Assertions.assertThat(callOnJavaFx(() -> pane.getArea().getText()))
        .isEqualTo("== Histoire ==\n" + TITLE + " et " + TITLE + ".");
    Assertions.assertThat(pane.getErrorsListView().getItems())
        .singleElement()
        .satisfies(algorithmErrors -> Assertions.assertThat(algorithmErrors.errors()).isEmpty());
    Assertions.assertThat(button.isDisable()).isTrue();
  }

  @DisplayName("should display the fixed algorithms on a light green background")
  @Test
  void displayFixedAlgorithm() throws ExecutionException, InterruptedException, TimeoutException {
    // GIVEN
    final PageAnalysisCheckPane pane = createPane();
    runOnJavaFx(
        () ->
            pane.updateAnalysis(PageAnalysisTestFactory.createPageAnalysis(TITLE, AUTOMATIC_TEXT)));
    final Button button =
        callOnJavaFx(
            () -> {
              PageAnalysisCheckPaneFactory.show(pane);
              return lookup(pane, CheckErrorsPane.AUTOMATIC_FIXES_ID, Button.class);
            });
    Assertions.assertThat(callOnJavaFx(() -> displayedBackgrounds(pane)))
        .doesNotContain(FIXED_BACKGROUND);

    // WHEN
    runOnJavaFx(button::fire);

    // THEN
    Assertions.assertThat(callOnJavaFx(() -> displayedTexts(pane))).containsExactly("Error No. 48");
    Assertions.assertThat(callOnJavaFx(() -> displayedBackgrounds(pane)))
        .containsExactly(FIXED_BACKGROUND);
  }

  @DisplayName("should display the selection rather than the fixed background")
  @Test
  void selectFixedAlgorithm() throws ExecutionException, InterruptedException, TimeoutException {
    // GIVEN
    final PageAnalysisCheckPane pane = createPane();
    runOnJavaFx(
        () -> {
          pane.updateAnalysis(PageAnalysisTestFactory.createPageAnalysis(TITLE, AUTOMATIC_TEXT));
          PageAnalysisCheckPaneFactory.show(pane);
          lookup(pane, CheckErrorsPane.AUTOMATIC_FIXES_ID, Button.class).fire();
        });

    // WHEN
    runOnJavaFx(() -> pane.getErrorsListView().getSelectionModel().selectFirst());

    // THEN
    Assertions.assertThat(callOnJavaFx(() -> displayedBackgrounds(pane)))
        .singleElement()
        .isNotEqualTo(FIXED_BACKGROUND);
  }

  @DisplayName("should display an icon with a tooltip for the automatic fixes")
  @Test
  void displayAutomaticFixesButton()
      throws ExecutionException, InterruptedException, TimeoutException {
    // GIVEN
    final PageAnalysisCheckPane pane = createPane();

    // WHEN
    final Button button =
        callOnJavaFx(
            () -> {
              PageAnalysisCheckPaneFactory.show(pane);
              return lookup(pane, CheckErrorsPane.AUTOMATIC_FIXES_ID, Button.class);
            });

    // THEN
    Assertions.assertThat(button.getText()).isEmpty();
    Assertions.assertThat(button.getGraphic()).isInstanceOf(ImageView.class);
    Assertions.assertThat(button.getTooltip().getText()).isEqualTo("Automatic fixes");
  }

  @DisplayName("should give the algorithms with fewer errors than initially as fixed")
  @Test
  void getFixedAlgorithms() throws ExecutionException, InterruptedException, TimeoutException {
    // GIVEN
    final PageAnalysisCheckPane pane = createPane();
    runOnJavaFx(
        () -> {
          pane.updateAnalysis(PageAnalysisTestFactory.createPageAnalysis(TITLE, TEXT));
          pane.resetInitialErrors();
        });
    Assertions.assertThat(callOnJavaFx(pane::getFixedAlgorithms)).isEmpty();

    // WHEN
    runOnJavaFx(
        () -> {
          PageAnalysisCheckPaneFactory.show(pane);
          lookup(pane, CheckErrorsPane.AUTOMATIC_FIXES_ID, Button.class).fire();
        });

    // THEN
    Assertions.assertThat(pane.getErrorsListView().getItems())
        .singleElement()
        .satisfies(errors -> Assertions.assertThat(errors.errors()).hasSize(1));
    Assertions.assertThat(callOnJavaFx(pane::getFixedAlgorithms))
        .extracting(CheckAlgorithm::number)
        .containsExactly(48);
  }

  @DisplayName("should not apply the replacements that are not automatic")
  @Test
  void applyAutomaticFixesWithoutAutomatic()
      throws ExecutionException, InterruptedException, TimeoutException {
    // GIVEN
    final PageAnalysisCheckPane pane = createPane();

    // WHEN
    runOnJavaFx(
        () ->
            pane.updateAnalysis(
                PageAnalysisTestFactory.createPageAnalysis(TITLE, FIRST_LINK + " est une ville.")));

    // THEN
    final Button button =
        callOnJavaFx(
            () -> {
              PageAnalysisCheckPaneFactory.show(pane);
              return lookup(pane, CheckErrorsPane.AUTOMATIC_FIXES_ID, Button.class);
            });
    Assertions.assertThat(button.isDisable()).isTrue();
  }

  private PageAnalysisCheckPane createPane()
      throws ExecutionException, InterruptedException, TimeoutException {
    return callOnJavaFx(paneFactory::createPane);
  }

  private static Stage showInStage(final PageAnalysisCheckPane pane) {
    final Stage stage = new Stage();
    stage.setScene(new Scene(pane, 800, 600));
    stage.show();
    pane.updateAnalysis(PageAnalysisTestFactory.createPageAnalysis(TITLE, TEXT));
    pane.applyCss();
    pane.layout();
    return stage;
  }

  private static Point2D characterCenter(final PageAnalysisArea area, final int position) {
    final Bounds bounds =
        area.screenToLocal(area.getCharacterBoundsOnScreen(position, position + 1).orElseThrow());
    return new Point2D(bounds.getCenterX(), bounds.getCenterY());
  }

  private static void requestContextMenu(final PageAnalysisCheckPane pane, final int position) {
    final PageAnalysisArea area = pane.getArea();
    final Point2D local = characterCenter(area, position);
    final Point2D scene = area.localToScene(local);
    final Point2D screen = area.localToScreen(local);
    area.fireEvent(
        new ContextMenuEvent(
            ContextMenuEvent.CONTEXT_MENU_REQUESTED,
            scene.getX(),
            scene.getY(),
            screen.getX(),
            screen.getY(),
            false,
            null));
  }

  private static void pressMouse(final PageAnalysisCheckPane pane, final int position) {
    final PageAnalysisArea area = pane.getArea();
    final Point2D local = characterCenter(area, position);
    final Point2D scene = area.localToScene(local);
    final Point2D screen = area.localToScreen(local);
    area.fireEvent(
        new MouseEvent(
            MouseEvent.MOUSE_PRESSED,
            scene.getX(),
            scene.getY(),
            screen.getX(),
            screen.getY(),
            MouseButton.PRIMARY,
            1,
            false,
            false,
            false,
            false,
            true,
            false,
            false,
            false,
            false,
            false,
            null));
  }

  private static List<ContextMenu> showingContextMenus() {
    return Window.getWindows().stream()
        .filter(ContextMenu.class::isInstance)
        .map(ContextMenu.class::cast)
        .filter(Window::isShowing)
        .toList();
  }

  private static List<Paint> displayedBackgrounds(final PageAnalysisCheckPane pane) {
    return PageAnalysisCheckPaneFactory.displayedCells(pane).stream()
        .map(cell -> cell.getBackground().getFills().getLast().getFill())
        .toList();
  }

  private static List<String> displayedTexts(final PageAnalysisCheckPane pane) {
    return PageAnalysisCheckPaneFactory.displayedCells(pane).stream()
        .map(Labeled::getText)
        .toList();
  }

  private static MouseEvent createClickEvent(final int clickCount) {
    return new MouseEvent(
        MouseEvent.MOUSE_CLICKED,
        0,
        0,
        0,
        0,
        MouseButton.PRIMARY,
        clickCount,
        false,
        false,
        false,
        false,
        false,
        false,
        false,
        false,
        false,
        false,
        null);
  }
}
