package org.wpcleaner.buildlogic.plugin.quality

open class QualityConfiguration(val name: String) {

  var isEnabled = true

  val isDisabled: Boolean
    get() = !isEnabled
}
