package com.carrotgarden.maven.scalor.eclipse

import org.sonatype.plexus.build.incremental.BuildContext
import org.apache.maven.plugins.annotations.Component
import org.apache.maven.plugins.annotations.Parameter

import com.carrotgarden.maven.scalor.base

import com.carrotgarden.maven.tools.Description
import com.carrotgarden.maven.scalor.util._

/**
 * Eclipse/Maven (M2E) build integration context.
 */
trait Build {

  self : base.Params =>

  @Description( """
  Eclipse build integration context.
  """ )
  @Component()
  var buildContext : BuildContext = _

  /**
   * Connect this Maven plugin with host Eclipse plugins.
   */
  lazy val wiringHandle = Error.TryHard {
    Wiring( buildContext ).setup
  }

  /**
   * Project pom.xml open in IDE.
   */
  def sourcePomFile = project.getModel.getPomFile

  /**
   * Remove build messages from Eclipse.
   */
  def contextReset() = buildContext.removeMessages( sourcePomFile );

  /**
   * Display build warnings in Eclipse.
   */
  def contextWarn( message : String, error : Throwable = null ) =
    buildContext.addMessage( sourcePomFile, 1, 1, message, BuildContext.SEVERITY_WARNING, error );

  /**
   * Display build errors in Eclipse.
   */
  def contextError( message : String, error : Throwable = null ) =
    buildContext.addMessage( sourcePomFile, 1, 1, message, BuildContext.SEVERITY_ERROR, error );

  /**
   * Detect incremental build from IDE.
   */
  def hasIncremental = buildContext.isIncremental()

  /**
   * Detect build invocation from IDE.
   */
  def hasEclipse = eclipse.isPresent || m2e.isPresent

  /**
   * M2E detection.
   */
  object m2e {
    /**
     *  A project property set by M2E, i.e.:
     *  m2e.version=1.9.0
     */
    val key = "m2e.version"
    /** M2E version when inside the Eclipse. */
    def version = session.getUserProperties.getProperty( key, "" )
    /** Verify invocation from Eclipse. */
    def isPresent = version != ""
  }

  /**
   * Eclipse detection.
   */
  object eclipse {
    /**
     *  A system property set by Eclipse, i.e.:
     *  eclipse.application=org.eclipse.ui.ide.workbench
     */
    val key = "eclipse.application"
    /** Eclipse application when inside the Eclipse. */
    def application = System.getProperty( key, "" )
    /** Verify invocation from Eclipse. */
    def isPresent = application != ""
  }

}

object Build {

}
