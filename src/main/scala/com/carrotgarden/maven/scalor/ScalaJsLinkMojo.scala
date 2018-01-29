package com.carrotgarden.maven.scalor

import org.apache.maven.plugin.AbstractMojo
import org.apache.maven.plugins.annotations._
import org.apache.maven.execution.MavenSession
import org.apache.maven.project.MavenProject
import org.apache.maven.plugin.BuildPluginManager
import org.apache.maven.plugin.MojoFailureException
import org.sonatype.plexus.build.incremental.BuildContext

import com.carrotgarden.maven.scalor.util.Folder._

import com.carrotgarden.maven.tools.Description
import java.io.File

/**
 * Shared linker mojo interface.
 * Generate Scala.js runtime.js JavaScript.
 */
trait ScalaJsLinkAnyMojo extends AbstractMojo
  with base.Mojo
  with base.Params
  with base.Logging
  with base.SkipMojo
  with eclipse.Context
  with scalajs.Build
  with scalajs.Linker
  with scalajs.ParamsLinkAny {

  @Description( """
  Flag to skip this execution: <code>scala-js-link-*</code>.
  """ )
  @Parameter(
    property     = "scalor.skipLinker", //
    defaultValue = "false"
  )
  var skipLinker : Boolean = _

  @Description( """
  Regular expression to detect when scalajs-library is present on class path.
  Must provide version capture group.
  Enablement parameter: <a href="#linkerLibraryDetect"><b>linkerLibraryDetect</b></a>.
  """ )
  @Parameter(
    property     = "scalor.linkerLibraryRegex", //
    defaultValue = "^scalajs-library_(.+)[.]jar$"
  )
  var linkerLibraryRegex : String = _

  @Description( """
  Invoke Scala.js linker only when scalajs-library is present
  in project dependencies with given scope.
  Detection parameter: <a href="#linkerLibraryRegex"><b>linkerLibraryRegex</b></a>.
  """ )
  @Parameter(
    property     = "scalor.linkerDetectScalajs", //
    defaultValue = "true"
  )
  var linkerLibraryDetect : Boolean = _

  lazy val linkerClassPath = {
    buildDependencyFolders ++ projectClassPath( buildDependencyScopes )
  }

  /**
   * Discover scalajs-library on project class path.
   */
  def libraryOption = {
    resolveJar( linkerClassPath, linkerLibraryRegex ).right.toOption
  }

  import scalajs.Linker._

  def reportLink( options : Options, classpath : Seq[ File ], runtime : File ) = {
    if ( linkerLogRuntime ) {
      log.info( s"Linker runtime: ${runtime}" )
    }
    if ( linkerLogOptions ) {
      log.info( s"Linker options:\n${config( options )}" )
    }
    if ( linkerLogClassPath ) {
      log.info( s"Linker classpath:" )
      classpath.sorted.foreach( file => log.info( s"   ${file}" ) )
    }
  }

  def invokeLinker() : Unit = {
    val options = Options.parse( linkerOptions )
    val classpath = linkerClassPath.toList
    val runtime = linkerRuntimeFile()
    log.info( s"Invoking Scala.js linker." )
    reportLink( options, classpath, runtime )
    performLink( options, classpath, runtime )
  }

  override def perform() : Unit = {
    if ( skipLinker || hasSkipMojo ) {
      reportSkipReason( "Skipping disabled goal execution." )
      return
    }
    if ( hasIncremental ) {
      reportSkipReason( "Skipping incremental build invocation." )
      return
    }
    if ( linkerLibraryDetect ) {
      val libraryDetect = libraryOption
      if ( libraryDetect.isDefined ) {
        log.info( s"Detected scalajs-library: ${libraryDetect.get.version}." )
        invokeLinker()
      } else {
        log.info( "Missing scalajs-library, skipping execution." )
      }
    } else {
      log.info( "Skipping library detect, forcing linker invocation." )
      invokeLinker()
    }
  }

}

@Description( """
Generate Scala.js runtime JavaScript for scope=main.
""" )
@Mojo(
  name                         = A.mojo.`scala-js-link-main`,
  defaultPhase                 = LifecyclePhase.PROCESS_CLASSES,
  requiresDependencyResolution = ResolutionScope.COMPILE
)
class ScalaJsLinkMainMojo extends ScalaJsLinkAnyMojo
  with scalajs.BuildMain
  with scalajs.ParamsLinkMain {

  override def mojoName = A.mojo.`scala-js-link-main`

  @Description( """
  Flag to skip goal execution: <code>scala-js-link-main</code>.
  """ )
  @Parameter(
    property     = "scalor.skipLinkerMain", //
    defaultValue = "false"
  )
  var skipLinkerMain : Boolean = _

  override def hasSkipMojo = skipLinkerMain

}

@Description( """
Generate Scala.js runtime JavaScript for scope=test.
""" )
@Mojo(
  name                         = A.mojo.`scala-js-link-test`,
  defaultPhase                 = LifecyclePhase.PROCESS_TEST_CLASSES,
  requiresDependencyResolution = ResolutionScope.TEST
)
class ScalaJsLinkTestMojo extends ScalaJsLinkAnyMojo
  with scalajs.BuildTest
  with scalajs.ParamsLinkTest {

  override def mojoName = A.mojo.`scala-js-link-test`

  @Description( """
  Flag to skip this goal execution: <code>scala-js-link-test</code>.
  """ )
  @Parameter(
    property     = "scalor.skipLinkerTest", //
    defaultValue = "false"
  )
  var skipLinkerTest : Boolean = _

  override def hasSkipMojo = skipLinkerTest

}
