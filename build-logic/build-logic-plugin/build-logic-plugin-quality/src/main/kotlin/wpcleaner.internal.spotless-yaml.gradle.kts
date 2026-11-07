import org.wpcleaner.buildlogic.plugin.quality.QualityExtension

plugins { id("wpcleaner.internal.spotless") }

val qualityExtension: QualityExtension = QualityExtension.create(project)

spotless {
    yaml {
        toggleOffOn()
        target("src/**/*.yml")
        jackson().yamlFeature("MINIMIZE_QUOTES", true).yamlFeature("WRITE_DOC_START_MARKER", false)
        val disabledSourceSet: Iterable<SourceSet> = qualityExtension.getDisabledSourceSet(project)
        val excludedPaths = disabledSourceSet.map { "src/${it.name}/**" }
        if (excludedPaths.isNotEmpty()) {
            project.logger.info("Disable spotless-yaml for {}:{}", project.name, disabledSourceSet)
            targetExclude(excludedPaths)
        }
    }
}

tasks.withType<ProcessResources>().configureEach { dependsOn(tasks.named("spotlessYamlApply")) }
