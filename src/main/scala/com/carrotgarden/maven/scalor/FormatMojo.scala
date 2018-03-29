package com.carrotgarden.maven.scalor

import java.io.File
import java.nio.charset.Charset

import com.carrotgarden.maven.tools.Description
import com.carrotgarden.maven.scalor.meta.Macro
import com.carrotgarden.maven.scalor.util.Error.Throw
import com.carrotgarden.maven.scalor.util.Maven

import org.apache.maven.plugin.AbstractMojo
import org.apache.maven.plugins.annotations._
import org.apache.maven.project.MavenProject

import A.mojo._
import com.carrotgarden.maven.scalor.format.Format
import com.carrotgarden.maven.scalor.util.Text

/**
 * Shared format mojo interface.
 */
trait FormatAnyMojo extends AbstractMojo
  with base.Mojo
  with base.Params
  with base.Logging
  with base.SkipMojo
  with format.Format
  with format.ParamsAny {

  import format.Format._

  @Description( """
  Flag to skip goal execution: <code>format-*</code>.
  """ )
  @Parameter(
    property     = "scalor.skipFormat",
    defaultValue = "false"
  )
  var skipFormat : Boolean = _

  lazy val formatCharset = {
    Charset.forName( formatSourceEncoding )
  }

  /**
   * Provide user reporting.
   */
  def reportFormat( resultList : Array[ Format.Result ] ) : Unit = {
    val changedList = resultList.filter( _.hasChange )
    val successList = resultList.filter( _.hasSuccess )
    val failureList = resultList.filter( _.hasFailure )
    if ( formatLogTotal ) {
      logger.info(
        s"Results: changed=${changedList.size} failure=${failureList.size} success=${successList.size} total=${resultList.size}"
      )
    }
    if ( formatLogChanged && !changedList.isEmpty ) {
      val report = Text.reportArray( changedList.map { result =>
        s"${result.path}"
      } )
      logger.info( s"Changed files:\n${report}" )
    }
    if ( formatLogSuccess && !successList.isEmpty ) {
      val report = Text.reportArray( successList.map { result =>
        s"${result.path} changed='${result.hasChange}'"
      } )
      logger.info( s"Success files:\n${report}" )
    }
    if ( formatLogFailure && !failureList.isEmpty ) {
      val report = Text.reportArray( failureList.map { result =>
        val error = result.error.get
        val trace = if ( formatLogTraces )
          util.Error.reportStackTrace( error ) else ""
        s"${result.path} failure='${error}' ${trace}"
      } )
      logger.info( s"Failure files:\n${report}" )
    }
  }

  /**
   * Format project sources.
   */
  def performFormat() : Unit = {
    if ( formatJavaEnable ) {
      logger.info( s"Formatting project Java sources." )
      val fileList = buildSourceJavaList( buildSourceFolders ).sorted
      val settingsFile = formatJavaSettingsFile( project, this )
      logger.info( s"Java formatter settings file: ${settingsFile}" )
      val formatContext = formatJavaContext( settingsFile )
      val resultList = formatJavaList( fileList, formatCharset, formatContext )
      reportFormat( resultList )
    }
    if ( formatScalaEnable ) {
      logger.info( s"Formatting project Scala sources." )
      val fileList = buildSourceScalaList( buildSourceFolders ).sorted
      val settingsFile = formatScalaSettingsFile( project, this )
      logger.info( s"Scala formatter settings file: ${settingsFile}" )
      val formatContext = formatScalaContext( settingsFile )
      val resultList = formatScalaList( fileList, formatCharset, formatContext )
      reportFormat( resultList )
    }
  }

  override def perform() : Unit = {
    if ( skipFormat || hasSkipMojo ) {
      reportSkipReason( "Skipping disabled goal execution." )
      return
    }
    if ( hasIncremental ) {
      reportSkipReason( "Skipping incremental build invocation." )
      return
    }
    performFormat()
  }

}

@Description( """
Format sources for all compilation scopes.
Invokes goals: format-*
""" )
@Mojo(
  name                         = A.mojo.`format`,
  defaultPhase                 = LifecyclePhase.GENERATE_SOURCES,
  requiresDependencyResolution = ResolutionScope.NONE
)
class FormatArkonMojo extends FormatAnyMojo
  with format.ParamsBuildMacro
  with format.ParamsBuildMain
  with format.ParamsBuildTest {

  override def mojoName = A.mojo.`format`

  override def buildResourceFolders = throwNotUsed
  override def buildSourceFolders = throwNotUsed

  override def performFormat() : Unit = {
    executeSelfMojo( A.mojo.`format-macro` )
    executeSelfMojo( A.mojo.`format-main` )
    executeSelfMojo( A.mojo.`format-test` )
  }

}

@Description( """
Format sources for compilation scope=macro.
A member of goal=format.
""" )
@Mojo(
  name                         = A.mojo.`format-macro`,
  defaultPhase                 = LifecyclePhase.GENERATE_SOURCES,
  requiresDependencyResolution = ResolutionScope.NONE
)
class FormatMacroMojo extends FormatAnyMojo
  with format.ParamsBuildMacro {

  override def mojoName = A.mojo.`format-macro`

}

@Description( """
Format sources for compilation scope=main.
A member of goal=format.
""" )
@Mojo(
  name                         = A.mojo.`format-main`,
  defaultPhase                 = LifecyclePhase.GENERATE_SOURCES,
  requiresDependencyResolution = ResolutionScope.NONE
)
class FormatMainMojo extends FormatAnyMojo
  with format.ParamsBuildMain {

  override def mojoName = A.mojo.`format-main`

}

@Description( """
Format sources for compilation scope=test.
A member of goal=format.
""" )
@Mojo(
  name                         = A.mojo.`format-test`,
  defaultPhase                 = LifecyclePhase.GENERATE_TEST_SOURCES,
  requiresDependencyResolution = ResolutionScope.NONE
)
class FormatTestMojo extends FormatAnyMojo
  with format.ParamsBuildTest {

  override def mojoName = A.mojo.`format-test`

}
