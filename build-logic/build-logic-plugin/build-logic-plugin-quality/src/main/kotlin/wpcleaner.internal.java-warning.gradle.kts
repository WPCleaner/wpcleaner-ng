import org.wpcleaner.buildlogic.plugin.quality.QualityExtension

val qualityExtension: QualityExtension = QualityExtension.create(project)

afterEvaluate {
  qualityExtension.getEnabledSourceSet(project).forEach { sourceSet ->
    val taskName = sourceSet.compileJavaTaskName
    project.logger.info("Enable java-warning for {}:{}", project.name, sourceSet.name)
    project.tasks.named<JavaCompile>(taskName) {
      options.compilerArgs.addAll(
        listOf(
          "-Xlint:all,-processing,-requires-transitive-automatic,-requires-automatic,-missing-explicit-ctor",
          "-Werror"
        )
      )
    }
  }
}
