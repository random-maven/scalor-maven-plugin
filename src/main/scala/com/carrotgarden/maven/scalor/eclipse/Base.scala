package com.carrotgarden.maven.scalor.eclipse

import org.apache.maven.plugin.MojoExecution
import org.apache.maven.project.MavenProject
import org.eclipse.core.runtime.IProgressMonitor
import org.eclipse.m2e.core.project.configurator.AbstractProjectConfigurator
import org.eclipse.m2e.core.project.configurator.ProjectConfigurationRequest

object Base {

  /**
   * M2E project configurator java -> scala compatibility layer.
   */
  abstract class Conf extends AbstractProjectConfigurator {

    def baseExecutionList(
      request : ProjectConfigurationRequest,
      monitor : IProgressMonitor
    ) : java.util.List[ MojoExecution ] =
      super.getMojoExecutions( request, monitor )

    def baseParamValue[ T ](
      project :       MavenProject,
      parameter :     String,
      asType :        Class[ T ],
      mojoExecution : MojoExecution,
      monitor :       IProgressMonitor
    ) : T = super.getParameterValue( project, parameter, asType, mojoExecution, monitor )

  }

}
