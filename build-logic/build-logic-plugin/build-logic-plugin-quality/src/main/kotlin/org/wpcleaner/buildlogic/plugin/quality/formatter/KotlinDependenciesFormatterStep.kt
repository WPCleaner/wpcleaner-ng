package org.wpcleaner.buildlogic.plugin.quality.formatter

import com.diffplug.spotless.FormatterStep
import java.io.File
import java.io.Serial

/**
 * This step
 * - finds the "dependencies {}" block in gradle.kts files
 * - removes empty lines
 * - sorts entries
 */
class KotlinDependenciesFormatterStep : FormatterStep {

  companion object {
    @Serial
    private const val serialVersionUID: Long = 3874688281103261436L
    private val dependenciesSectionRegexp = Regex("\ndependencies \\{\n([^{\\\\/]*)\n}")
  }

  override fun getName() = "KotlinDependenciesFormatterStep"

  override fun format(rawUnix: String, file: File): String {
    return dependenciesSectionRegexp.replace(rawUnix) { tryFormat(it) }
  }

  override fun close() {}

  private fun tryFormat(dependenciesSection: MatchResult): String {
    val dependenciesBlock = dependenciesSection.groupValues[1]
    val lines = dependenciesBlock.lines()
    val newBlock: String = if (canBeReformated(lines)) doFormat(lines) else dependenciesBlock
    return wrap(newBlock)
  }

  private fun canBeReformated(lines: List<String>): Boolean {
    return lines.all {
      !it.contains("{") &&
        !it.contains("/") &&
        !it.contains("\\") &&
        it.firstOrNull { c -> c != ' ' } != '"'
    }
  }

  private var prefixes =
    listOf(
      "  api(enforcedPlatform(",
      "  api(platform(",
      "  api(project(",
      "  api(",
      "  implementation(project(",
      "  implementation(",
      "  compileOnly(",
      "  runtimeOnly(",
      "  annotationProcessor(enforcedPlatform(project(",
      "  annotationProcessor(enforcedPlatform(",
      "  annotationProcessor(platform(",
      "  annotationProcessor(project",
      "  annotationProcessor(",
      "  testFixturesApi(project(",
      "  testFixturesApi(testFixtures(project(",
      "  testFixturesApi(testFixtures(",
      "  testFixturesApi",
      "  testFixturesImplementation(project(",
      "  testFixturesImplementation(testFixtures(project(",
      "  testFixturesImplementation(testFixtures(",
      "  testFixturesImplementation(",
      "  testFixtures",
      "  testImplementation(project(",
      "  testImplementation(testFixtures(project(",
      "  testImplementation(testFixtures(",
      "  testImplementation(",
      "  testRuntimeOnly(",
      "  test",
    )

  private fun doFormat(lines: List<String>): String {
    val comparator =
      compareBy<String> { string ->
          prefixes.withIndex().firstOrNull { string.startsWith(it.value) }?.index ?: prefixes.size
        }
        .thenComparing { it: String -> it }
    return lines.filter { it.isNotBlank() }.sortedWith(comparator).joinToString("\n")
  }

  private fun wrap(newBlock: String): String {
    return "\ndependencies {\n$newBlock\n}"
  }
}
