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

import scala.collection.JavaConverters._
import org.apache.maven.artifact.Artifact

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
    property     = "scalor.skipLinker",
    defaultValue = "false"
  )
  var skipLinker : Boolean = _

  @Description( """
  Regular expression used to detect when Scala.js library is present on class path.
  This regular expression is matched against resolved project depenencies in given scope.
  Regular expression in the form: <code>${groupId}:${artifactId}</code>.
  Enablement parameter: <a href="#linkerLibraryDetect"><b>linkerLibraryDetect</b></a>.
  """ )
  @Parameter(
    property     = "scalor.linkerLibraryRegex",
    defaultValue = "org.scala-js:scalajs-library_.+"
  )
  var linkerLibraryRegex : String = _

  @Description( """
  Invoke Scala.js linker only when Scala.js library is detected
  in project dependencies with given scope.
  Detection parameter: <a href="#linkerLibraryRegex"><b>linkerLibraryRegex</b></a>.
  """ )
  @Parameter(
    property     = "scalor.linkerLibraryDetect",
    defaultValue = "true"
  )
  var linkerLibraryDetect : Boolean = _

  /**
   * Provide linker project build class path.
   */
  def linkerClassPath : Array[ File ] = {
    buildDependencyFolders ++ projectClassPath( buildDependencyScopes )
  }

  /**
   * Discover Scala.js library on project class path.
   */
  def libraryArtifactOption : Option[ Artifact ] = {
    val libraryRegex = linkerLibraryRegex.r
    project.getArtifacts.asScala.find { artifact =>
      import artifact._
      val identity = s"${getGroupId}:${getArtifactId}"
      libraryRegex.pattern.matcher( identity ).matches
    }
  }

  import scalajs.Linker._

  def reportLinker( options : Options, classpath : Array[ File ], runtime : File ) = {
    if ( linkerLogRuntime ) {
      logger.info( s"Linker runtime: ${runtime}" )
    }
    if ( linkerLogOptions ) {
      logger.info( s"Linker options:\n${config( options )}" )
    }
    if ( linkerLogClassPath ) {
      loggerReportFileList( "Linker classpath:", classpath )
    }
  }

  def invokeLinker() : Unit = {
    val options = Options.parse( linkerOptions )
    val classpath = linkerClassPath
    val runtime = linkerRuntimeFile()
    logger.info( s"Invoking Scala.js linker." )
    reportLinker( options, classpath, runtime )
    performLinker( options, classpath, runtime )
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
      val libraryOption = libraryArtifactOption
      if ( libraryOption.isDefined ) {
        logger.info( s"Scala.js library present: ${libraryOption.get}." )
        invokeLinker()
      } else {
        logger.info( s"Scala.js library missing: ${linkerLibraryRegex}, skipping execution." )
      }
    } else {
      logger.info( "Skipping library detect, forcing linker invocation." )
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
    property     = "scalor.skipLinkerMain",
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
    property     = "scalor.skipLinkerTest",
    defaultValue = "false"
  )
  var skipLinkerTest : Boolean = _

  override def hasSkipMojo = skipLinkerTest

}
