package com.carrotgarden.maven.scalor.eclipse

import org.eclipse.core.runtime.IProgressMonitor

import com.carrotgarden.maven.scalor.util.Optioner.convert_Option_Value

/**
 * Provide eclipse .project file comment.
 */
trait Comment {

  self : Maven =>

  import org.eclipse.core.resources.IResource._

  /**
   */
  def ensureProjectComment(
    context : Config.SetupContext,
    monitor : IProgressMonitor
  ) : Unit = {
    import context._
    import config._
    if ( eclipseProjectCommentApply ) {
      logger.info( "Applying Eclipse .project comment." )
      val project = request.getProject
      val description = project.getDescription
      description.setComment( eclipseProjectCommentString )
      project.setDescription( description, FORCE, monitor )
    }
  }

}
