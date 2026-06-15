import org.wpcleaner.buildlogic.plugin.quality.QualityExtension
import net.ltgt.gradle.errorprone.errorprone

plugins {
    id("java")
    id("net.ltgt.errorprone")
}

val qualityExtension: QualityExtension = QualityExtension.create(project)

dependencies {
    errorprone("com.google.errorprone:error_prone_core:2.50.0")
    annotationProcessor("com.uber.nullaway:nullaway:0.13.6")
}

tasks.withType<JavaCompile>().configureEach {
    options.errorprone {
        options.errorprone {
            option("NullAway:AcknowledgeRestrictiveAnnotations", "true")
            // Predefined errorprone configuration to avoid failing in case of non-overloading
            option("NullAway:AnnotatedPackages", "")
        }
    }
}

afterEvaluate {
    qualityExtension.getDisabledSourceSet(project).forEach { sourceSet ->
        project.logger.info("Disable error-prone for {}:{}", project.name, sourceSet.name)
        project.tasks.named<JavaCompile>(sourceSet.compileJavaTaskName) {
            options.errorprone.enabled.set(false)
        }
    }
}
