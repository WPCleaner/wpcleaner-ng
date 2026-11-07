import org.wpcleaner.buildlogic.plugin.quality.formatter.KotlinDependenciesFormatterStep

plugins { id("wpcleaner.internal.spotless") }

spotless {
  kotlinGradle {
    toggleOffOn()
    ktfmt("0.65").googleStyle()
    addStep(KotlinDependenciesFormatterStep())
  }
}

tasks.named("spotlessKotlinGradleCheck") { dependsOn(tasks.named("spotlessKotlinGradleApply")) }
