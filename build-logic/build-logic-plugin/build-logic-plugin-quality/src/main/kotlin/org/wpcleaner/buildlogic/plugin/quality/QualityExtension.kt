package org.wpcleaner.buildlogic.plugin.quality

import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer

interface QualityExtension : NamedDomainObjectContainer<QualityConfiguration> {

  companion object {

    private const val NAME = "quality"

    fun create(project: Project): QualityExtension {
      val extension: QualityExtension? = project.extensions.findByType(QualityExtension::class.java)
      return extension ?: init(project)
    }

    private fun init(project: Project): QualityExtension {
      val qualityExtension =
        project.extensions.create(
          QualityExtension::class.java,
          NAME,
          QualityExtensionImpl::class.java
        )
      val sourceSet: SourceSetContainer? =
        project.extensions.findByType(JavaPluginExtension::class.java)?.sourceSets

      sourceSet?.forEach {
        project.logger.info("Add quality configuration for {}:{}", project.name, it.name)
        qualityExtension.register(it.name)
      }

      sourceSet?.whenObjectAdded {
        project.logger.info("Add quality configuration for {}:{}", project.name, this.name)
        qualityExtension.register(this.name)
      }

      return qualityExtension
    }
  }

  fun getDisabledSourceSet(project: Project): Iterable<SourceSet>

  fun getEnabledSourceSet(project: Project): Iterable<SourceSet>
}
