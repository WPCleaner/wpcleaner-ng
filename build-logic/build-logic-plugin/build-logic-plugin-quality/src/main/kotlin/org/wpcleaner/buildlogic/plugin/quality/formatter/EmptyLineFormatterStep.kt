package org.wpcleaner.buildlogic.plugin.quality.formatter

import com.diffplug.spotless.FormatterStep
import java.io.File
import java.io.Serial

/**
 * This step
 * - removes empty line after method declaration
 * - ensures there is exactly one empty line after class or interface declaration
 */
class EmptyLineFormatterStep : FormatterStep {

  companion object {
    @Serial
    private const val serialVersionUID: Long = 967764207558673214L
    private val classOrRecordStartRegexp = Regex("([\\n ])(class|interface) ([A-Z])([^{]*\\{)\\n+ ")
    private val recordRegexp =
      Regex(
        "\\) \\{\\n( +)(public |private |protected |static |[A-Z]\\w+ \\{|[A-Z]\\w+ \\w+\\(|[A-Z]\\w+\\()"
      )
    private val privateFinalFieldRegexp = Regex("(private final .*;)(\\n\\n)( +)(private final)")
    private val privateStaticFinalFieldRegexp =
      Regex("(private static final .*;)(\\n\\n)( +)(private static final)")
  }

  override fun getName(): String = this.javaClass.simpleName

  override fun format(rawUnix: String, file: File): String {
    return rawUnix
      .noBlankLineAtMethodDeclaration()
      .oneEmptyLineAtClassOrInterfaceDeclaration()
      .privateFinalFieldPacker()
      .privateStaticFinalFieldPacker()
  }

  override fun close() {}

  /**
   * Here the complexity is to detect method declaration without catching record declaration (which
   * are very close). The idea is to first remove all blank line in method-like declaration then
   * adding a new line in record-like declaration
   */
  private fun String.noBlankLineAtMethodDeclaration(): String {
    return this.replace(") {\n\n", ") {\n").replace(recordRegexp, ") {\n\n$1$2")
  }

  private fun String.oneEmptyLineAtClassOrInterfaceDeclaration(): String {
    return this.replace(classOrRecordStartRegexp, "$1$2 $3$4\n\n ")
  }

  /**
   * Avoid empty line in the following syntax.
   * <pre>
   *   private final String property1;
   *
   *   private final String property2;
   * </pre>
   */
  private fun String.privateFinalFieldPacker(): String {
    return this.replace(privateFinalFieldRegexp, "$1\n$3$4")
  }

  /**
   * Avoid empty line in the following syntax.
   * <pre>
   *   private static final String FIELD_1;
   *
   *   private static final String FIELD_2;
   * </pre>
   */
  private fun String.privateStaticFinalFieldPacker(): String {
    return this.replace(privateStaticFinalFieldRegexp, "$1\n$3$4")
  }
}
