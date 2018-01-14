package com.carrotgarden.maven.scalor

import org.apache.maven.plugin.AbstractMojo
import org.apache.maven.plugins.annotations._
import org.apache.maven.execution.MavenSession
import org.apache.maven.project.MavenProject
import org.apache.maven.plugin.BuildPluginManager
import org.apache.maven.plugin.MojoFailureException
import org.sonatype.plexus.build.incremental.BuildContext

import com.carrotgarden.maven.tools.Description

import A.mojo._
import org.apache.maven.reporting.MavenReport
import java.io.File
import java.util.Locale
import org.codehaus.doxia.sink.Sink

import util.Folder._
import org.apache.maven.archiver.MavenArchiver
import java.nio.file.Paths
import org.codehaus.plexus.archiver.jar.JarArchiver

/**
 *  Shared interface for site report mojo.
 */
trait ReportAnyMojo extends AbstractMojo
  with base.Mojo
  with base.Params
  with base.Logging
  with base.SkipMojo
  //
  with MavenReport
  with document.ReportAny {

  @Description( """
  Flag to skip document execution: <code>report-*</code>.
  """ )
  @Parameter(
    property     = "scalor.skipReport", //
    defaultValue = "false"
  )
  var skipReport : Boolean = _

  def performReport() : Unit = {
    log.info( s"TODO TODO TODO" )
  }

  override def perform() : Unit = {
    if ( skipReport || hasSkipMojo ) {
      reportSkipReason( "Skipping disabled goal execution." )
      return
    }
    performReport
  }

}

@Description( """
Produce Scaladoc site report for compilation scope=[macro,main].
""" )
@Mojo(
  name                         = `report-main`,
  defaultPhase                 = LifecyclePhase.PACKAGE,
  requiresDependencyResolution = ResolutionScope.COMPILE
)
class ReportMainMojo extends ReportAnyMojo
  with document.ReportMain {

  override def mojoName : String = `report-main`

  @Description( """
  Flag to skip goal execution: <code>report-main</code>.
  """ )
  @Parameter(
    property     = "scalor.skipReportMain", //
    defaultValue = "false"
  )
  var skipReportMain : Boolean = _

  override def hasSkipMojo = skipReportMain

}

@Description( """
Produce Scaladoc site report for compilation scope=[test].
""" )
@Mojo(
  name                         = `report-test`,
  defaultPhase                 = LifecyclePhase.PACKAGE,
  requiresDependencyResolution = ResolutionScope.COMPILE
)
class ReportTestMojo extends ReportAnyMojo
  with document.ReportTest {

  override def mojoName : String = `report-test`

  @Description( """
  Flag to skip goal execution: <code>report-test</code>.
  """ )
  @Parameter(
    property     = "scalor.skipReportTest", //
    defaultValue = "false"
  )
  var skipReportTest : Boolean = _

  override def hasSkipMojo = skipReportTest

}

/**
 *  Shared interface for Scaladoc mojo.
 */
trait ScaladocAnyMojo extends AbstractMojo
  //
  with document.Params
  with document.ScaladocAny
  //
  with base.Mojo
  with base.Params
  with base.Logging
  with base.SkipMojo
  //
  with base.Dir
  with base.Build
  with base.ParamsCompiler
  with resolve.Maven
  with zinc.Params
  with zinc.Compiler
  with zinc.Resolve {

  @Description( """
  Flag to skip document execution: <code>scaladoc-*</code>.
  """ )
  @Parameter(
    property     = "scalor.skipScaladoc", //
    defaultValue = "false"
  )
  var skipScaladoc : Boolean = _

  /**
   * Generate scaladoc content folder.
   */
  def performScaladoc() : Unit = {
    val folder = scaladocOutputFolder
    val options = Seq()
    log.info( s"Generating Scaladoc: ${folder}" )
    ensureFolder( folder )
    zincPerformDocument( folder, options )
  }

  lazy val scaladocArchive : File = {
    val scaladocTarget = basedirOutput.toFile
    ensureFolder( scaladocTarget )
    new File( scaladocTarget, scaladocArchiveName )
  }

  /**
   * Package scaladoc content into an archive jar.
   */
  def performPackage() : Unit = {
    log.info( s"Packaging Scaladoc: ${scaladocArchive}" )
    if ( scaladocArchive.exists ) { scaladocArchive.delete }
    val packager = new MavenArchiver()
    val archiveBuilder = new JarArchiver()
    packager.setArchiver( archiveBuilder )
    packager.setOutputFile( scaladocArchive )
    val includes = Array[ String ]( "**/**" ) // FIXME to config
    val excludes = Array[ String ]()
    packager.getArchiver.addDirectory( scaladocOutputFolder, includes, excludes )
    packager.createArchive( session, project, archiveConfig )
  }

  /**
   * Attach Scaladoc artifact to the project as deployment artifact.
   */
  def performAttach() : Unit = {
    if ( scaladocHasAttach ) {
      log.info( s"Attaching Scaladoc: ${scaladocArchive}" )
      projectHelper.attachArtifact( project, scaladocArchive, scaladocClassifier )
    }
  }

  override def perform() : Unit = {
    if ( skipScaladoc || hasSkipMojo ) {
      reportSkipReason( "Skipping disabled goal execution." )
      return
    }
    performScaladoc
    performPackage
    performAttach
  }

}

@Description( """
Produce project Scaladoc artifact for compilation scope=macro.
""" )
@Mojo(
  name                         = `scaladoc-macro`,
  defaultPhase                 = LifecyclePhase.PACKAGE,
  requiresDependencyResolution = ResolutionScope.COMPILE
)
class ScaladocMacroMojo extends ScaladocAnyMojo
  with document.ScaladocMacro
  with zinc.CompilerMacro {

  override def mojoName : String = `scaladoc-macro`

  @Description( """
  Flag to skip goal execution: <code>scaladoc-macro</code>.
  """ )
  @Parameter(
    property     = "scalor.skipScaladocMacro", //
    defaultValue = "false"
  )
  var skipScaladocMacro : Boolean = _

  override def hasSkipMojo = skipScaladocMacro

  override def performPackage = () // included in scope=main
  override def performAttach = () // included in scope=main

}

@Description( """
Produce project Scaladoc artifact for compilation scope=main.
""" )
@Mojo(
  name                         = `scaladoc-main`,
  defaultPhase                 = LifecyclePhase.PACKAGE,
  requiresDependencyResolution = ResolutionScope.COMPILE
)
class ScaladocMainMojo extends ScaladocAnyMojo
  with document.ScaladocMain
  with zinc.CompilerMain {

  override def mojoName : String = `scaladoc-main`

  @Description( """
  Flag to skip goal execution: <code>scaladoc-main</code>.
  """ )
  @Parameter(
    property     = "scalor.skipScaladocMain", //
    defaultValue = "false"
  )
  var skipScaladocMain : Boolean = _

  override def hasSkipMojo = skipScaladocMain

}

@Description( """
Produce project Scaladoc artifact for compilation scope=test.
""" )
@Mojo(
  name                         = `scaladoc-test`,
  defaultPhase                 = LifecyclePhase.PACKAGE,
  requiresDependencyResolution = ResolutionScope.TEST
)
class ScaladocTestMojo extends ScaladocAnyMojo
  with document.ScaladocTest
  with zinc.CompilerTest {

  override def mojoName : String = `scaladoc-test`

  @Description( """
  Flag to skip goal execution: <code>scaladoc-test</code>.
  """ )
  @Parameter(
    property     = "scalor.skipScaladocTest", //
    defaultValue = "false"
  )
  var skipScaladocTest : Boolean = _

  override def hasSkipMojo = skipScaladocTest

}
