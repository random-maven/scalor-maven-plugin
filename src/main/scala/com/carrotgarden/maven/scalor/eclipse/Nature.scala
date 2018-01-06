package com.carrotgarden.maven.scalor.eclipse

import org.eclipse.core.resources.IProject
import org.eclipse.core.runtime.IProgressMonitor
import scala.collection.mutable.ArrayBuffer
import org.eclipse.m2e.core.project.configurator.ProjectConfigurationRequest
import org.eclipse.jdt.core.JavaCore
import org.scalaide.core.SdtConstants
import org.eclipse.core.resources.IResource

/**
 * Provide eclipse .project file natures.
 */
trait Nature {

  self : Logging with Monitor =>

  import Nature._

  /**
   * Create/Delete project natures.
   */
  def ensureNature(
    project :    IProject,
    createList : Array[ String ],
    deleteList : Array[ String ],
    monitor :    IProgressMonitor
  ) : Unit = {
    log.info( "Ensuring project natures." )
    assertCancel( monitor )
    val description = project.getDescription
    val natureList = ArrayBuffer( description.getNatureIds : _* )
    var hasChange = false
    createList.foreach { natureId =>
      if ( !project.hasNature( natureId ) ) {
        natureList += natureId
        hasChange = true
      }
    }
    deleteList.foreach { natureId =>
      if ( project.hasNature( natureId ) ) {
        natureList -= natureId
        hasChange = true
      }
    }
    if ( hasChange ) {
      persistNature( project, natureList.toArray, monitor )
    } else {
      reportWorked( monitor )
    }
  }

  /**
   * Provide eclipse .project descriptor natures.
   */
  def ensureProjectNature(
    request : ProjectConfigurationRequest,
    config :  ParamsConfig,
    monitor : IProgressMonitor
  ) : Unit = {
    val createList = Array[ String ]( javaNatureId, scalaNatureId ) // XXX
    val deleteList = Array[ String ]()
    val project = request.getProject
    ensureNature( project, createList, deleteList, monitor )
  }

}

object Nature {

  val javaNatureId = JavaCore.NATURE_ID
  val scalaNatureId = SdtConstants.NatureId

  import IResource._

  /**
   * Work around Eclipse ignoring nature ordering.
   */
  def persistNature(
    project :    IProject,
    natureList : Array[ String ],
    monitor :    IProgressMonitor
  ) = {
    val description = project.getDescription
    // delete
    description.setNatureIds( Array() )
    project.setDescription( description, FORCE | AVOID_NATURE_CONFIG, monitor )
    // create
    description.setNatureIds( natureList )
    project.setDescription( description, FORCE, monitor )
  }

}
