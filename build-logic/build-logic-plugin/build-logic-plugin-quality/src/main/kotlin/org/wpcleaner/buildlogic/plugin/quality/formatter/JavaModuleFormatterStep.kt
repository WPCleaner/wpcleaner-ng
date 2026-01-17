package org.wpcleaner.buildlogic.plugin.quality.formatter

import com.diffplug.spotless.FormatterStep
import java.io.File
import java.io.Serial

/**
 * This step formats module
 * - finds the "modules {}" block in module-info.java files
 * - removes empty lines
 * - sorts module declarations
 */
class JavaModuleFormatterStep : FormatterStep {

  companion object {
    @Serial
    private const val serialVersionUID: Long = 6311385752802144483L
    private val dependenciesSectionRegexp = Regex("module (.*) \\{\n([^}]*)\n}")
  }

  override fun getName() = "JavaModuleFormatterStep"

  override fun format(rawUnix: String, file: File): String {
    if (file.name != "module-info.java") {
      return rawUnix
    }
    return dependenciesSectionRegexp.replace(rawUnix) { tryFormat(it) }
  }

  override fun close() {}

  private fun tryFormat(dependenciesSection: MatchResult): String {
    val moduleName = dependenciesSection.groupValues[1]
    val moduleBlock = dependenciesSection.groupValues[2]
    val lines = moduleBlock.lines()
    val newBlock: String = if (canBeReformated(lines)) doFormat(lines) else moduleBlock
    return wrap(moduleName, newBlock)
  }

  private fun canBeReformated(lines: List<String>): Boolean {
    return lines.all {
      !it.contains("{") && !it.contains("/") && !it.contains("@") && !it.contains("\\")
    }
  }

  private var prefixes =
    mapOf(
      1 to "  requires transitive",
      3 to "  requires static",
      2 to "  requires",
      4 to "  exports",
    )

  private fun doFormat(lines: List<String>): String {
    val comparator =
      compareBy<String> { string ->
          prefixes.entries.firstOrNull { string.startsWith(it.value) }?.key ?: prefixes.size
        }
        .thenComparing { it: String -> it }
    return lines.filter { it.isNotBlank() }.sortedWith(comparator).joinToString("\n")
  }

  private fun wrap(moduleName: String, newBlock: String): String {
    return "module $moduleName {\n$newBlock\n}"
  }
}
