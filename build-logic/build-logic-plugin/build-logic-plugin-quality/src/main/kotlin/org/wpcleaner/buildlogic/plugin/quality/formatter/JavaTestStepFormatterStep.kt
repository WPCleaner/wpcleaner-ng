package org.wpcleaner.buildlogic.plugin.quality.formatter

import com.diffplug.spotless.FormatterStep
import java.io.File
import java.io.Serial

/** This step formats java test files to avoid blank lines after GIVEN/WHEN/THEN. */
class JavaTestStepFormatterStep : FormatterStep {

  companion object {
    @Serial
    private const val serialVersionUID: Long = -6448050460362820846L
    private val testStep = Regex("(\\n+)( +)// (GIVEN|WHEN|THEN)(\\n+)")
    private val emptyLineAtMethodBeginning = Regex("\\{\n\n( +)// (GIVEN|WHEN|THEN)(\\n+)")
  }

  override fun getName() = "JavaTestStepFormatterStep"

  override fun format(rawUnix: String, file: File): String {
    if (file.name.endsWith("Test.java")) {
      return rawUnix
        .replace(testStep, "\n\n$2// $3\n")
        .replace(emptyLineAtMethodBeginning, "{\n$1// $2$3")
    }
    return rawUnix
  }

  override fun close() {}
}
