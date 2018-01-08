package com.carrotgarden.maven.scalor.zinc

import scala.tools.nsc.settings.ScalaVersion
import scala.tools.nsc.settings.NoScalaVersion

import org.apache.maven.artifact.Artifact

import com.carrotgarden.maven.scalor._

import java.security.MessageDigest
import java.nio.charset.StandardCharsets

import java.io.File

import ScalaInstall._
import Module._

/**
 * Scala compiler installation descriptor.
 *
 * Abstract away form Scala, Scala IDE, Zinc.
 * @param bridge - compiler-bridge jar
 * @param compiler - compiler-bridge jar
 * @param library - scala-library jar
 * @param reflect - scala-reflect jar
 * @param bridgeList - compiler-bridge with dependencies
 * @param compilerList - scala-compiler with dependencies
 * @param pluginDefineList - declared scala compiler plugins
 */
case class ScalaInstall(
  title :            String,
  bridge :           Module,
  compiler :         Module,
  library :          Module,
  reflect :          Module,
  bridgeList :       Seq[ Module ] = Seq(),
  compilerList :     Seq[ Module ] = Seq(),
  pluginDefineList : Seq[ Module ] = Seq(),
  version :          ScalaVersion  = NoScalaVersion
) {

  /**
   * Dependencies for Scala IDE Zinc.
   */
  def extraJars : Seq[ Module ] = {
    compilerList
      .filterNot( _.moduleType == ScalaCompiler )
      .filterNot( _.moduleType == ScalaLibrary )
  }

  /**
   * Dependencies for Maven Scalor Zinc.
   */
  def zincJars : Seq[ Module ] = {
    compilerList
  }

  /**
   * Insert cumulative path digest into the title.
   */
  def withTitleDigest : ScalaInstall = {
    val digest = digestFrom( this )
    this.copy( title = title + " [" + digest + "]" )
  }

}

object ScalaInstall {

  import util.Hex
  import util.Maven._
  import util.Error._

  def digestFrom( install : ScalaInstall ) : String = {
    val text = entriesFrom( install ).mkString( ";" )
    val array = text.getBytes( StandardCharsets.UTF_8 )
    val digest = MessageDigest.getInstance( "MD5" ).digest( array )
    val result = new String( Hex.value( digest ) );
    result
  }

  def entriesFrom( install : ScalaInstall ) : Seq[ String ] = {
    import install._
    val moduleList : Seq[ Module ] =
      ( Seq( compiler, library ) ++ extraJars ++ pluginDefineList )
        .sortBy( _.binaryArtifact.toString )
    moduleList.flatMap( module => Module.entiresFrom( module ) )
  }

  def fail(
    list :   List[ Module ],
    module : String, regex : String
  ) = {
    val listLine = list.mkString( "[ ", " ; ", " ]" )
    val helpText = "Can not resolve Scala installaton."
    Throw( s"${helpText} Missing module '${module}' with regex '${regex}' in list ${listLine}." )
  }

  def find(
    list :       List[ Module ],
    module :     String,
    regex :      String,
    moduleType : Module.Type
  ) : Module = {
    list.find( _.moduleType == moduleType ).getOrElse( fail( list, module, regex ) )
  }

  def make(
    detector : Detector,
    list :     List[ Artifact ]
  ) : List[ Module ] = {
    val binaryList = list.filter( _.getClassifier == null )
    val sourceList = list.filter( _.getClassifier == "sources" )
    val moduleList = binaryList.map { binaryArtifact =>
      val sourceArtifact = sourceList.find {
        sourceCandidate => hasIdentityMatch( binaryArtifact, sourceCandidate, true )
      }
      val moduleType = detector.moduleType( binaryArtifact )
      Module( moduleType, binaryArtifact, sourceArtifact )
    }
    moduleList
  }

  import base.Params._

  /**
   * Produce installation from dependency list.
   */
  def apply(
    title :    String,
    detector : Detector,
    define :   DefineResponse
  ) : ScalaInstall = {
    import define._

    val bridgeList = make( detector, defineBridge )
    val compilerList = make( detector, defineCompiler )
    val pluginDefineList = make( detector, definePluginList )

    import detector._
    val bridge = find( bridgeList, "bridge", regexCompilerBridge, CompilerBridge )
    val compiler = find( compilerList, "compiler", regexScalaCompiler, ScalaCompiler )
    val library = find( compilerList, "library", regexScalaLibrary, ScalaLibrary )
    val reflect = find( compilerList, "reflect", regexScalaReflect, ScalaReflect )

    val pluginList = pluginDefineList
      .filter( _.moduleType == CompilerPlugin )

    val version = ScalaVersion( versionFrom( library ) )

    ScalaInstall(
      title,
      bridge           = bridge,
      compiler         = compiler,
      library          = library,
      reflect          = reflect,
      bridgeList       = bridgeList,
      compilerList     = compilerList,
      pluginDefineList = pluginList,
      version          = version
    )

  }

}
