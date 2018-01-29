package com.carrotgarden.maven.scalor.eclipse

import scala.reflect.ClassTag

import org.eclipse.core.runtime.IProgressMonitor
import org.eclipse.m2e.core.project.IMavenProjectFacade

import com.carrotgarden.maven.scalor._
import org.apache.maven.plugin.MojoExecution
import org.eclipse.m2e.core.lifecyclemapping.model.IPluginExecutionMetadata
import org.eclipse.m2e.core.project.configurator.MojoExecutionBuildParticipant
import org.eclipse.core.resources.IProject
import org.eclipse.m2e.core.project.configurator.AbstractBuildParticipant
import org.eclipse.m2e.core.project.configurator.AbstractBuildParticipant2
import org.eclipse.core.runtime.SubMonitor
import com.carrotgarden.maven.scalor.util.Logging
import org.scalaide.core.internal.project.ScalaProject

/**
 * Assemble M2E project configurator components.
 */
object Project {

  /**
   * Project configurator.
   */
  trait Configurator extends Base.Conf
    with Comment
    with Config
    with Entry
    with Hack
    with Logging
    with Maven
    with Monitor
    with Nature
    with Order
    with Props
    with Version
    with MavenM2E
    with ScalaIDE {

  }

  import scala.collection.mutable

  /**
   * Remember managed projects.
   */
  trait Tracker {
    import Tracker._

    private val projectMap = new mutable.HashMap[ IProject, Context ]

    def projectRegister( project : IProject, config : ParamsConfig ) = projectMap.synchronized {
      projectMap += ( project -> Context( config, ScalaIDE.pluginProject( project ) ) )
    }

    def projectUnregister( project : IProject ) = projectMap.synchronized {
      projectMap -= project
    }

  }

  object Tracker {
    case class Context(
      config :  ParamsConfig,
      project : ScalaProject
    )
  }

}
