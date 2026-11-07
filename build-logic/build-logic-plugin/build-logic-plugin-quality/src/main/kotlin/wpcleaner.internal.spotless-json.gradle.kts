import org.wpcleaner.buildlogic.plugin.quality.QualityExtension

plugins { id("wpcleaner.internal.spotless") }

val qualityExtension: QualityExtension = QualityExtension.create(project)

spotless {
    json {
        toggleOffOn()
        target("src/**/*.json")
        gson().indentWithSpaces(2).sortByKeys()
        val disabledSourceSet: Iterable<SourceSet> = qualityExtension.getDisabledSourceSet(project)
        val excludedPaths = disabledSourceSet.map { "src/${it.name}/**" }
        if (excludedPaths.isNotEmpty()) {
            project.logger.info("Disable spotless-json for {}:{}", project.name, disabledSourceSet)
            targetExclude(excludedPaths)
        }
    }
}

tasks.withType<ProcessResources>().configureEach { dependsOn(tasks.named("spotlessJsonApply")) }
