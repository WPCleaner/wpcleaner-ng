package org.wpcleaner.buildlogic.plugin.quality.formatter

import com.diffplug.spotless.FormatterStep
import java.io.File
import java.io.Serial

/**
 * This step
 * - adds one line after preconditions
 */
class PreconditionsFormatterStep : FormatterStep {

  companion object {
    @Serial
    private const val serialVersionUID: Long = -7794004079824051648L
    private val preconditionLine = Regex("(\n+)( +)Objects\\.requireNonNull([^;]*);")
    private val endOfPreconditionBlock =
      Regex("([{;])\n( +)Objects\\.requireNonNull([^;]*);(\n +)( +)([a-zA-NP-Z])")
  }

  override fun getName() = "PreconditionFormatter"

  override fun format(rawUnix: String, file: File): String {
    return rawUnix.removeEmptyLineInPreconditionsBlock().addOneEmptyLineAfterPreconditionsBlock()
  }

  override fun close() {}

  private fun String.addOneEmptyLineAfterPreconditionsBlock(): String {
    return this.replace(endOfPreconditionBlock, "$1\n$2Objects.requireNonNull$3;\n\n$4$5$6")
      .replace(Regex("\n\n( +)return"), "\n$1return")
  }

  private fun String.removeEmptyLineInPreconditionsBlock(): String {
    return this.replace(preconditionLine, "\n$2Objects.requireNonNull$3;")
  }
}
