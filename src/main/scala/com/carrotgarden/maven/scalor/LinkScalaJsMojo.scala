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

@Description( """
Generate Scala.js runtime JavaScript for scope=main.
""" )
@Mojo(
  name                         = A.mojo.`link-scala-js-main`,
  defaultPhase                 = LifecyclePhase.PROCESS_CLASSES,
  requiresDependencyResolution = ResolutionScope.COMPILE
)
class LinkMainScalaJsMojo extends LinkAnyScalaJsMojo
  with scalajs.BuildMain {

  override def mojoName = A.mojo.`link-scala-js-main`

  @Description( """
  Flag to skip goal execution: link-scala-js-main.
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
  name                         = A.mojo.`link-scala-js-test`,
  defaultPhase                 = LifecyclePhase.PROCESS_TEST_CLASSES,
  requiresDependencyResolution = ResolutionScope.TEST
)
class LinkTestScalaJsMojo extends LinkAnyScalaJsMojo
  with scalajs.BuildTest {

  override def mojoName = A.mojo.`link-scala-js-test`

  @Description( """
  Flag to skip this goal execution: link-scala-js-test.
  """ )
  @Parameter(
    property     = "scalor.skipLinkerTest", //
    defaultValue = "false"
  )
  var skipLinkerTest : Boolean = _

  override def hasSkipMojo = skipLinkerTest

}

/**
 * Shared linker mojo interface.
 * Generate Scala.js runtime.js JavaScript with
 * https://github.com/scala-js/scala-js-cli
 */
trait LinkAnyScalaJsMojo extends AbstractMojo
  with base.Mojo
  with base.Params
  with base.Logging
  with base.SkipMojo
  with eclipse.Build
  with scalajs.Linker
  with scalajs.Build {

  @Description( """
  Flag to skip this execution: link-scala-js-*.
  """ )
  @Parameter(
    property     = "scalor.skipLinker", //
    defaultValue = "false"
  )
  var skipLinker : Boolean = _

  @Description( """
  Regular expression to detect when scalajs-library is present.
  Must provide version capture group.
  """ )
  @Parameter(
    property     = "scalor.linkerRegexScalajsLibrary", //
    defaultValue = "^scalajs-library_(.+)[.]jar$"
  )
  var linkerRegexScalajsLibrary : String = _

  @Description( """
  Invoke Scala.js linker only when scalajs-library is present
  in project dependencies with given scope.
  """ )
  @Parameter(
    property     = "scalor.linkerDetectScalajs", //
    defaultValue = "true"
  )
  var linkerDetectScalajs : Boolean = _

  /**
   * Discover scalajs-library on project class path.
   */
  def linkerLibraryOption = {
    resolveJar( linkerDependencyFilePath, linkerRegexScalajsLibrary ).right.toOption
  }

  @Description( """
  Flag to skip linker execution in Eclipse: link-scala-js-*.
  """ )
  @Parameter(
    property     = "scalor.skipLinkerEclipse", //
    defaultValue = "true"
  )
  var skipLinkerEclipse : Boolean = _

  override def perform() : Unit = {
    if ( skipLinker || hasSkipMojo ) {
      reportSkipReason( "Skipping disabled goal execution." )
      return
    }
    if ( hasEclipse && skipLinkerEclipse ) {
      reportSkipReason( "Skipping eclipse build invocation." )
      return
    }
    if ( hasIncremental ) {
      reportSkipReason( "Skipping incremental build invocation." )
      return
    }
    if ( linkerDetectScalajs ) {
      val option = linkerLibraryOption
      if ( option.isDefined ) {
        say.info( s"Detected scalajs-library: ${option.get.version}." )
        invokeLinker()
      } else {
        say.info( "Missing scalajs-library, skipping execution." )
      }
    } else {
      say.info( "Forcing linker invocation." )
      invokeLinker()
    }
  }

}
