package com.carrotgarden.maven.scalor.eclipse

import org.eclipse.m2e.core.project.configurator.ProjectConfigurationRequest
import org.eclipse.core.runtime.IProgressMonitor

import com.carrotgarden.maven.scalor.util
import util.Params._
import Params._

import com.carrotgarden.maven.scalor.eclipse.Logging.AnyLog
import org.eclipse.core.resources.IResource

/**
 * Provide eclipse .project file comment.
 */
trait Comment {

  self : Logging with Maven =>

  import Maven._
  import IResource._

  /**
   */
  def ensureProjectComment(
    request : ProjectConfigurationRequest,
    config :  ParamsConfig,
    monitor : IProgressMonitor
  ) : Unit = {
    import config._
    if ( eclipseProjectCommentApply ) {
      log.info( "Applying Eclipse .project comment." )
      val project = request.getProject
      val description = project.getDescription
      description.setComment( eclipseProjectCommentString )
      project.setDescription( description, FORCE, monitor )
    }
  }

}
