import org.wpcleaner.buildlogic.plugin.quality.QualityExtension

plugins { id("pmd") }

val qualityExtension: QualityExtension = QualityExtension.create(project)

pmd {
    toolVersion = "6.55.0"
    isConsoleOutput = true
    ruleSetFiles = project.rootProject.files("config/pmd/pmd-test.xml")
    ruleSets = listOf()
}

tasks.named<Pmd>("pmdMain") { ruleSetFiles = project.rootProject.files("config/pmd/pmd.xml") }

afterEvaluate {
    qualityExtension.getDisabledSourceSet(project).forEach {
        val taskName = it.getTaskName("pmd", null)
        project.logger.info("Disable pmd for {}:{}", project.name, it.name)
        project.tasks.named<Pmd>(taskName) { isEnabled = false }
    }
}
