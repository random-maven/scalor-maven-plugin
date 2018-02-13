package com.carrotgarden.maven.scalor.base

import java.io.File

import scala.util.matching.Regex

import org.apache.maven.execution.MavenSession
import org.apache.maven.plugins.annotations.Component
import org.codehaus.plexus.util.Scanner
import org.sonatype.plexus.build.incremental.BuildContext

import com.carrotgarden.maven.scalor.util
import com.carrotgarden.maven.tools.Description
import org.apache.maven.plugin.AbstractMojo

/**
 * Build integration context.
 */
trait Context {

  self : Params with AbstractMojo =>

  import Context._

  @Description( """
  Eclipse build integration context.
  Provides full implementation when running in M2E.
  Provides dummy implementation when running in Maven.
  """ )
  @Component()
  var buildContext : BuildContext = _

  /**
   * Extract incremental build state.
   */
  def contextExtract[ T <: Object ]( key : String ) : Option[ T ] = {
    Option( buildContext.getValue( key ).asInstanceOf[ T ] )
  }

  /**
   * Persist incremental build state.
   */
  def contextPersist[ T <: Object ]( key : String, option : Option[ T ] = None ) : Unit = {
    option match {
      case Some( value ) => buildContext.setValue( key, value )
      case None          => buildContext.setValue( key, null )
    }
  }

  /**
   * Provide incremental build state value.
   */
  def contextValue[ T <: Object ]( key : String )( provide : => T ) : T = {
    contextExtract[ T ]( key ).getOrElse {
      val value = provide
      contextPersist( key, Some( value ) )
      value
    }
  }

  /**
   * Scanner to find changed files.
   */
  def contextChangeScanner( root : File ) : Scanner = {
    buildContext.newScanner( root )
  }

  /**
   * Scanner to find deleted files.
   */
  def contextDeleteScanner( root : File ) : Scanner = {
    buildContext.newDeleteScanner( root )
  }

  /**
   * List of files changed since previous build.
   */
  def contextChangeResult( root : File, regex : Regex ) : Array[ File ] = {
    val scanner = contextScannerInvoke( true, root, regex )
    contextScannerReport( scanner, root, regex )
  }

  /**
   * List of files deleted since previous build.
   */
  def contextDeleteResult( root : File, regex : Regex ) : Array[ File ] = {
    val scanner = contextScannerInvoke( false, root, regex )
    contextScannerReport( scanner, root, regex )
  }

  /**
   * List of files changed or deleted since previous build.
   */
  def contextUpdateResult( root : File, regex : Regex ) : UpdateResult = {
    val changed = contextChangeResult( root, regex )
    val deleted = contextDeleteResult( root, regex )
    UpdateResult(
      basedir = root,
      changed = changed,
      deleted = deleted
    )
  }

  /**
   * List of files changed or deleted since previous build.
   */
  def contextUpdateResult( rootList : Array[ File ], regex : Regex ) : Array[ UpdateResult ] = {
    rootList.map( root => contextUpdateResult( root, regex ) )
  }

  /**
   * Perform change detection with given mode.
   */
  def contextScannerInvoke( change : Boolean, root : File, regex : Regex ) : Scanner = {
    val scanner = if ( change ) contextChangeScanner( root ) else contextDeleteScanner( root )
    scanner.setIncludes( null )
    scanner.setExcludes( null )
    scanner.scan()
    scanner
  }

  /**
   * Perform change detection with given mode.
   */
  def contextScannerReport( scanner : Scanner, root : File, regex : Regex ) : Array[ File ] = {
    val matcher = regex.pattern.matcher( "" )
    scanner.getIncludedFiles
      .filter { path => matcher.reset( path ).matches }
      .map( path => new File( root, path ).getAbsoluteFile )
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

  /**
   *  Plugin container context.
   */
  def pluginContext = getPluginContext.asInstanceOf[ java.util.Map[ String, Object ] ]

  /**
   * Extract plugin build state.
   */
  def pluginExtract[ T <: Object ]( key : String ) : Option[ T ] = {
    Option( pluginContext.get( key ).asInstanceOf[ T ] )
  }

  /**
   * Persist plugin build state.
   */
  def pluginPersist[ T <: Object ]( key : String, option : Option[ T ] = None ) : Unit = {
    option match {
      case Some( value ) => pluginContext.put( key, value )
      case None          => pluginContext.remove( key )
    }
  }

  /**
   * Provide incremental build state value.
   */
  def pluginValue[ T <: Object ]( key : String )( provide : => T ) : T = {
    pluginExtract[ T ]( key ).getOrElse {
      val value = provide
      pluginPersist( key, Some( value ) )
      value
    }
  }

}

object Context {

  /**
   * Incremental build change detection result.
   */
  case class UpdateResult(
    basedir : File,
    changed : Array[ File ],
    deleted : Array[ File ]
  ) {

    def hasUpdate = {
      changed.length > 0 || deleted.length > 0
    }

    def report : String = {
      val base = basedir.getAbsoluteFile.toPath
      def reportList( list : Array[ File ] ) = {
        util.Text.reportArray(
          list.map( file => base.relativize( file.getAbsoluteFile.toPath ) )
        )
      }
      val text = new StringBuffer
      import text._
      append( s"basedir: ${basedir}" )
      if ( changed.length > 0 ) append( s"\nchanged:\n${reportList( changed )}" )
      if ( deleted.length > 0 ) append( s"\ndeleted:\n${reportList( deleted )}" )
      text.toString
    }

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
