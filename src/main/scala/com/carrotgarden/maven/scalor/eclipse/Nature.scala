package com.carrotgarden.maven.scalor.eclipse

import org.eclipse.core.resources.IProject
import org.eclipse.core.runtime.IProgressMonitor
import scala.collection.mutable.ArrayBuffer
import org.eclipse.m2e.core.project.configurator.ProjectConfigurationRequest
import org.eclipse.jdt.core.JavaCore
import org.scalaide.core.SdtConstants

/**
 * Provide eclipse .project file natures, with optional sort.
 */
trait Nature {

  self : Logging with Monitor =>

  import Nature._

  /**
   * Provide Java and Scala nature, with optional sort.
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
    if ( !hasChange && natureList != natureList.sorted ) {
      hasChange = true
    }
    if ( hasChange ) {
      description.setNatureIds( natureList.sorted.toArray )
      project.setDescription( description, monitor )
    } else {
      reportWorked( monitor )
    }
  }

  def ensureNature(
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

}