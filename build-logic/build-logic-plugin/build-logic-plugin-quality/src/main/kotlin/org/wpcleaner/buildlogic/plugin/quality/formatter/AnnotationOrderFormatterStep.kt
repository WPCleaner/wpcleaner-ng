package org.wpcleaner.buildlogic.plugin.quality.formatter

import com.diffplug.spotless.FormatterStep
import java.io.File
import java.io.Serial

/**
 * This step tries to order annotations alphabetically.
 * 1. Tools like 'openrewrite' can perform this kind of things but are slow
 * 2. Handle the full annotation structure with regexp is nearly impossible, we just focus on sample
 *    use case (ie annotation without fields)
 */
class AnnotationOrderFormatterStep : FormatterStep {

  companion object {

    @Serial
    private const val serialVersionUID: Long = -5703078450016482427L

    /**
     * This regexp should catch
     * <pre>
     *   @Ano2 @Ano1 private final String toto;
     * </pre>
     */
    private val twoAnnotationsWithoutParamOnOneLine =
      Regex("""(@[a-zA-Z0-9]+) (@[a-zA-Z0-9]+)([\n ])""")

    /**
     * This regexp should catch
     * <pre>
     *   @Ano2("sample")
     *   @Custom(2)
     *   @Ano1(toto = "one")
     *   String myFunction() {
     *     return "one"
     *   }
     * </pre>
     */
    private val annotationWithParamOnSeveralLines =
      Regex("""(\n *)((@[a-zA-Z0-9_.]+(\([^)\n]*\))?)(\n *)){2,}""")
  }

  override fun getName(): String = this.javaClass.simpleName

  override fun format(rawUnix: String, file: File): String =
    rawUnix.orderAnnotationWithParamOnSeveralLines().orderTwoAnnotationsWithoutParamOnOneLine()

  private fun String.orderAnnotationWithParamOnSeveralLines(): String {
    return this.replace(annotationWithParamOnSeveralLines) { match ->
      val fullBlock = match.value
      val lines = fullBlock.lines()
      val annotations = lines.drop(1).dropLast(1).sortedBy { it.trim() }.joinToString("\n")
      val final = lines.first() + "\n" + annotations + "\n" + lines.last()
      final
    }
  }

  private fun String.orderTwoAnnotationsWithoutParamOnOneLine(): String {
    return this.replace(twoAnnotationsWithoutParamOnOneLine) { match ->
      val annotation1 = match.groupValues[1]
      val annotation2 = match.groupValues[2]
      val endOfExpression = match.groupValues[3]
      if (annotation1 < annotation2) {
        "$annotation1 $annotation2$endOfExpression"
      } else {
        "$annotation2 $annotation1$endOfExpression"
      }
    }
  }

  override fun close() {}
}
