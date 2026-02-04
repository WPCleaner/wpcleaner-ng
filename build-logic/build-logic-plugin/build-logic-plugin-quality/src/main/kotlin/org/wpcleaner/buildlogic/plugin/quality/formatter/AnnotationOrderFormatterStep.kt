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

    private val annotationName = Regex("""@([a-zA-Z0-9]++)""")
  }

  override fun getName(): String = this.javaClass.simpleName

  override fun format(rawUnix: String, file: File): String = rawUnix.formatStartingAt(0, file)

  private fun String.formatStartingAt(position: Int, file: File): String {
    val nextAtSign = indexOf('@', position)
    if (nextAtSign < 0 || nextAtSign >= length - 1) {
      return this
    }
    val annotations = mutableListOf<String>()
    var currentBlock = nextAtSign
    var currentPosition = currentBlock
    do {
      val nextAnnotation = extractAnnotation(currentPosition)
      if (nextAnnotation != "") {
        currentBlock = currentPosition + nextAnnotation.length
        annotations.add(nextAnnotation)
        currentPosition = ignoreWhitespace(currentBlock)
      } else {
        val sortedAnnotations = annotations.sorted()
        if (annotations == sortedAnnotations) return formatStartingAt(nextAtSign + 1, file)
        var lineBegin = nextAtSign
        while (lineBegin > 0 && this[lineBegin - 1] == ' ') lineBegin--
        val newRaw =
          substring(0, nextAtSign) +
                  sortedAnnotations.joinToString("\n${" ".repeat(nextAtSign - lineBegin)}") +
                  substring(currentBlock)
        return newRaw.formatStartingAt(nextAtSign + 1, file)
      }
    } while (true)
  }

  private fun String.extractAnnotation(position: Int): String {
    if (position !in 0 until length || this[position] != '@') return ""
    val nameMatch = annotationName.matchAt(this, position) ?: return ""
    val nameEnd = nameMatch.range.last + 1
    var currentPosition = nameEnd
    currentPosition = ignoreWhitespace(currentPosition)
    if (currentPosition >= length || this[currentPosition] != '(')
      return substring(position, nameEnd)
    currentPosition++
    var firstParam = true
    do {
      currentPosition = ignoreWhitespace(currentPosition)
      if (currentPosition < length && this[currentPosition] == ')') {
        if (firstParam) return substring(position, currentPosition + 1)
        return ""
      }
      firstParam = false
      val nextParameter = extractParameter(currentPosition)
      if (nextParameter.isBlank()) return ""
      currentPosition += nextParameter.length
      currentPosition = ignoreWhitespace(currentPosition)
      if (currentPosition >= length) return ""
      if (this[currentPosition] == ')') return substring(position, currentPosition + 1)
      if (this[currentPosition] != ',') return ""
      currentPosition++
    } while (true)
  }

  private fun String.extractParameter(position: Int): String {
    var currentPosition = ignoreWhitespace(position)
    if (currentPosition < length && "\"{".indexOf(this[currentPosition]) >= 0) {
      val value = extractValue(currentPosition)
      if (value == "") return ""
      return substring(position, currentPosition + value.length)
    }
    val paramStart = currentPosition
    while (
      currentPosition < length && "=\"{},) ".indexOf(this[currentPosition]) < 0
    ) currentPosition++
    if (currentPosition == paramStart) return ""
    currentPosition = ignoreWhitespace(currentPosition)
    if (currentPosition >= length) return ""
    if (this[currentPosition] == '=') {
      currentPosition = ignoreWhitespace(currentPosition + 1)
      val value = extractValue(currentPosition)
      if (value == "") return ""
      return substring(position, currentPosition + value.length)
    }
    if (",)".indexOf(this[currentPosition]) >= 0) {
      val value = substring(position, currentPosition)
      if (value.isBlank()) return ""
      return value
    }
    return ""
  }

  private fun String.extractArray(position: Int): String {
    var currentPosition = position
    if (currentPosition > length || this[currentPosition] != '{') return ""
    currentPosition = ignoreWhitespace(currentPosition + 1)
    while (currentPosition < length && this[currentPosition] != '}') {
      val value = extractValue(currentPosition)
      if (value.isBlank()) return ""
      currentPosition += value.length
      currentPosition = ignoreWhitespace(currentPosition)
      if (currentPosition >= length) return ""
      if (this[currentPosition] == ',') currentPosition = ignoreWhitespace(currentPosition + 1)
      else if (this[currentPosition] != '}') return ""
    }
    if (currentPosition >= length || this[currentPosition] != '}') return ""
    return substring(position, currentPosition + 1)
  }

  private fun String.extractValue(position: Int): String {
    var currentPosition = position
    if (currentPosition > length) return ""
    if (this[currentPosition] == '"') return extractString(currentPosition)
    if (this[currentPosition] == '{') return extractArray(currentPosition)
    while (currentPosition < length && " ,)\n".indexOf(this[currentPosition]) < 0) currentPosition++
    return substring(position, currentPosition)
  }

  private fun String.extractString(position: Int): String {
    var currentPosition = position
    if (currentPosition > length || this[currentPosition] != '"') return ""
    if (startsWith("\"\"\"", currentPosition)) {
      currentPosition += 3
      val nextQuotes = indexOf("\"\"\"", currentPosition)
      if (nextQuotes < 0) return ""
      return substring(position, nextQuotes + 3)
    }
    currentPosition++
    while (currentPosition < length && "\"\n".indexOf(this[currentPosition]) < 0) currentPosition++
    if (currentPosition >= length || this[currentPosition] != '"') return ""
    return substring(position, currentPosition + 1)
  }

  private fun String.ignoreWhitespace(position: Int): Int {
    var currentPosition = position
    while (currentPosition < length && this[currentPosition].isWhitespace()) currentPosition++
    return currentPosition
  }

  override fun close() {}
}
