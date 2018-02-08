package com.carrotgarden.maven.scalor.scalajs

import java.io.File

import com.carrotgarden.maven.tools.Description
import org.apache.maven.plugins.annotations.Parameter

import com.carrotgarden.maven.scalor._
import scala.beans.BeanProperty

trait ParamsLinkAny extends AnyRef
  with ParamsLogging
  with ParamsOptsAny {

}

trait ParamsLinkMain extends ParamsLinkAny
  with ParamsOptsMain {

}

trait ParamsLinkTest extends ParamsLinkAny
  with ParamsOptsTest {

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
  def linkerOptions : String = {
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
    { "checkIR":false, "parallel":true, "optimizer":true, "batchMode":false, "sourceMap":true, "prettyPrint":true }
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

  //  Normally, to ensure that test classes are not "optimized away",
  //  disable <code>optimizer</code>, or use explicit Scala.js export annotations.

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
    { "checkIR":true, "parallel":true, "optimizer":false, "batchMode":true, "sourceMap":true, "prettyPrint":false }
    """
  )
  var linkerTestOptionsProd : String = _

  override def linkerOptionsDevelopment = linkerTestOptionsDevs
  override def linkerOptionsProduction = linkerTestOptionsProd

}
