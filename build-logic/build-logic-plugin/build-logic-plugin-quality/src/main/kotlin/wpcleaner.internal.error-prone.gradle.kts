import org.wpcleaner.buildlogic.plugin.quality.QualityExtension
import net.ltgt.gradle.errorprone.errorprone

plugins {
    id("java")
    id("net.ltgt.errorprone")
}

val qualityExtension: QualityExtension = QualityExtension.create(project)

dependencies {
    errorprone("com.google.errorprone:error_prone_core:2.24.1")
    annotationProcessor("com.uber.nullaway:nullaway:0.10.14")
}

tasks.withType<JavaCompile>().configureEach {
    options.errorprone {
        // Our old dependencies still use java.util.Date
        disable("JdkObsolete")
        // Unable to understand jupiter private @MethodSource. In 'main' source set this check will be
        // performed by pmd.
        disable("UnusedMethod")
        // Some legacy library still use java.util.Date
        disable("JavaUtilDate")
        // We don't want to annotate all "return this" method with @CanIgnoreReturnValue
        disable("CanIgnoreReturnValueSuggester")
        // error-prone doesn't know BDDMockito
        disable("DirectInvocationOnMock")
        options.errorprone { // Predefined errorprone configuration to avoid failing in case of
            // non-overloading
            option("NullAway:AnnotatedPackages", "")
        }
    }
}

afterEvaluate {
    qualityExtension.getDisabledSourceSet(project).forEach { sourceSet ->
        project.logger.info("Disable error-prone for {}:{}", project.name, sourceSet.name)
        project.tasks.named<JavaCompile>(sourceSet.compileJavaTaskName) {
            options.errorprone.isEnabled.set(false)
        }
    }
}
