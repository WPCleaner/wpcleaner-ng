import org.wpcleaner.buildlogic.plugin.quality.QualityExtension

plugins { id("com.diffplug.spotless") }

val qualityExtension: QualityExtension = QualityExtension.create(project)

spotless {
    yaml {
        target("src/**/*.yml")
        jackson().yamlFeature("MINIMIZE_QUOTES", true).yamlFeature("WRITE_DOC_START_MARKER", false)
        val disabledSourceSet: Iterable<SourceSet> = qualityExtension.getDisabledSourceSet(project)
        val sourceSets: List<SourceDirectorySet> = disabledSourceSet.map { it.allSource }
        if (sourceSets.isNotEmpty()) {
            project.logger.info("Disable spotless-yaml for {}:{}", project.name, disabledSourceSet)
            targetExclude(sourceSets)
        }
    }
}

tasks.withType<ProcessResources>().configureEach { dependsOn(tasks.named("spotlessYamlApply")) }
