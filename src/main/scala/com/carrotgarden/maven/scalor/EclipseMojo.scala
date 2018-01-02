package com.carrotgarden.maven.scalor

import org.apache.maven.plugin.AbstractMojo
import org.apache.maven.plugins.annotations._
import com.carrotgarden.maven.tools.Description

import A.mojo._

import util.OSGI._
import util.Error._
import util.Params._
import util.Classer._
import util.Props._

import eclipse.Wiring._
import eclipse.Plugin

import base.Params._

import scala.util.Success
import scala.util.Failure

import scala.collection.JavaConverters._

import org.eclipse.m2e.core.ui.internal.UpdateMavenProjectJob
import org.eclipse.core.resources.IProject

@Description( """
Install companion Eclipse plugin.
""" )
@Mojo(
  name                         = `eclipse-config`,
  defaultPhase                 = LifecyclePhase.INITIALIZE,
  requiresDependencyResolution = ResolutionScope.NONE
)
class EclipseConfigMojo extends EclipseAnyMojo
  with base.ParamsArtifact
  with eclipse.Params {

  import EclipseConfigMojo._

  override def mojoName = `eclipse-config`

  override def performEclipse = {
    say.info( "Configuring companion Eclipse plugin:" )
    val handle = resolveHandle

    val descriptorUrl = Plugin.Config.pluginPropertiesUrl
    val descriptorProps = propertiesFrom( descriptorUrl )

    val pluginId = descriptorProps.getProperty( Plugin.Config.key.pluginId )
    val pluginLocation = Plugin.Config.location
    say.info( "   pluginId: " + pluginId )
    say.info( "   location: " + pluginLocation )

    val context = handle.bundleM2E.getBundleContext

    val pluginOption = Option( context.getBundle( pluginLocation ) )

    val ( message, pluginBundle, hasProjectUpdate ) = if ( pluginOption.isDefined ) {
      ( "Companion plugin was previously installed", pluginOption.get, false )
    } else {
      context.installBundle( pluginLocation ).start()
      ( "Companion plugin is now installed in Eclipse", context.getBundle( pluginLocation ), true )
    }

    say.info( message + ": " + pluginBundle )

    if ( hasProjectUpdate ) {
      say.info( "Scheduling project update in Eclipse to invoke M2E project configurator." )
      val projectList = handle.workspace.getRoot.getProjects
      val currentProject = projectWithBase( projectList, project.getBasedir )
      // TODO remove dependency on m2e.core.ui
      val updateJob = new UpdateMavenProjectJob( Array[ IProject ]( currentProject ) )
      val updateName = "Project update for: " + pluginId + " @ " + project.getArtifactId
      updateJob.setName( updateName )
      updateJob.setPriority( 10 ) // INTERACTIVE
      updateJob.schedule( 1 * 1000 )
    }

  }

}

object EclipseConfigMojo {

}

/**
 * Shared Eclipse mojo interface.
 */
trait EclipseAnyMojo extends base.Mojo {

  @Description( """
  Flag to skip goal execution: eclipse-*.
  """ )
  @Parameter(
    property     = "scalor.skipEclipse", //
    defaultValue = "false"
  )
  var skipEclipse : Boolean = _

  @Description( """
  Invoke eclipse configuration executions
  only when runnting inside Eclipse/M2E.
  """ )
  @Parameter(
    property     = "scalor.eclipseDetectPresent", //
    defaultValue = "true"
  )
  var eclipseDetectPresent : Boolean = _

  def reportHandle = wiringHandle match {
    case Success( handle ) =>
      say.info( "Using Eclipse platform plugins:" )
      say.info( s"   ${handle.resourcesPlugin.getBundle}" )
      say.info( s"   ${handle.mavenPlugin.getBundle}" )
      say.info( s"   ${handle.mavenPluginUI.getBundle}" )
    case Failure( error ) =>
      say.error( "Required eclipse plugin is missing: " + error )
  }

  def resolveHandle = wiringHandle match {
    case Success( handle ) =>
      handle
    case Failure( error ) =>
      throw error
  }

  def performEclipse : Unit

  override def perform() : Unit = {
    if ( skipEclipse ) {
      reportSkipReason( "Skipping disabled goal execution." )
      return
    }
    if ( eclipseDetectPresent && !hasEclipse ) {
      reportSkipReason( "Skipping non-eclipse build invocation." )
      return
    }
    if ( hasIncremental ) {
      reportSkipReason( "Skipping incremental build invocation." )
      return
    }
    reportHandle
    resolveHandle
    performEclipse
  }

}
