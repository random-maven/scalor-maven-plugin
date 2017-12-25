package com.carrotgarden.maven.scalor.zinc

import java.io.File
import xsbti.compile.CompileOrder
import org.apache.maven.plugins.annotations._
import com.carrotgarden.maven.tools.Description

import com.carrotgarden.maven.scalor.base
import com.carrotgarden.maven.scalor.util.Folder._

/**
 * Compiler configuration parameters for scope=macro.
 */
trait ParamsMacro extends Params {

  @Description( """
  Location of zinc incremental compiler state file for scope=macro.
  Keep in sync with Eclipse.
  """ )
  @Parameter(
    property     = "scalor.zincCacheMacro",
    defaultValue = "${project.basedir}/.cache-macros"
  )
  var zincCacheMacro : File = _

}

/**
 * Compiler configuration parameters for scope=main.
 */
trait ParamsMain extends Params {

  @Description( """
  Location of zinc incremental compiler state file for scope=main.
  Keep in sync with Eclipse.
  """ )
  @Parameter(
    property     = "scalor.zincCacheMain",
    defaultValue = "${project.basedir}/.cache-main"
  )
  var zincCacheMain : File = _

}

/**
 * Compiler configuration parameters for scope=test.
 */
trait ParamsTest extends Params {

  @Description( """
  Location of zinc incremental compiler state file for scope=test.
  Keep in sync with Eclipse.
  """ )
  @Parameter(
    property     = "scalor.zincCacheTest",
    defaultValue = "${project.basedir}/.cache-tests"
  )
  var zincCacheTest : File = _

}

/**
 * Discover zinc compiler plugins from the class loader class path.
 */
trait ParamsPluginList {

  @Description( """
  Scala compiler plugin descriptor file name,
  stored inside compiler plugin jar.
  Used for auto discovery of compiler plugins
  form this maven plugin class path.
  """ )
  @Parameter(
    property     = "scalor.zincPluginDescriptor",
    defaultValue = "scalac-plugin.xml"
  )
  var zincPluginDescriptor : String = _

  /**
   * Discover zinc compiler plugins from the class loader class path.
   */
  def zincPluginDiscoveryList( loader : ClassLoader ) : Array[ File ] =
    findJarByResource( loader, zincPluginDescriptor )

}

/**
 *
 */
trait ParamsRegexJar {

  @Description( """
  Regular expression for scala-library jar file.
  Must provide version capture group.
  """ )
  @Parameter(
    property     = "scalor.zincRegexScalaLibrary",
    defaultValue = """^scala-library-(.+)[.]jar$"""
  )
  var zincRegexScalaLibrary : String = _

  @Description( """
  Regular expression for zinc compiler-bridge jar name.
  Must provide version capture group.
  """ )
  @Parameter(
    property     = "scalor.zincRegexCompilerBridge",
    defaultValue = """^compiler-bridge_([^-]+)-.+[.]jar$"""
  )
  var zincRegexCompilerBridge : String = _

  @Description( """
  Regular expression for scala-reflect jar file.
  Must provide version capture group.
  """ )
  @Parameter(
    property     = "scalor.zincRegexScalaReflect",
    defaultValue = """^scala-reflect-(.+)[.]jar$"""
  )
  var zincRegexScalaReflect : String = _

  @Description( """
  Regular expression for scala-compiler jar file.
  Must provide version capture group.
  """ )
  @Parameter(
    property     = "scalor.zincRegexScalaCompiler",
    defaultValue = """^scala-compiler-(.+)[.]jar$"""
  )
  var zincRegexScalaCompiler : String = _

}

/**
 * Incremental compiler configuration parameters.
 */
trait Params extends AnyRef
  with base.BuildEncoding
  with ParamsRegexJar
  with ParamsPluginList {

  import Params._

  // https://github.com/scala-ide/scala-ide/blob/master/org.scala-ide.sdt.core/src/org/scalaide/core/internal/project/scopes/BuildScopeUnit.scala

  @Description( """
  Regular expression for Java source file discovery.
  """ )
  @Parameter(
    property     = "scalor.zincRegexAnyJava",
    defaultValue = """^(.+)[.]java$"""
  )
  var zincRegexAnyJava : String = _

  @Description( """
  Regular expression for Scala source file discovery.
  """ )
  @Parameter(
    property     = "scalor.zincRegexAnyScala",
    defaultValue = """^(.+)[.]scala$"""
  )
  var zincRegexAnyScala : String = _

  /**
   * Regular expression for Java or Scala source file discovery.
   */
  def zincRegexAnySource = zincRegexAnyJava + "|" + zincRegexAnyScala

  @Description( """
  Flag to force version constistency check
  among dependency jars discovered with regular expressions.
  """ )
  @Parameter(
    property     = "scalor.zincVerifyVersion",
    defaultValue = "true"
  )
  var zincVerifyVersion : Boolean = _

  @Description( """
  Abort zinc incremental compiler after error limit.
  """ )
  @Parameter(
    property     = "scalor.zincMaximumErrors",
    defaultValue = "64"
  )
  var zincMaximumErrors : Int = _

  @Description( """
  Additional user options for JavaC compiler.
  """ )
  @Parameter(
    property     = "scalor.zincOptionsJavaC",
    defaultValue = """
    -deprecation ;
    """
  )
  var zincOptionsJavaC : String = _

  @Description( """
  Additional user options for ScalaC compiler.
  """ )
  @Parameter(
    property     = "scalor.zincOptionsScalaC",
    defaultValue = """
    -deprecation ; 
    -unchecked ;
    -feature ; 
    """
  )
  var zincOptionsScalaC : String = _

  @Description( """
  Option separator regular expression.
  """ )
  @Parameter(
    property     = "scalor.zincOptionSeparator",
    defaultValue = """[;\n]+"""
  )
  var zincOptionSeparator : String = _

  //  @Description( """
  //  Source encoding for Java and Scala files.
  //  """ )
  //  @Parameter(
  //    property     = "scalor.zincSourceEncoding",
  //    defaultValue = "${project.build.sourceEncoding}"
  //  )
  //  var zincSourceEncoding : String = _

  @Description( """
  Generated JVM class file version for java and scala.
  """ )
  @Parameter(
    property     = "scalor.zincTargetVersion",
    defaultValue = "${maven.compiler.target}"
  )
  var zincTargetVersion : String = _

  @Description( """
  Java+Scala compilation order: Mixed, JavaThenScala, ScalaThenJava.
  """ )
  @Parameter(
    property     = "scalor.zincCompileOrder",
    defaultValue = "Mixed"
  )
  var zincCompileOrder : CompileOrder = _

  @Description( """
  Activate logging of source files.
  """ )
  @Parameter(
    property     = "scalor.zincLogSourcesList",
    defaultValue = "false"
  )
  var zincLogSourcesList : Boolean = _

  @Description( """
  Enable zinc compiler output.
  Uses Maven logger at "INFO" level.
  Available zinc logger levels:
  1 = Debug, Info, Warn, Error;
  2 = Info, Warn, Error;
  3 = Warn, Error;
  4 = Error;
  """ )
  @Parameter(
    property     = "scalor.zincLogAtLevel",
    defaultValue = "1"
  )
  var zincLogAtLevel : Int = _

  @Description( """
  Activate logging of compiler build class path.
  """ )
  @Parameter(
    property     = "scalor.zincLogBuildClassPath",
    defaultValue = "false"
  )
  var zincLogBuildClassPath : Boolean = _

  @Description( """
  Activate logging of this plugin class path.
  """ )
  @Parameter(
    property     = "scalor.zincLogPluginClassPath",
    defaultValue = "false"
  )
  var zincLogPluginClassPath : Boolean = _

  @Description( """
  Activate logging of zinc compiler plugins
  discovered on this maven plugin class path.
  """ )
  @Parameter(
    property     = "scalor.zincLogPluginDiscoveryList",
    defaultValue = "false"
  )
  var zincLogPluginDiscoveryList : Boolean = _

  @Description( """
  Activate logging of incremental compiler units.
  """ )
  @Parameter(
    property     = "scalor.zincLogProgressUnit",
    defaultValue = "false"
  )
  var zincLogProgressUnit : Boolean = _

  @Description( """
  Activate logging of incremental compiler progress.
  """ )
  @Parameter(
    property     = "scalor.zincLogProgressRate",
    defaultValue = "false"
  )
  var zincLogProgressRate : Boolean = _

  /**
   * JavaC/ScalaC source encoding configuration stanza.
   */
  def zincStanzaEncoding = Array[ String ]( "-encoding", buildSourceEncoding )

  /**
   * JavaC target JVM class version configuration stanza.
   */
  def zincStanzaTargetJavaC = Array[ String ]( "-target", zincTargetVersion )

  /**
   * ScalaC target JVM class version configuration stanza.
   */
  def zincStanzaTargetScalaC = Array[ String ]( s"-target:jvm-${zincTargetVersion}" )

  /**
   * Produce final JavaC options from system options and user options.
   */
  def zincSettingsJavaC : Array[ String ] =
    zincStanzaEncoding ++ zincStanzaTargetJavaC ++ zincParseOptions( zincOptionsJavaC, zincOptionSeparator )

  /**
   * Produce final ScalaC options from system options and user options.
   */
  def zincSettingsScalaC : Array[ String ] =
    zincStanzaEncoding ++ zincStanzaTargetScalaC ++ zincParseOptions( zincOptionsScalaC, zincOptionSeparator )

}

object Params {

  /**
   * Produce clean compiler options.
   */
  def zincParseOptions( options : String, separator : String ) : Array[ String ] = {
    options
      .split( separator )
      .map( entry => entry.trim() )
      .filter( entry => !entry.isEmpty() )
  }

}
