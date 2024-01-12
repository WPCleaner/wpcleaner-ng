import org.wpcleaner.buildlogic.plugin.quality.QualityExtension

plugins { id("com.diffplug.spotless") }

val qualityExtension: QualityExtension = QualityExtension.create(project)

spotless {
    kotlin {
        ktfmt().googleStyle()
        val disabledSourceSet: Iterable<SourceSet> = qualityExtension.getDisabledSourceSet(project)
        val sourceSets: List<SourceDirectorySet> = disabledSourceSet.map { it.allSource }
        if (sourceSets.isNotEmpty()) {
            project.logger.info("Disable spotless-kotlin for {}:{}", project.name, disabledSourceSet)
            targetExclude(sourceSets)
        }
    }
}

tasks.withType<JavaCompile>().configureEach { dependsOn(tasks.named("spotlessKotlinApply")) }
