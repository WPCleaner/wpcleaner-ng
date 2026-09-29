package org.wpcleaner.application.gui.javafx;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.concurrent.atomic.AtomicReference;
import javafx.scene.text.Text;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class JavaFxRtlTextLayoutBugTest extends JavaFxTest {

  private static final String ARABIC_TEXT =
      "يُعد من الكفاءات الرقمية التي واكبت بعين الخبير المعماري الفجر الحقيقي لثورة الأجهزة"
          + " الذكية؛ حيث بدأت مسيرته في خبايا علوم الحاسوب وهندسة النظم منذ عام 2009،"
          + " بالتزامن مع البدايات التأسيسية لأنظمة الأندرويد والـ iOS محلياً وعالمياً. راكم"
          + " خبرة استثنائية ونادرة في الدمج الهندسي المتقدم بين عتاد الأجهزة ('''Hardware''')"
          + " وبرمجياتها الدقيقة ('''Software''')، موجهاً هذه الخلفية التقنية العميقة لبناء منصات"
          + " فائقة القدرة تُعالج التحديات المعيشية، تبتكر حلول التوصيل والتجارة، وتدعم نظم"
          + " السلامة المدنية وإدارة الأزمات في الوقت الفعلي.";

  @DisplayName("Reproduces ArrayIndexOutOfBoundsException when wrapping complex RTL text in JavaFX")
  @Test
  void reproducesArrayIndexOutOfBoundsExceptionOnComplexRtlTextWrapping() throws Exception {
    final AtomicReference<IndexOutOfBoundsException> exceptionReference = new AtomicReference<>();
    runOnJavaFx(
        () -> {
          try {
            final Text text = new Text(ARABIC_TEXT);
            text.setWrappingWidth(400);
            text.getBoundsInLocal();
          } catch (final IndexOutOfBoundsException exception) {
            exceptionReference.set(exception);
          }
        });

    Assertions.assertThat(exceptionReference.get())
        .isInstanceOf(ArrayIndexOutOfBoundsException.class)
        .hasMessageContaining("out of bounds for length 527");
  }
}
