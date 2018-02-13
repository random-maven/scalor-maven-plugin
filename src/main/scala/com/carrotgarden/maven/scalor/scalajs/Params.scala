package com.carrotgarden.maven.scalor.scalajs

import java.io.File

import com.carrotgarden.maven.tools.Description
import org.apache.maven.plugins.annotations.Parameter

import com.carrotgarden.maven.scalor._
import scala.beans.BeanProperty

trait ParamsLinkAny extends AnyRef
  with ParamsRegex
  with ParamsLogging
  with ParamsLibrary
  with ParamsOptsAny {

}

trait ParamsLinkMain extends ParamsLinkAny
  with ParamsOptsMain {

}

trait ParamsLinkTest extends ParamsLinkAny
  with ParamsOptsTest {

}

trait ParamsRegex {

  @Description( """
  Regular expression used to discover Scala.js IR classes from class path.
  """ )
  @Parameter(
    property     = "scalor.linkerClassRegex",
    defaultValue = "^.+[.]sjsir$"
  )
  var linkerClassRegex : String = _

}

trait ParamsLibrary {

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

}

trait ParamsLogging {

  @Description( """
  Enable logging of linker options.
  Use to review actual Scala.js linker invocation configuration.
  """ )
  @Parameter(
    property     = "scalor.linkerLogOptions", //
    defaultValue = "false"
  )
  var linkerLogOptions : Boolean = _

  @Description( """
  Enable logging of Scala.js linker runtime.js.
  Use to review actual generated output <code>runtime.js</code> location.
  """ )
  @Parameter(
    property     = "scalor.linkerLogRuntime", //
    defaultValue = "true"
  )
  var linkerLogRuntime : Boolean = _

  @Description( """
  Enable logging of Scala.js linker class path.
  Use to review actual resources used for <code>*.sjsir</code> class discovery.
  """ )
  @Parameter(
    property     = "scalor.linkerLogClassPath", //
    defaultValue = "false"
  )
  var linkerLogClassPath : Boolean = _

  @Description( """
  Enable logging of Scala.js linker build phase statistics, including phase durations.
  Use to review linker performance profile.
  """ )
  @Parameter(
    property     = "scalor.linkerLogBuildStats", //
    defaultValue = "false"
  )
  var linkerLogBuildStats : Boolean = _

  @Description( """
  Enable logging of Scala.js linker update result of M2E incremental change detection.
  Use to review actual <code>*.sjsir</code> classes which triggered Eclipse linker build.
  """ )
  @Parameter(
    property     = "scalor.linkerLogUpdateResult", //
    defaultValue = "false"
  )
  var linkerLogUpdateResult : Boolean = _

}

/**
 * Linker invocation options.
 */
trait ParamsOptsAny extends base.ParamsAny {

  @Description( """
  List of names of environment variables, which, 
  when detected, activate <code>integration</code> Scala.js linker options.
  Otherwise, linker will use <code>interactive</code> linker options.
  IDE <code>interactive</code> options are for development.
  CI <code>integration</code> options are for production.
  Separator parameter: <a href="#commonSequenceSeparator"><b>commonSequenceSeparator</b></a>.
  """ )
  @Parameter(
    property     = "scalor.linkerEnvVarListCI",
    defaultValue = """
    CONTINUOUS_INTEGRATION ★ JENKINS_HOME ★ HUDSON_HOME ★  
    """
  )
  var linkerEnvVarListCI : String = _

  /**
   * Linker engine options for detected IDE vs CI mode.
   */
  def linkerOptionsActive : String = {
    val varsList = parseCommonList( linkerEnvVarListCI )
    val hasCI = varsList.find( name => System.getenv.get( name ) != null ).isDefined
    if ( hasCI ) linkerOptionsProduction else linkerOptionsDevelopment
  }

  /**
   * IDE <code>interactive</code> options are for development.
   */
  def linkerOptionsDevelopment : String

  /**
   * CI <code>integration</code> options are for production.
   */
  def linkerOptionsProduction : String

}

/**
 * Linker invocation options for scope=main.
 */
trait ParamsOptsMain extends ParamsOptsAny {

  @Description( """
  Scala.js linker options for scope=main, mode=IDE/development.
  Scala.js linker options reference:
  <a href="https://github.com/scala-js/scala-js/blob/master/linker/shared/src/main/scala/org/scalajs/linker/StandardLinker.scala">
    StandardLinker.scala
  </a>
  Configuration detection parameter: <a href="#linkerEnvVarListCI"><b>linkerEnvVarListCI</b></a>
  Uses simple <code>json</code> format.
  """ )
  @Parameter(
    property     = "scalor.linkerMainOptionsDevs",
    defaultValue = """
    { "checkIR":false, "parallel":true, "optimizer":false, "batchMode":false, "sourceMap":true, "prettyPrint":true }
    """
  )
  var linkerMainOptionsDevs : String = _

  @Description( """
  Scala.js linker options for scope=main, mode=CI/production.
  Scala.js linker options reference:
  <a href="https://github.com/scala-js/scala-js/blob/master/linker/shared/src/main/scala/org/scalajs/linker/StandardLinker.scala">
    StandardLinker.scala
  </a>
  Configuration detection parameter: <a href="#linkerEnvVarListCI"><b>linkerEnvVarListCI</b></a>
  Uses simple <code>json</code> format.
  """ )
  @Parameter(
    property     = "scalor.linkerMainOptionsProd",
    defaultValue = """
    { "checkIR":true, "parallel":true, "optimizer":true, "batchMode":true, "sourceMap":true, "prettyPrint":false }
    """
  )
  var linkerMainOptionsProd : String = _

  override def linkerOptionsDevelopment = linkerMainOptionsDevs
  override def linkerOptionsProduction = linkerMainOptionsProd

}

/**
 * Linker invocation options for scope=test.
 */
trait ParamsOptsTest extends ParamsOptsAny {

  @Description( """
  Scala.js linker options for scope=test, mode=IDE/development.
  Scala.js linker options reference:
  <a href="https://github.com/scala-js/scala-js/blob/master/linker/shared/src/main/scala/org/scalajs/linker/StandardLinker.scala">
    StandardLinker.scala
  </a>
  Configuration detection parameter: <a href="#linkerEnvVarListCI"><b>linkerEnvVarListCI</b></a>
  Uses simple <code>json</code> format.
  """ )
  @Parameter(
    property     = "scalor.linkerTestOptionsDevs",
    defaultValue = """
    { "checkIR":false, "parallel":true, "optimizer":false, "batchMode":false, "sourceMap":true, "prettyPrint":true }
    """
  )
  var linkerTestOptionsDevs : String = _

  @Description( """
  Scala.js linker options for scope=test, mode=CI/production.
  Scala.js linker options reference:
  <a href="https://github.com/scala-js/scala-js/blob/master/linker/shared/src/main/scala/org/scalajs/linker/StandardLinker.scala">
    StandardLinker.scala
  </a>
  Configuration detection parameter: <a href="#linkerEnvVarListCI"><b>linkerEnvVarListCI</b></a>
  Uses simple <code>json</code> format.
  """ )
  @Parameter(
    property     = "scalor.linkerTestOptionsProd",
    defaultValue = """
    { "checkIR":true, "parallel":true, "optimizer":true, "batchMode":true, "sourceMap":true, "prettyPrint":false }
    """
  )
  var linkerTestOptionsProd : String = _

  override def linkerOptionsDevelopment = linkerTestOptionsDevs
  override def linkerOptionsProduction = linkerTestOptionsProd

}
