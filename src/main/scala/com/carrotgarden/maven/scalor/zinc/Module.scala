package com.carrotgarden.maven.scalor.zinc

import com.carrotgarden.maven.scalor.util.Maven
import com.carrotgarden.maven.scalor.util.Folder

import org.apache.maven.artifact.Artifact

import java.io.File

/**
 * A module is binary jar with optional source jar.
 */
case class Module(
  moduleType :     Module.Type,
  binaryArtifact : Artifact,
  sourceArtifact : Option[ Artifact ]
)

object Module {

  /**
   * Classify supported modules.
   */
  sealed trait Type
  case object Unknown extends Type
  case object ScalaLibrary extends Type
  case object ScalaReflect extends Type
  case object ScalaCompiler extends Type
  case object CompilerBridge extends Type
  case object CompilerPlugin extends Type

  def fileFrom( module : Module ) : File = {
    import Folder._
    ensureCanonicalFile( module.binaryArtifact.getFile )
  }

  def versionFrom( module : Module ) : String = {
    module.binaryArtifact.getVersion
  }

  def entiresFrom( module : Module ) : Seq[ String ] = {
    import Folder._
    import module._
    Seq(
      ensureCanonicalPath( binaryArtifact.getFile ),
      sourceArtifact.map( artifact => ensureCanonicalPath( artifact.getFile ) ).getOrElse( "" )
    )
  }

  /**
   * Provide artifact module type detector.
   */
  case class Detector(
    regexCompilerBridge :      String,
    regexScalaCompiler :       String,
    regexScalaLibrary :        String,
    regexScalaReflect :        String,
    resourcePluginDescriptor : String
  ) {

    val RxCompilerBridge = regexCompilerBridge.r
    val RxScalaCompiler = regexScalaCompiler.r
    val RxScalaLibrary = regexScalaLibrary.r
    val RxScalaReflect = regexScalaReflect.r

    /**
     * Provide artifact module type detector.
     */
    def moduleType( artifact : Artifact ) : Type = {
      Maven.artifactIdentity( artifact ) match {
        case RxCompilerBridge() => CompilerBridge
        case RxScalaCompiler() => ScalaCompiler
        case RxScalaLibrary() => ScalaLibrary
        case RxScalaReflect() => ScalaReflect
        case _ if Maven.hasResourceMatch( `artifact`, resourcePluginDescriptor ) => CompilerPlugin
        case _ => Unknown
      }
    }

  }

}
