import org.wpcleaner.buildlogic.plugin.quality.QualityExtension
import org.wpcleaner.buildlogic.plugin.quality.formatter.AnnotationOrderFormatterStep
import org.wpcleaner.buildlogic.plugin.quality.formatter.EmptyLineFormatterStep
import org.wpcleaner.buildlogic.plugin.quality.formatter.JavaModuleFormatterStep
import org.wpcleaner.buildlogic.plugin.quality.formatter.JavaTestStepFormatterStep
import org.wpcleaner.buildlogic.plugin.quality.formatter.PreconditionsFormatterStep
import org.wpcleaner.buildlogic.plugin.quality.formatter.ReturnOperationFormatterStep

plugins { id("com.diffplug.spotless") }

val qualityExtension: QualityExtension = QualityExtension.create(project)

spotless {
    java {
        googleJavaFormat("1.30.0")
        addStep(AnnotationOrderFormatterStep())
        addStep(EmptyLineFormatterStep())
        addStep(JavaModuleFormatterStep())
        addStep(JavaTestStepFormatterStep())
        addStep(PreconditionsFormatterStep())
        addStep(ReturnOperationFormatterStep())
        val disabledSourceSet: Iterable<SourceSet> = qualityExtension.getDisabledSourceSet(project)
        val sourceSets: List<SourceDirectorySet> = disabledSourceSet.map { it.allJava }
        if (sourceSets.isNotEmpty()) {
            project.logger.info("Disable spotless-java for {}:{}", project.name, disabledSourceSet)
            targetExclude(sourceSets)
        }
    }
}

tasks.withType<JavaCompile>().configureEach { dependsOn(tasks.named("spotlessJavaApply")) }
