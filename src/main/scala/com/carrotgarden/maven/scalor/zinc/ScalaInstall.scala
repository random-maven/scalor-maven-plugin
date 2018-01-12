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
 *
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

/**
 * Install factory.
 */
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

  /**
   * Fail with formatted error.
   */
  def fail(
    list :   List[ Module ],
    module : String, regex : String
  ) = {
    val helpText = "Can not resolve Scala installaton."
    val listLine = list.mkString( "[ ", " ; ", " ]" )
    Throw( s"${helpText} Missing module '${module}' with regex '${regex}' in list ${listLine}." )
  }

  /**
   * Find scala module by type.
   */
  def moduleFind(
    list :       List[ Module ],
    module :     String,
    regex :      String,
    moduleType : Module.Type
  ) : Module = {
    list.find( _.moduleType == moduleType ).getOrElse( fail( list, module, regex ) )
  }

  /**
   * Build module list form artifact list containing binary and source jars.
   */
  def moduleList(
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
   * Produce installation from dependency definition.
   */
  def apply(
    title :    String,
    detector : Detector,
    define :   DefineResponse
  ) : ScalaInstall = {
    import define._

    val bridgeList = moduleList( detector, defineBridge )
    val compilerList = moduleList( detector, defineCompiler )
    val pluginDefineList = moduleList( detector, definePluginList )

    import detector._
    val bridge = moduleFind( bridgeList, "bridge", regexCompilerBridge, CompilerBridge )
    val compiler = moduleFind( compilerList, "compiler", regexScalaCompiler, ScalaCompiler )
    val library = moduleFind( compilerList, "library", regexScalaLibrary, ScalaLibrary )
    val reflect = moduleFind( compilerList, "reflect", regexScalaReflect, ScalaReflect )

    val pluginList = pluginDefineList
      .filter( _.moduleType == CompilerPlugin )

    val version = ScalaVersion( versionFrom( library ) )

    ScalaInstall(
      title            = title,
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
