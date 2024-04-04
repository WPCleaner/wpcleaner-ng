import org.wpcleaner.buildlogic.plugin.quality.QualityExtension

plugins { id("com.diffplug.spotless") }

val qualityExtension: QualityExtension = QualityExtension.create(project)

spotless {
    java {
        googleJavaFormat("1.22.0")
        val disabledSourceSet: Iterable<SourceSet> = qualityExtension.getDisabledSourceSet(project)
        val sourceSets: List<SourceDirectorySet> = disabledSourceSet.map { it.allJava }
        if (sourceSets.isNotEmpty()) {
            project.logger.info("Disable spotless-java for {}:{}", project.name, disabledSourceSet)
            targetExclude(sourceSets)
        }
    }
}

tasks.withType<JavaCompile>().configureEach { dependsOn(tasks.named("spotlessJavaApply")) }
