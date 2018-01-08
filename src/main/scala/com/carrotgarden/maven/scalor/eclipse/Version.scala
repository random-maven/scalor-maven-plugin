package com.carrotgarden.maven.scalor.eclipse

import org.eclipse.core.runtime.IProgressMonitor

import org.osgi.framework.VersionRange

import scala.util.Success
import scala.util.Failure

import com.carrotgarden.maven.scalor._

import meta.Macro._
import util.Error._

import org.scalaide.core.internal.ScalaPlugin
import org.osgi.framework.Bundle
import org.eclipse.m2e.core.ui.internal.M2EUIPluginActivator

/**
 * Version verification support.
 */
trait Version {

  self : Logging =>

  def assertVersion(
    config :       ParamsConfig,
    hasCheck :     Boolean,
    hasError :     Boolean,
    pluginBundle : Bundle,
    pluginName :   String,
    paramName :    String,
    paramValue :   String,
    monitor :      IProgressMonitor
  ) : Unit = {
    import config._
    if ( hasCheck ) {
      log.info( s"Verifying ${pluginName} version." )
      val range = rangeFrom( paramValue ) match {
        case Success( range ) =>
          range
        case Failure( error ) =>
          val message = s"Invalid version range in ${paramName}=${paramValue} [${error.getMessage}]"
          log.fail( message )
          Throw( message )
      }
      val version = pluginBundle.getVersion
      if ( range.includes( version ) ) {
        log.info( s"   version ${version} is in range ${range}" )
      } else {
        val message = s"${pluginName} version ${version} is out of range ${range}."
        log.fail( message )
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
    config :  ParamsConfig,
    monitor : IProgressMonitor
  ) : Unit = {
    import config._
    assertVersion(
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
    config :  ParamsConfig,
    monitor : IProgressMonitor
  ) : Unit = {
    import config._
    assertVersion(
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
    config :  ParamsConfig,
    monitor : IProgressMonitor
  ) : Unit = {
    assertVersionMaven( config, monitor )
    assertVersionScala( config, monitor )
  }

  def rangeFrom( range : String ) = TryHard {
    new VersionRange( range )
  }

}
