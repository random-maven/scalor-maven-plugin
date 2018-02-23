package com.carrotgarden.maven.scalor

import java.io.File

import scala.collection.JavaConverters.asScalaSetConverter

import org.apache.maven.artifact.Artifact
import org.apache.maven.plugin.AbstractMojo
import org.apache.maven.plugins.annotations.Mojo
import org.apache.maven.plugins.annotations.Parameter
import org.apache.maven.plugins.annotations.LifecyclePhase
import org.apache.maven.plugins.annotations.ResolutionScope

import com.carrotgarden.maven.scalor.base.Context.UpdateResult
import com.carrotgarden.maven.scalor.scalajs.Linker
import com.carrotgarden.maven.tools.Description

/**
 * Shared linker mojo interface.
 * Generate Scala.js runtime.js JavaScript.
 */
trait ScalaJsLinkAnyMojo extends AbstractMojo
  with base.Mojo
  with base.Params
  with base.Logging
  with base.SkipMojo
  with com.carrotgarden.maven.scalor.base.Context
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

  /**
   * Provide linker project build class path.
   */
  lazy val linkerClassPath : Array[ File ] = {
    buildDependencyFolders ++ projectClassPath( buildDependencyScopes )
  }

  /**
   * Discover Scala.js library on project class path.
   */
  lazy val libraryArtifactOption : Option[ Artifact ] = {
    util.Maven.locateArtifact( project, linkerLibraryRegex )
  }

  /**
   * Produce user reports.
   */
  def linkerReport( context : Linker.Context ) : Unit = {
    import context._
    if ( linkerLogRuntime ) {
      logger.info( s"Linker runtime: ${runtime}" )
    }
    if ( linkerLogOptions ) {
      logger.info( s"Linker options:\n${Linker.newConfig( options )}" )
    }
    if ( linkerLogClassPath ) {
      loggerReportFileList( "Linker classpath:", classpath )
    }
    if ( linkerLogUpdateResult ) {
      val report = if ( hasUpdate ) {
        updateList.filter( _.hasUpdate ).map( _.report ).mkString( "\n", "\n", "" )
      } else {
        "None"
      }
      logger.info( s"Linker update result: ${report}" )
    }
  }

  /**
   * Produce linker runtime.js.
   */
  def linkerInvoke(
    runtime :    File,
    options :    String,
    updateList : Array[ UpdateResult ]
  ) : Unit = {
    val linkerOptions = Linker.Options.parse( options )
    val context = Linker.Context(
      options          = linkerOptions,
      classpath        = linkerClassPath,
      runtime          = runtime,
      updateList       = updateList,
      initializerList  = linkerInitializerList,
      initializerRegex = linkerInitializerRegex,
      hasLogStats      = linkerLogBuildStats
    )
    linkerReport( context )
    linkerPerform( context )
  }

  /**
   * Generate runtime.js and runtime.min.js.
   */
  def performLinker( updateList : Array[ UpdateResult ] ) : Unit = {
    val hasBuild = linkerHasBuild( hasIncremental )
    if ( hasBuild ) {
      linkerInvoke( linkerRuntimeFile, linkerRuntimeOptions, updateList )
    }
    val hasBuildMin = linkerHasBuildMin( hasIncremental )
    if ( hasBuildMin ) {
      linkerInvoke( linkerRuntimeMinFile, linkerRuntimeMinOptions, updateList )
    }
  }

  def performInvocation() : Unit = {
    if ( linkerLibraryDetect && libraryArtifactOption.isEmpty ) {
      reportSkipReason( s"Skipping execution: Scala.js library missing: ${linkerLibraryRegex}." )
      return
    }
    val updateList = if ( hasIncremental ) {
      contextUpdateResult( buildDependencyFolders, linkerClassRegex.r )
    } else {
      Array.empty[ UpdateResult ]
    }
    if ( hasIncremental ) {
      logger.info( s"Incremental build request." )
      val hasUpdate = updateList.count( _.hasUpdate ) > 0
      if ( hasUpdate ) {
        performLinker( updateList )
      }
    } else {
      logger.info( s"Full linker build request." )
      performLinker( updateList )
    }
  }

  override def perform() : Unit = {
    if ( skipLinker || hasSkipMojo ) {
      reportSkipReason( "Skipping disabled goal execution." )
      return
    }
    performInvocation()
  }

}

@Description( """
Generate Scala.js runtime JavaScript for all scopes.
Invokes goals: scala-js-link-*.
""" )
@Mojo(
  name                         = A.mojo.`scala-js-link`,
  defaultPhase                 = LifecyclePhase.PROCESS_CLASSES,
  requiresDependencyResolution = ResolutionScope.TEST
)
class ScalaJsLinkArkonMojo extends ScalaJsLinkAnyMojo
  with scalajs.BuildMain
  with scalajs.BuildTest
  with scalajs.ParamsLinkMain
  with scalajs.ParamsLinkTest {

  override def mojoName = A.mojo.`scala-js-link`

  override def performInvocation() : Unit = {
    executeSelfMojo( A.mojo.`scala-js-link-main` )
    executeSelfMojo( A.mojo.`scala-js-link-test` )
  }

}

@Description( """
Generate Scala.js runtime JavaScript for scope=main.
Provides incremental linking in M2E.
A member of goal=scala-js-link.
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
Provides incremental linking in M2E.
A member of goal=scala-js-link.
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
