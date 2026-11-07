import com.diffplug.gradle.spotless.SpotlessExtension
import com.diffplug.spotless.LineEnding

plugins { id("com.diffplug.spotless") }

configure<SpotlessExtension> {
  lineEndings = LineEnding.UNIX
}
