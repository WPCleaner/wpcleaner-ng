import org.wpcleaner.buildlogic.plugin.quality.QualityExtension

plugins { id("com.diffplug.spotless") }

val qualityExtension: QualityExtension = QualityExtension.create(project)

spotless {
    json {
        target("src/**/*.json")
        gson().indentWithSpaces(2).sortByKeys()
        val disabledSourceSet: Iterable<SourceSet> = qualityExtension.getDisabledSourceSet(project)
        val sourceSets: List<SourceDirectorySet> = disabledSourceSet.map { it.allSource }
        if (sourceSets.isNotEmpty()) {
            project.logger.info("Disable spotless-json for {}:{}", project.name, disabledSourceSet)
            targetExclude(sourceSets)
        }
    }
}

tasks.withType<ProcessResources>().configureEach { dependsOn(tasks.named("spotlessJsonApply")) }
