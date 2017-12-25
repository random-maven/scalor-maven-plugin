package com.carrotgarden.maven.scalor

import org.apache.maven.plugin.AbstractMojo
import org.apache.maven.plugins.annotations._
import org.apache.maven.execution.MavenSession
import org.apache.maven.project.MavenProject
import org.apache.maven.plugin.BuildPluginManager
import org.apache.maven.plugin.MojoFailureException
import org.sonatype.plexus.build.incremental.BuildContext

import scala.collection.JavaConverters._

import java.util.Date
import java.io.File

import A.mojo._
import java.nio.file.Files
import java.nio.file.Path

import com.carrotgarden.maven.tools.Description

@Description( """
Prepare build resources for packaging for compilation scope=macro.
""" )
@Mojo(
  name                         = `prepack-macro`,
  defaultPhase                 = LifecyclePhase.COMPILE,
  requiresDependencyResolution = ResolutionScope.NONE
)
class PrepareMacroMojo extends PrepackAnyMojo
  with base.BuildMacroTarget {

  override def mojoName = `prepack-macro`

  @Description( """
  Flag to skip goal execution: prepack-macro.
  """ )
  @Parameter(
    property     = "scalor.skipPrepackMacro",
    defaultValue = "false"
  )
  var skipPrepackMacro : Boolean = _

  override def hasSkipMojo = skipPrepackMacro
  override def outputFolder = project.getBuild.getOutputDirectory

}

@Description( """
Prepare build resources for packaging for compilation scope=main.
""" )
@Mojo(
  name                         = `prepack-main`,
  defaultPhase                 = LifecyclePhase.COMPILE,
  requiresDependencyResolution = ResolutionScope.NONE
)
class PrepareMainMojo extends PrepackAnyMojo
  with base.BuildMainTarget {

  override def mojoName = `prepack-main`

  @Description( """
  Flag to skip goal execution: prepack-main.
  """ )
  @Parameter(
    property     = "scalor.skipPrepackMain",
    defaultValue = "false"
  )
  var skipPrepackMain : Boolean = _

  override def hasSkipMojo = skipPrepackMain
  override def outputFolder = project.getBuild.getOutputDirectory

}

@Description( """
Prepare build resources for packaging for compilation scope=test.
""" )
@Mojo(
  name                         = `prepack-test`,
  defaultPhase                 = LifecyclePhase.TEST_COMPILE,
  requiresDependencyResolution = ResolutionScope.NONE
)
class PrepareTestMojo extends PrepackAnyMojo
  with base.BuildTestTarget {

  override def mojoName = `prepack-test`

  @Description( """
  Flag to skip goal execution: prepack-test.
  """ )
  @Parameter(
    property     = "scalor.skipPrepackTest",
    defaultValue = "false"
  )
  var skipPrepackTest : Boolean = _

  override def hasSkipMojo = skipPrepackTest
  override def outputFolder = project.getBuild.getTestOutputDirectory

}

@Description( """
Prepare linker build resources for packaging for compilation scope=main.
""" )
@Mojo(
  name                         = `prepack-linker-main`,
  defaultPhase                 = LifecyclePhase.PROCESS_CLASSES,
  requiresDependencyResolution = ResolutionScope.NONE
)
class PrepackLinkerMainMojo extends PrepackAnyMojo
  with scalajs.BuildMainTarget {

  override def mojoName = `prepack-linker-main`

  @Description( """
  Flag to skip goal execution: prepack-linker-main.
  """ )
  @Parameter(
    property     = "scalor.skipPrepackLinkerMain",
    defaultValue = "false"
  )
  var skipPrepackLinkerMain : Boolean = _

  override def hasSkipMojo = skipPrepackLinkerMain
  override def outputFolder =
    project.getBuild.getOutputDirectory + File.separator + linkerMetaFolder

}

@Description( """
Prepare linker build resources for packaging for compilation scope=test.
""" )
@Mojo(
  name                         = `prepack-linker-test`,
  defaultPhase                 = LifecyclePhase.PROCESS_TEST_CLASSES,
  requiresDependencyResolution = ResolutionScope.NONE
)
class PrepackLinkerTestMojo extends PrepackAnyMojo
  with scalajs.BuildTestTarget {

  override def mojoName = `prepack-linker-test`

  @Description( """
  Flag to skip goal execution: prepack-linker-test.
  """ )
  @Parameter(
    property     = "scalor.skipPrepackLinkerTest",
    defaultValue = "false"
  )
  var skipPrepackLinkerTest : Boolean = _

  override def hasSkipMojo = skipPrepackLinkerTest
  override def outputFolder =
    project.getBuild.getTestOutputDirectory + File.separator + linkerMetaFolder

}

/**
 * Prepare build resources for packaging for given scope.
 */
trait PrepackAnyMojo extends AbstractMojo
  with base.Mojo
  with base.Params
  with base.Logging
  with base.Skip
  with base.BuildAnyTarget
  with eclipse.Build {
  import com.carrotgarden.maven.scalor.util.Folder._

  @Description( """
  Flag to skip goal execution: prepack-linker-*.
  """ )
  @Parameter(
    property     = "scalor.skipPrepackLinker",
    defaultValue = "false"
  )
  var skipPrepackLinker : Boolean = _

  @Description( """
  Report prepare folders.
  """ )
  @Parameter(
    property     = "scalor.prepackLogSummary",
    defaultValue = "true"
  )
  var prepackLogSummary : Boolean = _

  @Description( """
  Report file transfer details.
  """ )
  @Parameter(
    property     = "scalor.prepackLogTransfer",
    defaultValue = "true"
  )
  var prepackLogTransfer : Boolean = _

  @Description( """
  Create output folder when missing.
  """ )
  @Parameter(
    property     = "scalor.prepackEnsureOutput",
    defaultValue = "true"
  )
  var prepackEnsureOutput : Boolean = _

  @Description( """
  Fail build when origin folder is missing.
  """ )
  @Parameter(
    property     = "scalor.prepackFailOnInvalidSource",
    defaultValue = "false"
  )
  var prepackFailOnInvalidSource : Boolean = _

  @Description( """
  Fail build when output folder is missing.
  """ )
  @Parameter(
    property     = "scalor.prepackFailOnInvalidTarget",
    defaultValue = "false"
  )
  var prepackFailOnInvalidTarget : Boolean = _

  /**
   * Default project build output folder for given scope.
   */
  def outputFolder : String

  /**
   * Fail build when origin folder is missing.
   */
  def assertOrigin( path : Path ) = {
    if ( prepackFailOnInvalidSource ) {
      throw new RuntimeException( s"Invalid origin: ${path}" )
    } else {
      say.warn( s"Skipping invalid origin: ${path}" )
    }
  }

  /**
   * Fail build when output folder is missing.
   */
  def assertOutput( path : Path ) = {
    if ( prepackFailOnInvalidTarget ) {
      throw new RuntimeException( s"Invalid output: ${path}" )
    } else {
      say.warn( s"Skipping invalid output: ${path}" )
    }
  }

  /**
   * Transfer build resources from origin folder to output folder.
   */
  def performPrepare() : Unit = {

    val origin : Path = buildTargetFolder.getCanonicalFile.toPath
    if ( !Files.isDirectory( origin ) ) {
      assertOrigin( origin )
      return
    }

    val output : Path = new File( outputFolder ).getCanonicalFile.toPath
    if ( prepackEnsureOutput ) {
      ensureFolder( output.toFile )
    }
    if ( !Files.isDirectory( output ) ) {
      assertOutput( output )
      return
    }

    def reportSummary() = if ( prepackLogSummary ) {
      say.info( s"origin: ${origin}" )
      say.info( s"output: ${output}" )
    }

    val reportTransfer = new TransferListener {
      override def onFile( source : Path, target : Path, relative : Path ) : Unit = {
        if ( prepackLogTransfer ) say.info( s"path: ${relative}" )
        // FIXME update eclipse
      }
    }

    val hasSame = Files.isSameFile( origin, output )
    if ( hasSame ) {
      say.info( "Skipping prepare: origin and output are same." )
      reportSummary()
    } else {
      say.info( "Copying build classes from origin into output." )
      reportSummary()
      transferFolder( origin, output, listener = reportTransfer )
    }
  }

  /**
   * Transfer build resources from origin folder to output folder.
   */
  override def perform() : Unit = {
    if ( skipPrepackLinker || hasSkipMojo ) {
      say.info( "Skipping disabled goal execution." )
      return
    }
    if ( hasIncremental ) {
      say.info( "Skipping incremental build invocation." )
      return
    }
    performPrepare()
  }

}
