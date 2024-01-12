package org.wpcleaner.buildlogic.plugin.quality

import javax.inject.Inject
import org.gradle.api.Namer
import org.gradle.api.Project
import org.gradle.api.internal.AbstractValidatingNamedDomainObjectContainer
import org.gradle.api.internal.CollectionCallbackActionDecorator
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.reflect.TypeOf
import org.gradle.api.tasks.SourceSet
import org.gradle.internal.reflect.Instantiator

open class QualityExtensionImpl @Inject constructor(instantiator: Instantiator) :
  AbstractValidatingNamedDomainObjectContainer<QualityConfiguration>(
    QualityConfiguration::class.java,
    instantiator,
    Namer { obj: QualityConfiguration -> obj.name },
    CollectionCallbackActionDecorator.NOOP
  ),
  QualityExtension {

  override fun doCreate(name: String): QualityConfiguration =
    this.instantiator.newInstance(QualityConfiguration::class.java, name)

  override fun getPublicType(): TypeOf<*> = TypeOf.typeOf(QualityExtensionImpl::class.java)

  override fun getDisabledSourceSet(project: Project): Iterable<SourceSet> =
    getAvailableSourceSet(project).filter { isDisabled(it) }

  override fun getEnabledSourceSet(project: Project): Iterable<SourceSet> =
    getAvailableSourceSet(project).filter { isEnabled(it) }

  private fun isDisabled(sourceSet: SourceSet): Boolean =
    this.any { config: QualityConfiguration -> config.isDisabled && config.name == sourceSet.name }

  private fun isEnabled(sourceSet: SourceSet): Boolean = !isDisabled(sourceSet)

  private fun getAvailableSourceSet(project: Project): Collection<SourceSet> {
    val javaPlugin = project.extensions.findByType(JavaPluginExtension::class.java)
    return javaPlugin?.sourceSets ?: listOf()
  }
}
