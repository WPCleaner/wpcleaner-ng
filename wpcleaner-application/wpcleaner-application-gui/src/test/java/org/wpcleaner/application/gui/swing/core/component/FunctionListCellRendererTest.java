package org.wpcleaner.application.gui.swing.core.component;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JList;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class FunctionListCellRendererTest {

  @DisplayName("Map value to string in list cell renderer")
  @Test
  void mapValueToString() {
    final FunctionListCellRenderer<Integer> renderer =
        new FunctionListCellRenderer<>(value -> "Number: " + value);
    final JList<Integer> list = new JList<>();

    final Component component = renderer.getListCellRendererComponent(list, 42, 0, false, false);

    Assertions.assertThat(component).isInstanceOf(JLabel.class);
    final JLabel label = (JLabel) component;
    Assertions.assertThat(label.getText()).isEqualTo("Number: 42");
  }

  @DisplayName("Map null value to empty string in list cell renderer")
  @Test
  void mapNullValue() {
    final FunctionListCellRenderer<Integer> renderer =
        new FunctionListCellRenderer<>(value -> "Number: " + value);
    final JList<Integer> list = new JList<>();

    final Component component = renderer.getListCellRendererComponent(list, null, 0, false, false);

    Assertions.assertThat(component).isInstanceOf(JLabel.class);
    final JLabel label = (JLabel) component;
    Assertions.assertThat(label.getText()).isEmpty();
  }
}
