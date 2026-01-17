import org.wpcleaner.buildlogic.plugin.quality.formatter.KotlinDependenciesFormatterStep

plugins { id("com.diffplug.spotless") }

spotless {
  kotlinGradle {
    ktfmt().googleStyle()
    addStep(KotlinDependenciesFormatterStep())
  }
}

tasks.named("spotlessKotlinGradleCheck") { dependsOn(tasks.named("spotlessKotlinGradleApply")) }
