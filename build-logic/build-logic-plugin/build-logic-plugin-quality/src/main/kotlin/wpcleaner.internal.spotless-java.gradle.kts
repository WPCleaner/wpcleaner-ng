import org.wpcleaner.buildlogic.plugin.quality.QualityExtension
import org.wpcleaner.buildlogic.plugin.quality.formatter.AnnotationOrderFormatterStep
import org.wpcleaner.buildlogic.plugin.quality.formatter.EmptyLineFormatterStep
import org.wpcleaner.buildlogic.plugin.quality.formatter.JavaModuleFormatterStep
import org.wpcleaner.buildlogic.plugin.quality.formatter.JavaTestStepFormatterStep
import org.wpcleaner.buildlogic.plugin.quality.formatter.PreconditionsFormatterStep
import org.wpcleaner.buildlogic.plugin.quality.formatter.ReturnOperationFormatterStep

plugins { id("wpcleaner.internal.spotless") }

val qualityExtension: QualityExtension = QualityExtension.create(project)

spotless {
    java {
        toggleOffOn()
        forbidWildcardImports()
        googleJavaFormat("1.35.0")
        addStep(AnnotationOrderFormatterStep())
        addStep(EmptyLineFormatterStep())
        addStep(JavaModuleFormatterStep())
        addStep(JavaTestStepFormatterStep())
        addStep(PreconditionsFormatterStep())
        addStep(ReturnOperationFormatterStep())
        target("src/*/java/**/*.java")
        val disabledSourceSet: Iterable<SourceSet> = qualityExtension.getDisabledSourceSet(project)
        val excludedPaths = disabledSourceSet.map { "src/${it.name}/**" }
        if (excludedPaths.isNotEmpty()) {
            project.logger.info("Disable spotless-java for {}:{}", project.name, disabledSourceSet)
            targetExclude(excludedPaths)
        }
    }
}

tasks.withType<JavaCompile>().configureEach { dependsOn(tasks.named("spotlessJavaApply")) }
