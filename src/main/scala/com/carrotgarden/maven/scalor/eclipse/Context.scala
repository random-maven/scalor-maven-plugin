package com.carrotgarden.maven.scalor.eclipse

import org.apache.maven.plugins.annotations.Component
import org.sonatype.plexus.build.incremental.BuildContext

import com.carrotgarden.maven.scalor.base
import com.carrotgarden.maven.scalor.util.Error.TryHard
import com.carrotgarden.maven.tools.Description
import org.apache.maven.execution.MavenSession

/**
 * Eclipse/Maven (M2E) build integration context.
 */
@Description( """
Note: controlled OSGI environment.
""" )
trait Context {

  self : base.Params =>

  import Context._

  @Description( """
  Eclipse build integration context.
  Provides full implementation when running in M2E.
  Provides dummy implementation when running in Maven.
  """ )
  @Component()
  var buildContext : BuildContext = _

  /**
   * Connect this Maven plugin with host Eclipse plugins.
   * Only used when running inside Eclipse platform with M2E.
   */
  // Lazy, for plexus injector.
  lazy val wiringHandle = TryHard {
    Wiring( buildContext ).setup
  }

  /**
   * Extract incremental build state.
   */
  def contextExtract[ T ]( key : String ) : Option[ T ] = {
    Option( buildContext.getValue( key ).asInstanceOf[ T ] )
  }

  /**
   * Persist incremental build state.
   */
  def contextPersist[ T ]( key : String, option : Option[ T ] = None ) : Unit = {
    option match {
      case None          => buildContext.setValue( key, null )
      case Some( value ) => buildContext.setValue( key, value )
    }
  }

  /**
   * Project pom.xml open in the IDE.
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
  def hasEclipseContext = eclipse.isPresent( session ) || m2e.isPresent( session )

}

object Context {

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
    def isPresent( session : MavenSession ) = application != ""

  }

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
    def version( session : MavenSession ) = session.getUserProperties.getProperty( key, "" )

    /** Verify invocation from Eclipse. */
    def isPresent( session : MavenSession ) = version( session ) != ""

  }

}
