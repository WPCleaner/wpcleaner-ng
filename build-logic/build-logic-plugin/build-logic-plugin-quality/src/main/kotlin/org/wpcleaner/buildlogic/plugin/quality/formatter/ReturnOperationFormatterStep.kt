package org.wpcleaner.buildlogic.plugin.quality.formatter

import com.diffplug.spotless.FormatterStep
import java.io.File
import java.io.Serial

/**
 * This step
 * - removes empty lines before return
 */
class ReturnOperationFormatterStep : FormatterStep {

  companion object {
    @Serial
    private const val serialVersionUID: Long = 2653773819122036784L
    private val returnRegExp = Regex("\n\n( +)return")
  }

  override fun getName() = "ReturnOperationFormatter"

  override fun format(rawUnix: String, file: File): String {
    return rawUnix.replace(returnRegExp, "\n$1return")
  }

  override fun close() {}
}
