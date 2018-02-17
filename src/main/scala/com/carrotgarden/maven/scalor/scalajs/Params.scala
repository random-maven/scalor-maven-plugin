package com.carrotgarden.maven.scalor.scalajs

import org.apache.maven.plugins.annotations.Parameter

import com.carrotgarden.maven.scalor.base
import com.carrotgarden.maven.tools.Description

trait ParamsLinkAny extends AnyRef
  with ParamsRegex
  with ParamsLogging
  with ParamsLibrary
  with ParamsOptsAny
  with ParamsInitListAny {

}

trait ParamsLinkMain extends ParamsLinkAny
  with ParamsOptsMain
  with ParamsInitListMain {

}

trait ParamsLinkTest extends ParamsLinkAny
  with ParamsOptsTest
  with ParamsInitListTest {

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

/**
 * List of Scala.js module initializers.
 */
trait ParamsInitListAny {

  @Description( """
  Regular expression used to extract Scala.js module initializer configuration.
  Must provide extractor for pattern: <code>packageName.ClassName.methodName(arg0,arg1,...)</code>.
  Must provide exactly 3 capture groups: class, method, arguments.
  Arguments separator is hard-coded comma <code>,</code> not part of this regex.
  """ )
  @Parameter(
    property     = "scalor.linkerInitializerRegex",
    defaultValue = ParamsInitListAny.initializerRegex
  )
  var linkerInitializerRegex : String = _

  /**
   * List of Scala.js module initializers.
   */
  def linkerInitializerList : Array[ String ]

}

object ParamsInitListAny {

  final val initializerRegex = """([a-zA-Z_.0-9]+)[.]([a-zA-Z_0-9]+)[(]([^()]*)[)]"""

  final val initializerDescription = """
  List of Scala.js module initializer declarations.
  Module initializers are JavaScript equivalent to Java <code>main</code> method convention.
  They are invoked when linker-generated <code>runtime.js</code> script is loaded in Node.js or Browser JS-VM.
  Each list entry must be a fully qualified class name of Scala object, 
  with method name, with argument list, which follows Java <code>main</code> contract,
  and is annotated with <code>@JSExportTopLevel</code>, <code>@JSExport</code>.
  For example, the following <code>pom.xml</code> plugin configuration entry:
<pre>
&lt;linkerTestInitializerList&gt;
   &lt;initializer&gt;test.Init.main(build=${project.artifactId},stamp=${maven.build.timestamp})&lt;/initializer&gt;
&lt;/linkerTestInitializerList&gt;
</pre> 
   must be accompanied by Scala object in file <code>test/Init.scala</code>:
<pre>
package test
import scala.scalajs.js.annotation._
@JSExportTopLevel( "test.Init" )
object Init {
  @JSExport
  // Arguments contain "build" and "stamp" entries from pom.xml.
  def main( args : Array[ String ] ) : Unit = {
    // This module output is printed on JS-VM console (Node.js or Browser).
    println( s"init-main: ${args(0)} ${args(1)}" )
  }
}
</pre>
  This list is empty by default.
  Extractor parameter: <a href="#linkerInitializerRegex"><b>linkerInitializerRegex</b></a>.
  """
}

trait ParamsInitListMain extends ParamsInitListAny {

  @Description( ParamsInitListAny.initializerDescription )
  @Parameter(
    property     = "scalor.linkerMainInitializerList",
    defaultValue = ""
  )
  var linkerMainInitializerList : Array[ String ] = Array.empty

  override def linkerInitializerList = linkerMainInitializerList

}

trait ParamsInitListTest extends ParamsInitListAny {
  import ParamsInitListAny._

  @Description( ParamsInitListAny.initializerDescription )
  @Parameter(
    property     = "scalor.linkerTestInitializerList",
    defaultValue = ""
  )
  var linkerTestInitializerList : Array[ String ] = Array.empty

  override def linkerInitializerList = linkerTestInitializerList

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
