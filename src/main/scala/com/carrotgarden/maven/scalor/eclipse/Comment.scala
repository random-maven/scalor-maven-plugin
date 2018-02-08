package com.carrotgarden.maven.scalor.eclipse

import org.eclipse.m2e.core.project.configurator.ProjectConfigurationRequest
import org.eclipse.core.runtime.IProgressMonitor

import com.carrotgarden.maven.scalor.util
import util.Params._
import ParamsConfigBase._

import org.eclipse.core.resources.IResource
import com.carrotgarden.maven.scalor.util.Logging

import util.Option.convert._

/**
 * Provide eclipse .project file comment.
 */
trait Comment {

  self : Maven =>

  import Maven._
  import IResource._

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
