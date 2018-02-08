package com.carrotgarden.maven.scalor.eclipse

import org.apache.maven.plugin.MojoExecution
import org.eclipse.core.runtime.IProgressMonitor
import org.eclipse.m2e.core.project.IMavenProjectFacade
import org.eclipse.m2e.core.project.configurator.ProjectConfigurationRequest
import org.eclipse.m2e.jdt.IClasspathDescriptor

import com.carrotgarden.maven.scalor.A
import com.carrotgarden.maven.scalor.util.Logging.AnyLog

/**
 * Extract plugin configuration parameters.
 */
trait Config {

  self : Monitor with Maven =>

  import Config._

  /**
   * Extract plugin configuration parameters for given goal.
   */
  def paramsAnyGoal[ T <: ParamsConfigValue ](
    logger :  AnyLog,
    facade :  IMavenProjectFacade,
    goal :    String,
    config :  T,
    monitor : IProgressMonitor
  ) : T = {
    config.update( extractGoalValue( facade, monitor, goal ) )
    if ( config.paramsLogConfig ) {
      val report = new StringBuffer()
      config.reportParams( paramsAnyReport( report ) )
      logger.info( s"Plugin parameters for ${goal}:\n${report}" )
    }
    config
  }

  /**
   * Report named parameter from this plugin configuration.
   */
  def paramsAnyReport( report : StringBuffer )( name : String, value : Any ) : Unit = {
    val text = if ( value.isInstanceOf[ Array[ _ ] ] ) {
      value.asInstanceOf[ Array[ _ ] ].mkString( "[ ", " , ", " ]" )
    } else {
      value
    }
    report.append( s"   ${name}=${text}\n" )
  }

  /**
   * Extract plugin configuration parameters for `eclipse-config`.
   */
  def paramsConfig(
    logger :  AnyLog,
    facade :  IMavenProjectFacade,
    monitor : IProgressMonitor
  ) : ParamsConfig = {
    paramsAnyGoal( logger, facade, A.mojo.`eclipse-config`, ParamsConfig(), monitor )
  }

  /**
   * Extract plugin configuration parameters for `eclipse-restart`.
   */
  def paramsRestart(
    logger :  AnyLog,
    facade :  IMavenProjectFacade,
    monitor : IProgressMonitor
  ) : ParamsRestart = {
    paramsAnyGoal( logger, facade, A.mojo.`eclipse-restart`, ParamsRestart(), monitor )
  }

  /**
   * Extract plugin configuration parameters for `eclipse-prescomp`.
   */
  def paramsPrescomp(
    logger :  AnyLog,
    facade :  IMavenProjectFacade,
    monitor : IProgressMonitor
  ) : ParamsPrescomp = {
    paramsAnyGoal( logger, facade, A.mojo.`eclipse-prescomp`, ParamsPrescomp(), monitor )
  }

}

object Config {

  trait Context {
    val logger : AnyLog
    val config : ParamsConfig
    val facade : IMavenProjectFacade
  }

  /**
   * Build participant context.
   */
  case class BuildContext(
    logger :    AnyLog,
    config :    ParamsConfig,
    restart :   Option[ ParamsRestart ]  = None,
    prescomp :  Option[ ParamsPrescomp ] = None,
    facade :    IMavenProjectFacade,
    execution : MojoExecution
  ) extends Context

  /**
   * Project configurator context.
   */
  case class SetupContext(
    logger :    AnyLog,
    config :    ParamsConfig,
    facade :    IMavenProjectFacade,
    request :   Option[ ProjectConfigurationRequest ] = None,
    classpath : Option[ IClasspathDescriptor ]        = None
  ) extends Context

}
