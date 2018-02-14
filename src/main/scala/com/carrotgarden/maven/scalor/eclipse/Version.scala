package com.carrotgarden.maven.scalor.eclipse

import scala.util.Failure
import scala.util.Success

import org.eclipse.core.runtime.IProgressMonitor
import org.eclipse.m2e.core.ui.internal.M2EUIPluginActivator
import org.osgi.framework.Bundle
import org.osgi.framework.VersionRange
import org.scalaide.core.internal.ScalaPlugin

import com.carrotgarden.maven.scalor.meta.Macro.nameOf
import com.carrotgarden.maven.scalor.util.Error.Throw
import com.carrotgarden.maven.scalor.util.Error.TryHard
import com.carrotgarden.maven.scalor.util.Logging.AnyLog

/**
 * Version verification support.
 */
trait Version {

  def assertVersion(
    logger :       AnyLog,
    config :       ParamsConfig,
    hasCheck :     Boolean,
    hasError :     Boolean,
    pluginBundle : Bundle,
    pluginName :   String,
    paramName :    String,
    paramValue :   String,
    monitor :      IProgressMonitor
  ) : Unit = {
    if ( hasCheck ) {
      logger.info( s"Verifying ${pluginName} version." )
      val range = rangeFrom( paramValue ) match {
        case Success( range ) =>
          range
        case Failure( error ) =>
          val message = s"Invalid version range in ${paramName}=${paramValue} [${error.getMessage}]"
          logger.fail( message )
          Throw( message )
      }
      val version = pluginBundle.getVersion
      if ( range.includes( version ) ) {
        logger.info( s"   version ${version} is in range ${range}" )
      } else {
        val message = s"${pluginName} version ${version} is out of range ${range}."
        logger.fail( message )
        if ( hasError ) {
          Throw( message )
        }
      }
    }
  }

  /**
   * Verify Eclipse M2E plugin version.
   */
  def assertVersionMaven(
    logger :  AnyLog,
    config :  ParamsConfig,
    monitor : IProgressMonitor
  ) : Unit = {
    import config._
    assertVersion(
      logger,
      config,
      hasCheck     = eclipseMavenPluginVersionCheck,
      hasError     = eclipseMavenPluginVersionError,
      pluginBundle = M2EUIPluginActivator.getDefault.getBundle,
      pluginName   = "Maven M2E",
      paramName    = nameOf( eclipseMavenPluginVersionRange ),
      paramValue   = eclipseMavenPluginVersionRange,
      monitor
    )
  }

  /**
   * Verify Scala IDE plugin version.
   */
  def assertVersionScala(
    logger :  AnyLog,
    config :  ParamsConfig,
    monitor : IProgressMonitor
  ) : Unit = {
    import config._
    assertVersion(
      logger,
      config,
      hasCheck     = eclipseScalaPluginVersionCheck,
      hasError     = eclipseScalaPluginVersionError,
      pluginBundle = ScalaPlugin().getBundle,
      pluginName   = "Scala IDE",
      paramName    = nameOf( eclipseScalaPluginVersionRange ),
      paramValue   = eclipseScalaPluginVersionRange,
      monitor
    )
  }

  /**
   * Verify required Eclipse Platform plugins versions.
   */
  def assertVersion(
    logger :  AnyLog,
    config :  ParamsConfig,
    monitor : IProgressMonitor
  ) : Unit = {
    assertVersionMaven( logger, config, monitor )
    assertVersionScala( logger, config, monitor )
  }

  def rangeFrom( range : String ) = TryHard {
    new VersionRange( range )
  }

}
