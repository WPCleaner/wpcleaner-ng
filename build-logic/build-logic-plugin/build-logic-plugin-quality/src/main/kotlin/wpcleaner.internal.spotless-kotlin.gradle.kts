import org.wpcleaner.buildlogic.plugin.quality.QualityExtension

plugins { id("wpcleaner.internal.spotless") }

val qualityExtension: QualityExtension = QualityExtension.create(project)

spotless {
    kotlin {
        toggleOffOn()
        ktfmt("0.65").googleStyle()
        target("src/**/*.kt", "src/**/*.kts")
        targetExclude("**/build/**")
        val disabledSourceSet: Iterable<SourceSet> = qualityExtension.getDisabledSourceSet(project)
        val excludedPaths = disabledSourceSet.map { "src/${it.name}/**" }
        if (excludedPaths.isNotEmpty()) {
            project.logger.info("Disable spotless-kotlin for {}:{}", project.name, disabledSourceSet)
            targetExclude(excludedPaths)
        }
    }
}

tasks.withType<JavaCompile>().configureEach { dependsOn(tasks.named("spotlessKotlinApply")) }
