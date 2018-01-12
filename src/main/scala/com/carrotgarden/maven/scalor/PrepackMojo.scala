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
import java.nio.file.Paths

/**
 * Prepare build resources for packaging for given scope.
 */
trait PrepackAnyMojo extends AbstractMojo
  with base.Dir
  with base.Mojo
  with base.Params
  with base.Logging
  with base.SkipMojo
  with base.BuildAnyTarget
  with eclipse.Context {

  import util.Folder._
  import util.Error._

  @Description( """
  Enable to log package origin/output folders.
  """ )
  @Parameter(
    property     = "scalor.prepackLogSummary",
    defaultValue = "true"
  )
  var prepackLogSummary : Boolean = _

  @Description( """
  Enable to log package file transfer details: origin -> output.
  """ )
  @Parameter(
    property     = "scalor.prepackLogTransfer",
    defaultValue = "false"
  )
  var prepackLogTransfer : Boolean = _

  @Description( """
  Create package output folder when missing.
  """ )
  @Parameter(
    property     = "scalor.prepackEnsureOutputFolder",
    defaultValue = "true"
  )
  var prepackEnsureOutputFolder : Boolean = _

  @Description( """
  Fail build when package origin folder is missing.
<pre>
  true  -> fail with error
  false -> only report error
</pre>
  """ )
  @Parameter(
    property     = "scalor.prepackFailOnInvalidOrigin",
    defaultValue = "false"
  )
  var prepackFailOnInvalidOrigin : Boolean = _

  @Description( """
  Fail build when package output folder is missing.
<pre>
  true  -> fail with error
  false -> only report error
</pre>
  """ )
  @Parameter(
    property     = "scalor.prepackFailOnInvalidOutput",
    defaultValue = "false"
  )
  var prepackFailOnInvalidOutput : Boolean = _

  /**
   * Fail build when origin folder is missing.
   */
  def assertOrigin( path : Path ) = {
    if ( prepackFailOnInvalidOrigin ) {
      Throw( s"Invalid origin: ${path}" )
    } else {
      say.warn( s"Skipping invalid origin: ${path}" )
    }
  }

  /**
   * Fail build when output folder is missing.
   */
  def assertOutput( path : Path ) = {
    if ( prepackFailOnInvalidOutput ) {
      Throw( s"Invalid output: ${path}" )
    } else {
      say.warn( s"Skipping invalid output: ${path}" )
    }
  }

  /**
   * Transfer build resources from origin folder to output folder.
   */
  // FIXME copy only delta
  def performPrepare() : Unit = {

    val origin : Path = basedir.absolute( buildTargetFolder.toPath )
    if ( !Files.isDirectory( origin ) ) {
      assertOrigin( origin )
      return
    }

    val output : Path = basedir.absolute( buildOutputFolder.toPath )
    if ( prepackEnsureOutputFolder ) {
      say.info( s"Ensuring output folder." )
      ensureFolder( output )
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
        if ( prepackLogTransfer ) say.info( s"   ${relative}" )
      }
    }

    val hasSame = basedir.isSamePath( origin, output )
    if ( hasSame ) {
      say.info( "Skipping prepack: origin and output are same." )
      reportSummary()
    } else {
      say.info( "Copying build classes from origin into output." )
      reportSummary()
      // FIXME update eclipse
      transferFolder( origin, output, listener = reportTransfer )
    }
  }

  /**
   * Transfer build resources from origin folder to output folder.
   */
  override def perform() : Unit = {
    if ( hasSkipMojo ) {
      reportSkipReason( "Skipping disabled goal execution." )
      return
    }
    if ( hasIncremental ) {
      reportSkipReason( "Skipping incremental build invocation." )
      return
    }
    performPrepare()
  }

}

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
  Flag to skip goal execution: <code>prepack-macro</code>.
  """ )
  @Parameter(
    property     = "scalor.skipPrepackMacro",
    defaultValue = "false"
  )
  var skipPrepackMacro : Boolean = _

  override def hasSkipMojo = skipPrepackMacro

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
  Flag to skip goal execution: <code>prepack-main</code>.
  """ )
  @Parameter(
    property     = "scalor.skipPrepackMain",
    defaultValue = "false"
  )
  var skipPrepackMain : Boolean = _

  override def hasSkipMojo = skipPrepackMain

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
  Flag to skip goal execution: <code>prepack-test</code>.
  """ )
  @Parameter(
    property     = "scalor.skipPrepackTest",
    defaultValue = "false"
  )
  var skipPrepackTest : Boolean = _

  override def hasSkipMojo = skipPrepackTest

}

trait PrepackLinkerAnyMojo extends PrepackAnyMojo
  with scalajs.BuildMainMetaFolder {

  @Description( """
  Flag to skip goal execution: <code>prepack-linker-*</code>.
  """ )
  @Parameter(
    property     = "scalor.skipPrepackLinker",
    defaultValue = "false"
  )
  var skipPrepackLinker : Boolean = _

}

@Description( """
Prepare linker build resources for packaging for compilation scope=main.
""" )
@Mojo(
  name                         = `prepack-linker-main`,
  defaultPhase                 = LifecyclePhase.PROCESS_CLASSES,
  requiresDependencyResolution = ResolutionScope.NONE
)
class PrepackLinkerMainMojo extends PrepackLinkerAnyMojo
  with scalajs.BuildMainTarget {

  override def mojoName = `prepack-linker-main`

  @Description( """
  Flag to skip goal execution: <code>prepack-linker-main</code>.
  """ )
  @Parameter(
    property     = "scalor.skipPrepackLinkerMain",
    defaultValue = "false"
  )
  var skipPrepackLinkerMain : Boolean = _

  override def hasSkipMojo = skipPrepackLinker || skipPrepackLinkerMain

  /**
   * Final script location in the jar.
   */
  override def buildOutputFolder = new File( super.buildOutputFolder, linkerMainMetaFolder )

}

@Description( """
Prepare linker build resources for packaging for compilation scope=test.
""" )
@Mojo(
  name                         = `prepack-linker-test`,
  defaultPhase                 = LifecyclePhase.PROCESS_TEST_CLASSES,
  requiresDependencyResolution = ResolutionScope.NONE
)
class PrepackLinkerTestMojo extends PrepackLinkerAnyMojo
  with scalajs.BuildTestTarget {

  override def mojoName = `prepack-linker-test`

  @Description( """
  Flag to skip goal execution: <code>prepack-linker-test</code>.
  """ )
  @Parameter(
    property     = "scalor.skipPrepackLinkerTest",
    defaultValue = "false"
  )
  var skipPrepackLinkerTest : Boolean = _

  override def hasSkipMojo = skipPrepackLinker || skipPrepackLinkerTest

  /**
   * Final script location in the jar.
   */
  override def buildOutputFolder = new File( super.buildOutputFolder, linkerTestMetaFolder )

}
