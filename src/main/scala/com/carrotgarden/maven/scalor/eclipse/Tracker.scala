package com.carrotgarden.maven.scalor.eclipse

import scala.collection.mutable

import org.eclipse.core.resources.IProject
import org.scalaide.core.internal.project.ScalaProject

/**
 * Remember managed projects.
 */
trait Tracker {

  import Tracker._

  private val projectMemento = new mutable.HashMap[ IProject, Context ]

  def projectRegister( project : IProject, config : ParamsConfig ) = projectMemento.synchronized {
    projectMemento += ( project -> Context( config, ScalaIDE.pluginProject( project ) ) )
  }

  def projectUnregister( project : IProject ) = projectMemento.synchronized {
    projectMemento -= project
  }

}

object Tracker {

  case class Context(
    config :  ParamsConfig,
    project : ScalaProject
  )

}
