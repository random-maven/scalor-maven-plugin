package com.carrotgarden.maven.scalor.eclipse

import scala.collection.mutable.ArrayBuffer

import org.eclipse.core.resources.IProject
import org.eclipse.core.runtime.IProgressMonitor
import org.eclipse.jdt.core.JavaCore
import org.scalaide.core.SdtConstants

import com.carrotgarden.maven.scalor.util.Optioner.convert_Option_Value

/**
 * Provide eclipse .project file natures.
 */
trait Nature {

  self : Monitor =>

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
    }
  }

  /**
   * Provide eclipse .project descriptor natures.
   */
  def ensureProjectNature(
    context : Config.SetupContext,
    monitor : IProgressMonitor
  ) : Unit = {
    import context._
    logger.info( "Ensuring required .project natures." )
    val createList = Array[ String ]( javaNatureId, scalaNatureId ) // XXX
    val deleteList = Array[ String ]()
    val project = request.getProject
    ensureNature( project, createList, deleteList, monitor )
  }

}

object Nature {

  val javaNatureId = JavaCore.NATURE_ID
  val scalaNatureId = SdtConstants.NatureId

  import org.eclipse.core.resources.IResource._

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
